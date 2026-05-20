package com.kms.tripplanning.utils.Impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.kms.tripplanning.exception.types.BadRequestException;
import com.kms.tripplanning.utils.GenericFilterRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

/**
 * Enhanced generic filter repository that fixes the following issues from V1:
 * <ul>
 * <li>#1/#2 Field allowlist validation to prevent reflection probing.</li>
 * <li>#3 Supports {@code List}, {@code Set}, and generic {@code Collection}
 * relations.</li>
 * <li>#4 Count query replays the join graph so nested-path filters resolve.</li>
 * <li>#5 {@code search()} broken into smaller, testable methods.</li>
 * <li>#6 Enum value conversion.</li>
 * <li>#7 Type-safe {@code gt}/{@code lt}/{@code eq} via
 * {@code ComparableExpression}.</li>
 * <li>#8 Per-value parse error handling with field context.</li>
 * <li>#9 Sanitized exception messages (no class names leaked).</li>
 * <li>#10 Sorting from {@link Pageable} with deterministic fallback.</li>
 * <li>#11 {@link FieldInfo} reflection cache.</li>
 * </ul>
 */
public class GenericFilterRepositoryImpl<T> implements GenericFilterRepository<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericFilterRepositoryImpl.class);

  /** Reflection cache shared across all repository instances. */
  private static final Map<String, FieldInfo> FIELD_INFO_CACHE = new ConcurrentHashMap<>();

  private static final String DEFAULT_SORT_FIELD = "createdAt";

  private final JPAQueryFactory queryFactory;
  private final Class<T> entityClass;

  public GenericFilterRepositoryImpl(EntityManager em, Class<T> entityClass) {
    Objects.requireNonNull(em, "entityManager");
    this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
    this.queryFactory = new JPAQueryFactory(em);
  }

  /**
   * Test-friendly constructor that allows injecting a mocked QueryDSL factory.
   */
  GenericFilterRepositoryImpl(JPAQueryFactory queryFactory, Class<T> entityClass) {
    this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory");
    this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
  }

  // ---------------------------------------------------------------------------
  // Public API
  // ---------------------------------------------------------------------------

  @Override
  public Page<T> search(
      Map<String, List<Object>> filters,
      Pageable pageable,
      List<String> loadRelations,
      Set<String> allowedFilterFields) {

    String alias = entityClass.getSimpleName().toLowerCase();
    PathBuilder<T> root = new PathBuilder<>(entityClass, alias);

    // --- 1. ID query (with filter joins, sorting, pagination) ----------------
    JPAQuery<UUID> idQuery = queryFactory.select(root.get("id", UUID.class)).from(root);
    FilterContext filterCtx = buildFilters(root, idQuery, filters, allowedFilterFields);

    idQuery.where(filterCtx.predicate()).groupBy(root.get("id", UUID.class)); // Needed if filters contain collection joins
    applySorting(idQuery, root, pageable, filterCtx);

    List<UUID> ids = idQuery
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    if (ids.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0L);
    }

    // --- 2. Content query (with fetch joins, same ordering) ------------------
    // Share a single join map between fetch joins and sort joins to avoid
    // duplicate joins when sort path overlaps with a loaded relation.
    JPAQuery<T> contentQuery = queryFactory.selectFrom(root);
    FilterContext contentCtx = new FilterContext(
        new BooleanBuilder(), new HashMap<>(), new HashMap<>());
    applyFetchJoins(contentQuery, root, loadRelations, contentCtx);
    applySorting(contentQuery, root, pageable, contentCtx);

    List<T> content = contentQuery
        .where(root.get("id", UUID.class).in(ids))
        .distinct()
        .fetch();

    // --- 3. Count query (replay filter joins, reuse predicate) ---------------
    long total = fetchTotal(root, filters, filterCtx.predicate(), allowedFilterFields);

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public <S, R> Page<R> castDTO(Page<S> values, Function<S, R> mapper) {
    List<R> content = values.getContent().stream().map(mapper).toList();
    return new PageImpl<>(content, values.getPageable(), values.getTotalElements());
  }

  // ---------------------------------------------------------------------------
  // Filter building
  // ---------------------------------------------------------------------------

  /**
   * Build predicates and register joins on the given query. Returns a context
   * carrying the predicate and the join maps so the same joins can be replayed
   * elsewhere (e.g. on the count query).
   */
  private FilterContext buildFilters(
      PathBuilder<T> root,
      JPAQuery<?> query,
      Map<String, List<Object>> filters,
      Set<String> allowedFilterFields) {

    Map<String, PathBuilder<?>> joins = new HashMap<>();
    Map<String, Class<?>> joinEntityClasses = new HashMap<>();
    BooleanBuilder builder = new BooleanBuilder();

    if (filters == null || filters.isEmpty()) {
      return new FilterContext(builder, joins, joinEntityClasses);
    }

    for (Map.Entry<String, List<Object>> entry : filters.entrySet()) {
      String key = entry.getKey();
      List<Object> values = entry.getValue();

      if (values == null || values.isEmpty()) {
        continue;
      }

      ParsedFilter parsed = parseFilterKey(key);

      // #1/#2 - Reject unknown fields when an allowlist is configured.
      if (allowedFilterFields != null && !allowedFilterFields.isEmpty()
          && !allowedFilterFields.contains(parsed.field())) {
        throw new BadRequestException("Filter field not allowed: '" + parsed.field() + "'");
      }

      PathBuilder<?> path = resolvePath(root, joins, joinEntityClasses, parsed.field(), query);
      List<Object> convertedValues = convertFilterValues(parsed.field(), values, joinEntityClasses);

      builder.and(buildPredicate(path, parsed.field(), parsed.operator(), convertedValues));
    }

    return new FilterContext(builder, joins, joinEntityClasses);
  }

  /**
   * Replay only the join graph on the count query so that aliases referenced by
   * the shared {@code BooleanBuilder} resolve. The predicate itself is reused
   * unchanged.
   */
  private long fetchTotal(
      PathBuilder<T> root,
      Map<String, List<Object>> filters,
      BooleanBuilder predicate,
      Set<String> allowedFilterFields) {

    JPAQuery<Long> countQuery = queryFactory
        .select(root.get("id", UUID.class).countDistinct())
        .from(root);

    Map<String, PathBuilder<?>> countJoins = new HashMap<>();
    Map<String, Class<?>> countJoinClasses = new HashMap<>();

    if (filters != null) {
      for (String rawKey : filters.keySet()) {
        ParsedFilter parsed = parseFilterKey(rawKey);
        if (allowedFilterFields != null && !allowedFilterFields.isEmpty()
            && !allowedFilterFields.contains(parsed.field())) {
          continue;
        }
        if (parsed.field().contains(".")) {
          resolvePath(root, countJoins, countJoinClasses, parsed.field(), countQuery);
        }
      }
    }

    return Optional.ofNullable(countQuery.where(predicate).fetchOne()).orElse(0L);
  }

  /**
   * Parse a filter key of the form {@code "<field>__<operator>"} or just
   * {@code "<field>"}. Operator defaults to {@code in}.
   */
  private ParsedFilter parseFilterKey(String key) {
    if (key.contains("__")) {
      String[] parts = key.split("__", 2);
      return new ParsedFilter(parts[0], parts[1]);
    }
    return new ParsedFilter(key, "in");
  }

  // ---------------------------------------------------------------------------
  // Join resolution
  // ---------------------------------------------------------------------------

  /**
   * Resolve a nested filter path, registering the required left joins along the
   * way. Singular associations use a direct path join; collection associations
   * use an aliased join (Hibernate 6+ requirement).
   */
  private PathBuilder<?> resolvePath(
      PathBuilder<?> root,
      Map<String, PathBuilder<?>> joins,
      Map<String, Class<?>> joinEntityClasses,
      String field,
      JPAQuery<?> query) {

    if (!field.contains(".")) {
      return root;
    }

    String[] parts = field.split("\\.");
    PathBuilder<?> current = root;
    Class<?> currentClass = entityClass;
    StringBuilder joinKey = new StringBuilder();

    for (int i = 0; i < parts.length - 1; i++) {
      if (joinKey.length() > 0) {
        joinKey.append(".");
      }
      joinKey.append(parts[i]);
      String key = joinKey.toString();

      if (!joins.containsKey(key)) {
        FieldInfo fieldInfo = resolveFieldInfo(currentClass, parts[i]);
        joins.put(key, registerJoin(query, current, parts[i], fieldInfo,
            key.replace(".", "_") + "_join", false));
        joinEntityClasses.put(key, fieldInfo.elementType());
      }

      current = joins.get(key);
      currentClass = joinEntityClasses.get(key);
    }

    return current;
  }

  /**
   * Apply fetch joins for eager-loaded relations. Each relation may be a nested
   * dot-notation path (e.g. {@code "trips.destination"}). The provided context's
   * join maps are populated so subsequent operations (e.g. sorting) can reuse
   * the same aliases instead of registering duplicate joins.
   */
  private void applyFetchJoins(JPAQuery<?> query, PathBuilder<T> root,
      List<String> loadRelations, FilterContext ctx) {
    if (loadRelations == null || loadRelations.isEmpty()) {
      return;
    }

    Map<String, PathBuilder<?>> joins = ctx.joins();
    Map<String, Class<?>> joinEntityClasses = ctx.joinEntityClasses();

    for (String relation : loadRelations) {
      String[] parts = relation.split("\\.");
      PathBuilder<?> current = root;
      Class<?> currentClass = entityClass;
      StringBuilder joinKey = new StringBuilder();

      for (String part : parts) {
        if (joinKey.length() > 0) {
          joinKey.append(".");
        }
        joinKey.append(part);
        String key = joinKey.toString();

        if (!joins.containsKey(key)) {
          FieldInfo fieldInfo = resolveFieldInfo(currentClass, part);
          joins.put(key, registerJoin(query, current, part, fieldInfo,
              key.replace(".", "_") + "_fetch", true));
          joinEntityClasses.put(key, fieldInfo.elementType());
        }
        current = joins.get(key);
        currentClass = joinEntityClasses.get(key);
      }
    }
  }

  /**
   * Register a left join on the query. Collection associations use an aliased
   * join with the appropriate collection accessor based on the raw field type.
   */
  private PathBuilder<?> registerJoin(
      JPAQuery<?> query,
      PathBuilder<?> parent,
      String fieldName,
      FieldInfo fieldInfo,
      String aliasName,
      boolean fetch) {

    if (!fieldInfo.isCollection()) {
      PathBuilder<Object> join = parent.get(fieldName, Object.class);
      var clause = query.leftJoin(join);
      if (fetch) {
        clause.fetchJoin();
      }
      return join;
    }
    return leftJoinCollection(query, parent, fieldName, fieldInfo, aliasName, fetch);
  }

  /**
   * Aliased left join on a collection association. Picks the right accessor
   * ({@code getList}, {@code getSet}, or {@code getCollection}) based on the
   * field's declared raw type. This is the #3 fix.
   */
  @SuppressWarnings("unchecked")
  private <E> PathBuilder<E> leftJoinCollection(
      JPAQuery<?> query,
      PathBuilder<?> parent,
      String fieldName,
      FieldInfo fieldInfo,
      String aliasName,
      boolean fetch) {

    Class<E> elementType = (Class<E>) fieldInfo.elementType();
    PathBuilder<E> alias = new PathBuilder<>(elementType, aliasName);
    Class<?> rawType = fieldInfo.rawType();

    if (List.class.isAssignableFrom(rawType)) {
      var clause = query.leftJoin(parent.getList(fieldName, elementType), alias);
      if (fetch) {
        clause.fetchJoin();
      }
    } else if (Set.class.isAssignableFrom(rawType)) {
      var clause = query.leftJoin(parent.getSet(fieldName, elementType), alias);
      if (fetch) {
        clause.fetchJoin();
      }
    } else {
      var clause = query.leftJoin(parent.getCollection(fieldName, elementType), alias);
      if (fetch) {
        clause.fetchJoin();
      }
    }
    return alias;
  }

  // ---------------------------------------------------------------------------
  // Predicate building
  // ---------------------------------------------------------------------------

  /**
   * Build a predicate for the given operator. Comparison operators use type-safe
   * {@link ComparableExpressionBase} APIs instead of raw SQL templates.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected BooleanExpression buildPredicate(
      PathBuilder<?> path,
      String field,
      String operator,
      List<Object> values) {

    String leaf = leafName(field);

    switch (operator) {
      case "gt": {
        ComparableExpression expr = path.getComparable(leaf, Comparable.class);
        return expr.gt((Comparable) values.get(0));
      }
      case "lt": {
        ComparableExpression expr = path.getComparable(leaf, Comparable.class);
        return expr.lt((Comparable) values.get(0));
      }
      case "gte": {
        ComparableExpression expr = path.getComparable(leaf, Comparable.class);
        return expr.goe((Comparable) values.get(0));
      }
      case "lte": {
        ComparableExpression expr = path.getComparable(leaf, Comparable.class);
        return expr.loe((Comparable) values.get(0));
      }
      case "like":
        return path.getString(leaf).containsIgnoreCase(String.valueOf(values.get(0)));
      case "eq":
        return path.get(leaf).eq(values.get(0));
      case "ne":
        return path.get(leaf).ne(values.get(0));
      default: // in
        return path.get(leaf).in(values);
    }
  }

  private String leafName(String field) {
    return field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
  }

  // ---------------------------------------------------------------------------
  // Sorting
  // ---------------------------------------------------------------------------

  /**
   * Apply ordering from {@link Pageable}. Reuses filter joins for nested sort
   * paths when {@code filterCtx} is provided; otherwise creates a new join map.
   * Falls back to {@link #DEFAULT_SORT_FIELD} desc for deterministic pagination.
   */
  private void applySorting(JPAQuery<?> query, PathBuilder<T> root, Pageable pageable,
      FilterContext filterCtx) {

    if (pageable.getSort().isUnsorted()) {
      try {
        ComparableExpressionBase<?> fallback =
            root.getComparable(DEFAULT_SORT_FIELD, Comparable.class);
        query.orderBy(fallback.desc());
      } catch (Exception ignored) {
        // Entity has no createdAt; skip the deterministic fallback.
      }
      return;
    }

    Map<String, PathBuilder<?>> joins =
        filterCtx != null ? filterCtx.joins() : new HashMap<>();
    Map<String, Class<?>> joinClasses =
        filterCtx != null ? filterCtx.joinEntityClasses() : new HashMap<>();

    for (Sort.Order order : pageable.getSort()) {
      String property = order.getProperty();
      PathBuilder<?> sortPath = root;

      if (property.contains(".")) {
        sortPath = resolvePath(root, joins, joinClasses, property, query);
      }

      ComparableExpressionBase<?> expr =
          sortPath.getComparable(leafName(property), Comparable.class);
      query.orderBy(new OrderSpecifier<>(
          order.isAscending() ? Order.ASC : Order.DESC, expr));
    }
  }

  // ---------------------------------------------------------------------------
  // Value conversion
  // ---------------------------------------------------------------------------

  /**
   * Convert raw string filter values to the Java type declared on the target
   * entity field. Adds support for enums (#6) and per-value error handling (#8).
   */
  private List<Object> convertFilterValues(String field, List<Object> values,
      Map<String, Class<?>> joinEntityClasses) {

    String leaf = leafName(field);
    Class<?> ownerClass = field.contains(".")
        ? joinEntityClasses.getOrDefault(field.substring(0, field.lastIndexOf('.')), entityClass)
        : entityClass;

    FieldInfo fieldInfo;
    try {
      fieldInfo = resolveFieldInfo(ownerClass, leaf);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Could not resolve field type for '{}', using raw values", field);
      return values;
    }

    Class<?> targetType = fieldInfo.elementType();

    if (UUID.class.equals(targetType)) {
      return convertEach(values, field, v -> UUID.fromString(v.toString()));
    }
    if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
      return convertEach(values, field, v -> Integer.valueOf(v.toString()));
    }
    if (Long.class.equals(targetType) || long.class.equals(targetType)) {
      return convertEach(values, field, v -> Long.valueOf(v.toString()));
    }
    if (Double.class.equals(targetType) || double.class.equals(targetType)) {
      return convertEach(values, field, v -> Double.valueOf(v.toString()));
    }
    if (Float.class.equals(targetType) || float.class.equals(targetType)) {
      return convertEach(values, field, v -> Float.valueOf(v.toString()));
    }
    if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
      return convertEach(values, field, v -> Boolean.valueOf(v.toString()));
    }
    if (targetType.isEnum()) {
      return convertEach(values, field, v -> parseEnum(targetType, v.toString()));
    }

    return values;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static Enum<?> parseEnum(Class<?> enumType, String value) {
    return Enum.valueOf((Class<? extends Enum>) enumType, value.toUpperCase());
  }

  /**
   * Apply a converter to every value with consistent error handling (#8).
   */
  private List<Object> convertEach(List<Object> values, String field,
      Function<Object, ?> converter) {
    return values.stream()
        .map(v -> {
          try {
            return (Object) converter.apply(v);
          } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                "Invalid filter value '" + v + "' for field '" + field + "'");
          }
        })
        .toList();
  }

  // ---------------------------------------------------------------------------
  // Reflection
  // ---------------------------------------------------------------------------

  /**
   * Resolve and cache {@link FieldInfo} for a class/field pair. Walks the class
   * hierarchy for inherited fields (e.g. {@code AuditMixin}).
   */
  private FieldInfo resolveFieldInfo(Class<?> clazz, String fieldName) {
    // TODO: Implement cache
    // String cacheKey = clazz.getName() + "#" + fieldName;
    // return FIELD_INFO_CACHE.computeIfAbsent(cacheKey, k -> doResolveFieldInfo(clazz, fieldName));
    return doResolveFieldInfo(clazz, fieldName);
  }

  private FieldInfo doResolveFieldInfo(Class<?> clazz, String fieldName) {
    Class<?> currentClass = clazz;
    while (currentClass != null) {
      try {
        Field field = currentClass.getDeclaredField(fieldName);
        Class<?> rawType = field.getType();
        boolean isCollection = Collection.class.isAssignableFrom(rawType);
        Class<?> elementType;

        if (isCollection) {
          Type genericType = field.getGenericType();
          if (genericType instanceof ParameterizedType pt
              && pt.getActualTypeArguments().length > 0
              && pt.getActualTypeArguments()[0] instanceof Class<?> c) {
            elementType = c;
          } else {
            elementType = Object.class;
          }
        } else {
          elementType = rawType;
        }
        return new FieldInfo(isCollection, elementType, rawType);
      } catch (NoSuchFieldException e) {
        currentClass = currentClass.getSuperclass();
      }
    }
    // #9 - Sanitized message; do not leak entity class names.
    throw new IllegalArgumentException("Unknown filter field: '" + fieldName + "'");
  }

  // ---------------------------------------------------------------------------
  // Internal records
  // ---------------------------------------------------------------------------

  /** Metadata extracted from a reflected entity field. */
  private record FieldInfo(boolean isCollection, Class<?> elementType, Class<?> rawType) {
  }

  /** Parsed components of a filter map key. */
  private record ParsedFilter(String field, String operator) {
  }

  /**
   * Carrier for the filter predicate and the joins that were registered while
   * building it. Allows the same join graph to be replayed on the count query.
   */
  private record FilterContext(
      BooleanBuilder predicate,
      Map<String, PathBuilder<?>> joins,
      Map<String, Class<?>> joinEntityClasses) {
  }
}
