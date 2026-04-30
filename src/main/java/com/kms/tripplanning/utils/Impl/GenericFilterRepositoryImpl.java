package com.kms.tripplanning.utils.Impl;

import com.kms.tripplanning.utils.GenericFilterRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.*;

import java.util.*;

public class GenericFilterRepositoryImpl<T> implements GenericFilterRepository<T> {

    private final JPAQueryFactory queryFactory;
    private final Class<T> entityClass;


    public GenericFilterRepositoryImpl(EntityManager em, Class<T> entityClass) {
        this.entityClass = entityClass;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<T> search(Map<String, List<Object>> filters, Pageable pageable) {

        String alias = entityClass.getSimpleName().toLowerCase();

        PathBuilder<T> root = new PathBuilder<>(entityClass, alias);
        JPAQuery<T> query = queryFactory.selectFrom(root);

        Map<String, PathBuilder<?>> joins = new HashMap<>();
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

            PathBuilder<?> path = resolvePath(root, joins, field, query);

            builder.and(buildPredicate(path, field, operator, values));
        }

        query.where(builder).distinct();

        // 🔹 pagination
        List<T> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 🔹 count query (NO joins/fetch)
        long total = Optional.ofNullable(
                queryFactory
                        .select(root.count())
                        .from(root)
                        .where(builder)
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    // Resolve nested path + dynamic joins
    private PathBuilder<?> resolvePath(PathBuilder<?> root,
            Map<String, PathBuilder<?>> joins,
            String field,
            JPAQuery<?> query) {

        if (!field.contains("."))
            return root;

        String[] parts = field.split("\\.");
        PathBuilder<?> current = root;

        StringBuilder joinKey = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {

            if (joinKey.length() > 0)
                joinKey.append(".");
            joinKey.append(parts[i]);

            String key = joinKey.toString();

            if (!joins.containsKey(key)) {
                PathBuilder<Object> join = current.get(parts[i], Object.class);

                query.leftJoin(join);
                joins.put(key, join);
            }

            current = joins.get(key);
        }

        return current;
    }

    // Dynamic predicate builder
    private BooleanExpression buildPredicate(PathBuilder<?> path,
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
                return path.getString(actualField)
                        .containsIgnoreCase((String) values.get(0));

            case "eq":
                return Expressions.booleanTemplate("{0} = {1}",
                        path.get(actualField), values.get(0));

            default: // IN
                return path.get(actualField).in(values);
        }
    }

    public <DTO> List<DTO> castList(List<Object> values, Class<DTO> clazz) {
        return values.stream()
                .map(clazz::cast)
                .toList();
    }
}