package com.kms.tripplanning.utils.Impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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

public class GenericFilterRepositoryImpl<
  T
> implements GenericFilterRepository<T> {

  private final JPAQueryFactory queryFactory;
  private final Class<T> entityClass;

  public GenericFilterRepositoryImpl(EntityManager em, Class<T> entityClass) {
    this.entityClass = entityClass;
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public Page<T> search(
    Map<String, List<Object>> filters,
    Pageable pageable,
    List<String> loadRelations
  ) {
    String alias = entityClass.getSimpleName().toLowerCase();

    PathBuilder<T> root = new PathBuilder<>(entityClass, alias);
    JPAQuery<UUID> query = queryFactory
      .select(root.get("id", UUID.class))
      .from(root);

    Map<String, PathBuilder<?>> joins = new HashMap<>();
    BooleanBuilder builder = new BooleanBuilder();

    // Build predicates
    for (Map.Entry<String, List<Object>> entry : filters.entrySet()) {
      String key = entry.getKey();
      List<Object> values = entry.getValue();

      if (values == null || values.isEmpty()) continue;

      String field = key;
      String operator = "in";

      // parse operator
      if (key.contains("__")) {
        String[] parts = key.split("__");
        field = parts[0];
        operator = parts[1];
      }

      PathBuilder<?> path = resolvePath(root, joins, field, query);

      builder.and(buildPredicate(path, field, operator, values));
    }

    query.where(builder).distinct();

    List<UUID> ids = query
      .offset(pageable.getOffset())
      .limit(pageable.getPageSize())
      .fetch();

    var contentQuery = queryFactory.selectFrom(root);

    // Apply joins if specified
    applyJoins(contentQuery, root, joins, loadRelations);

    // pagination
    List<T> content = contentQuery
      .where(root.get("id", UUID.class).in(ids))
      .distinct()
      .fetch();

    // count query (NO joins/fetch)
    long total = Optional.ofNullable(
      queryFactory.select(root.count()).from(root).where(builder).fetchOne()
    ).orElse(0L);

    return new PageImpl<>(content, pageable, total);
  }

  // Resolve nested path + dynamic joins
  private PathBuilder<?> resolvePath(
    PathBuilder<?> root,
    Map<String, PathBuilder<?>> joins,
    String field,
    JPAQuery<?> query
  ) {
    if (!field.contains(".")) return root;

    String[] parts = field.split("\\.");
    PathBuilder<?> current = root;

    StringBuilder joinKey = new StringBuilder();

    for (int i = 0; i < parts.length - 1; i++) {
      if (joinKey.length() > 0) joinKey.append(".");
      joinKey.append(parts[i]);

      String key = joinKey.toString();

      if (!joins.containsKey(key)) {
        PathBuilder<Object> join = current.get(parts[i], Object.class);

        query.leftJoin(join);
        // Store the join path for future reference to avoid duplicate joins and fetch joins will be applied later in applyJoins method
        joins.put(key, join);
      }

      current = joins.get(key);
    }

    return current;
  }

  // Dynamic predicate builder

  protected BooleanExpression buildPredicate(
    PathBuilder<?> path,
    String field,
    String operator,
    List<Object> values
  ) {
    String actualField = field.contains(".")
      ? field.substring(field.lastIndexOf('.') + 1)
      : field;

    switch (operator) {
      case "gt":
        return Expressions.booleanTemplate(
          "{0} > {1}",
          path.get(actualField),
          values.get(0)
        );
      case "lt":
        return Expressions.booleanTemplate(
          "{0} < {1}",
          path.get(actualField),
          values.get(0)
        );
      case "like":
        return path
          .getString(actualField)
          .containsIgnoreCase((String) values.get(0));
      case "eq":
        return Expressions.booleanTemplate(
          "{0} = {1}",
          path.get(actualField),
          values.get(0)
        );
      default: // IN
        return path.get(actualField).in(values);
    }
  }

  private void applyJoins(
    JPAQuery<?> query,
    PathBuilder<T> root,
    Map<String, PathBuilder<?>> joins,
    List<String> loadRelations
  ) {
    for (String relation : loadRelations) {
      String[] parts = relation.split("\\.");
      PathBuilder<?> current = root;

      StringBuilder joinKey = new StringBuilder();

      for (int i = 0; i < parts.length; i++) {
        if (joinKey.length() > 0) joinKey.append(".");
        joinKey.append(parts[i]);

        String key = joinKey.toString();

        if (!joins.containsKey(key)) {
          PathBuilder<Object> join = current.get(parts[i], Object.class);
          // Apply fetch join for loadRelations to avoid N+1 problem
          query.leftJoin(join).fetchJoin();
          joins.put(key, join);
        } else {
          // If join already exists but not fetch joined, apply fetch join
          query.leftJoin(joins.get(key)).fetchJoin();
        }

        current = joins.get(key);
      }
    }
  }

  public <S, T> Page<T> castDTO(Page<S> values, Function<S, T> mapper) {
    List<T> content = values.getContent()
            .stream()
            .map(mapper)
            .toList();

    return new PageImpl<>(content, values.getPageable(), values.getTotalElements());
}
}
