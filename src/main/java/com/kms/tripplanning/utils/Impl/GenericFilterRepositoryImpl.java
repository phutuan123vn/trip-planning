package com.kms.tripplanning.utils.Impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kms.tripplanning.utils.GenericFilterRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class GenericFilterRepositoryImpl<T> implements GenericFilterRepository<T> {

  private final JPAQueryFactory queryFactory;
  private final Class<T> entityClass;
  private final Logger logger = LoggerFactory.getLogger(GenericFilterRepositoryImpl.class);

  public GenericFilterRepositoryImpl(EntityManager em, Class<T> entityClass) {
    this.entityClass = entityClass;
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public Page<T> search(
      Map<String, List<Object>> filters,
      Pageable pageable,
      List<String> loadRelations) {
    String alias = entityClass.getSimpleName().toLowerCase();

    PathBuilder<T> root = new PathBuilder<>(entityClass, alias);
    JPAQuery<UUID> query = queryFactory
        .select(root.get("id", UUID.class))
        .from(root);

    // Track the resolved entity class at each join level for reflection lookups
    Map<String, PathBuilder<?>> joins = new HashMap<>();
    Map<String, Class<?>> joinEntityClasses = new HashMap<>();
    BooleanBuilder builder = new BooleanBuilder();

    // Build predicates
    for (Map.Entry<String, List<Object>> entry : filters.entrySet()) {
      String key = entry.getKey();
      List<Object> values = entry.getValue();

      if (values == null || values.isEmpty())
        continue;

      String field = key;
      String operator = "in";

      // parse operator
      if (key.contains("__")) {
        String[] parts = key.split("__");
        field = parts[0];
        operator = parts[1];
      }

      PathBuilder<?> path = resolvePath(root, joins, joinEntityClasses, field, query);

      // Convert string filter values to the correct Java type for the target field
      List<Object> convertedValues = convertFilterValues(field, values, joinEntityClasses);

      builder.and(buildPredicate(path, field, operator, convertedValues));
    }

    query.where(builder).distinct();

    List<UUID> ids = query
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    var contentQuery = queryFactory.selectFrom(root);

    // Apply fetch joins with a fresh map to avoid stale references from the filter
    // query
    applyJoins(contentQuery, root, new HashMap<>(), new HashMap<>(), loadRelations);

    // pagination
    List<T> content = contentQuery
        .where(root.get("id", UUID.class).in(ids))
        .distinct()
        .fetch();

    // count query with its own joins for filters on nested paths
    JPAQuery<Long> countQuery = queryFactory
        .select(root.get("id").countDistinct())
        .from(root);

    long total = Optional.ofNullable(
        countQuery.where(builder).fetchOne()).orElse(0L);

    return new PageImpl<>(content, pageable, total);
  }

  /**
   * Resolve nested path with proper aliased joins for collection associations.
   * Hibernate 6+ requires explicit aliased joins when navigating through plural
   * (collection) paths.
   */
  private PathBuilder<?> resolvePath(
      PathBuilder<?> root,
      Map<String, PathBuilder<?>> joins,
      Map<String, Class<?>> joinEntityClasses,
      String field,
      JPAQuery<?> query) {
    if (!field.contains("."))
      return root;

    String[] parts = field.split("\\.");
    PathBuilder<?> current = root;
    Class<?> currentClass = entityClass;

    StringBuilder joinKey = new StringBuilder();

    for (int i = 0; i < parts.length - 1; i++) {
      if (joinKey.length() > 0)
        joinKey.append(".");
      joinKey.append(parts[i]);

      String key = joinKey.toString();

      if (!joins.containsKey(key)) {
        FieldInfo fieldInfo = resolveFieldInfo(currentClass, parts[i]);

        if (fieldInfo.isCollection) {
          // For collection fields, use aliased join: leftJoin(collection, alias)
          // This is required by Hibernate 6+ for plural path navigation
          String aliasName = key.replace(".", "_") + "_join";
          PathBuilder<?> alias = leftJoinCollection(query, current, parts[i], fieldInfo.elementType, aliasName, false);
          joins.put(key, alias);
        } else {
          // For singular associations, use simple path join
          PathBuilder<Object> join = current.get(parts[i], Object.class);
          query.leftJoin(join);
          joins.put(key, join);
        }
        joinEntityClasses.put(key, fieldInfo.elementType);
      }

      current = joins.get(key);
      currentClass = joinEntityClasses.get(key);
    }

    return current;
  }

  /**
   * Convert filter values from strings to the correct Java type based on the
   * target entity field type (e.g. String → UUID for UUID fields).
   */
  private List<Object> convertFilterValues(String field, List<Object> values,
      Map<String, Class<?>> joinEntityClasses) {
    // Determine the entity class that owns the leaf field
    String actualField;
    Class<?> ownerClass;
    if (field.contains(".")) {
      String parentPath = field.substring(0, field.lastIndexOf('.'));
      actualField = field.substring(field.lastIndexOf('.') + 1);
      ownerClass = joinEntityClasses.getOrDefault(parentPath, entityClass);
    } else {
      actualField = field;
      ownerClass = entityClass;
    }

    try {
      FieldInfo fieldInfo = resolveFieldInfo(ownerClass, actualField);
      Class<?> targetType = fieldInfo.elementType;

      if (UUID.class.equals(targetType)) {
        return values.stream()
            .map(v -> (Object) UUID.fromString(v.toString()))
            .toList();
      }
      if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
        return values.stream()
            .map(v -> (Object) Integer.valueOf(v.toString()))
            .toList();
      }
      if (Long.class.equals(targetType) || long.class.equals(targetType)) {
        return values.stream()
            .map(v -> (Object) Long.valueOf(v.toString()))
            .toList();
      }
      if (Double.class.equals(targetType) || double.class.equals(targetType)) {
        return values.stream()
            .map(v -> (Object) Double.valueOf(v.toString()))
            .toList();
      }
      if (Float.class.equals(targetType) || float.class.equals(targetType)) {
        return values.stream()
            .map(v -> (Object) Float.valueOf(v.toString()))
            .toList();
      }
      if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
        return values.stream()
            .map(v -> (Object) Boolean.valueOf(v.toString()))
            .toList();
      }
    } catch (IllegalArgumentException e) {
      logger.warn("Could not resolve field type for '{}', using raw values", field);
    }

    return values;
  }

  // Dynamic predicate builder

  protected BooleanExpression buildPredicate(
      PathBuilder<?> path,
      String field,
      String operator,
      List<Object> values) {
    String actualField = field.contains(".")
        ? field.substring(field.lastIndexOf('.') + 1)
        : field;

    switch (operator) {
      case "gt":
        return Expressions.booleanTemplate(
            "{0} > {1}",
            path.get(actualField),
            values.get(0));
      case "lt":
        return Expressions.booleanTemplate(
            "{0} < {1}",
            path.get(actualField),
            values.get(0));
      case "like":
        return path
            .getString(actualField)
            .containsIgnoreCase((String) values.get(0));
      case "eq":
        return Expressions.booleanTemplate(
            "{0} = {1}",
            path.get(actualField),
            values.get(0));
      default: // IN
        return path.get(actualField).in(values);
    }
  }

  /**
   * Apply fetch joins for eager loading of relations.
   * Uses aliased joins for collection associations (Hibernate 6+ requirement).
   */
  private void applyJoins(
      JPAQuery<?> query,
      PathBuilder<T> root,
      Map<String, PathBuilder<?>> joins,
      Map<String, Class<?>> joinEntityClasses,
      List<String> loadRelations) {
    for (String relation : loadRelations) {
      String[] parts = relation.split("\\.");
      PathBuilder<?> current = root;
      Class<?> currentClass = entityClass;

      StringBuilder joinKey = new StringBuilder();

      for (int i = 0; i < parts.length; i++) {
        if (joinKey.length() > 0)
          joinKey.append(".");
        joinKey.append(parts[i]);

        String key = joinKey.toString();

        if (!joins.containsKey(key)) {
          FieldInfo fieldInfo = resolveFieldInfo(currentClass, parts[i]);

          if (fieldInfo.isCollection) {
            // For collection fields, use aliased fetch join
            String aliasName = key.replace(".", "_") + "_fetch";
            PathBuilder<?> alias = leftJoinCollection(query, current, parts[i], fieldInfo.elementType, aliasName, true);
            joins.put(key, alias);
          } else {
            // For singular associations, use simple fetch join
            PathBuilder<Object> join = current.get(parts[i], Object.class);
            query.leftJoin(join).fetchJoin();
            joins.put(key, join);
          }
          joinEntityClasses.put(key, fieldInfo.elementType);
        }
        // Navigate to the joined path for the next level
        current = joins.get(key);
        currentClass = joinEntityClasses.get(key);
      }
    }
  }

  /**
   * Type-capturing helper to perform aliased left join on a Set collection.
   * Isolates the unchecked cast required when working with dynamic Class<?> types.
   */
  @SuppressWarnings("unchecked")
  private <E> PathBuilder<E> leftJoinCollection(JPAQuery<?> query, PathBuilder<?> parent,
      String fieldName, Class<?> elementType, String aliasName, boolean fetch) {
    Class<E> type = (Class<E>) elementType;
    PathBuilder<E> alias = new PathBuilder<>(type, aliasName);
    var joinClause = query.leftJoin(parent.getSet(fieldName, type), alias);
    if (fetch) {
      joinClause.fetchJoin();
    }
    return alias;
  }

  /**
   * Resolve field type information using reflection.
   * Determines if a field is a collection and extracts the element type.
   */
  private FieldInfo resolveFieldInfo(Class<?> clazz, String fieldName) {
    Class<?> currentClass = clazz;
    while (currentClass != null) {
      try {
        Field field = currentClass.getDeclaredField(fieldName);
        boolean isCollection = Collection.class.isAssignableFrom(field.getType());
        Class<?> elementType;

        if (isCollection) {
          Type genericType = field.getGenericType();
          if (genericType instanceof ParameterizedType pt) {
            Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> c) {
              elementType = c;
            } else {
              elementType = Object.class;
            }
          } else {
            elementType = Object.class;
          }
        } else {
          elementType = field.getType();
        }

        return new FieldInfo(isCollection, elementType);
      } catch (NoSuchFieldException e) {
        // Walk up the class hierarchy (e.g. for fields in AuditMixin superclass)
        currentClass = currentClass.getSuperclass();
      }
    }
    throw new IllegalArgumentException("Field '" + fieldName + "' not found in " + clazz.getName() + " or its superclasses");
  }

  /**
   * Holds metadata about an entity field for join resolution.
   */
  private record FieldInfo(boolean isCollection, Class<?> elementType) {
  }

  public <S, T> Page<T> castDTO(Page<S> values, Function<S, T> mapper) {
    List<T> content = values.getContent()
        .stream()
        .map(mapper)
        .toList();

    return new PageImpl<>(content, values.getPageable(), values.getTotalElements());
  }
}

