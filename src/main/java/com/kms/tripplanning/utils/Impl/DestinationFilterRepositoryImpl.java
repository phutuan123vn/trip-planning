package com.kms.tripplanning.utils.Impl;

import java.util.List;

import com.kms.tripplanning.entity.Destination;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;

import jakarta.persistence.EntityManager;

public class DestinationFilterRepositoryImpl extends GenericFilterRepositoryImpl<Destination> {
    public DestinationFilterRepositoryImpl(EntityManager em) {
        super(em, Destination.class);
    }

    @Override
    protected BooleanExpression buildPredicate(PathBuilder<?> path,
            String field,
            String operator,
            List<Object> values) {

        if ("coordinates".equals(field) && operator.equals("in")) {
            return buildCoordinatesPredicate(path, values);
        }

        if ("coordinates".equals(field) && operator.equals("near")) {
            return buildCoordinatesNearPredicate(path, values);
        }

        return super.buildPredicate(path, field, operator, values);
    }

    private BooleanExpression buildCoordinatesPredicate(PathBuilder<?> path, List<Object> values) {
        if (values.size() != 2) {
            throw new IllegalArgumentException("Coordinates filter requires exactly 2 values: [latitude, longitude]");
        }

        try {
            double latitude = Double.parseDouble(values.get(0).toString());
            double longitude = Double.parseDouble(values.get(1).toString());

            // Assuming the entity has 'latitude' and 'longtitude' fields
            return path.getNumber("latitude", Double.class).eq(latitude)
                    .and(path.getNumber("longtitude", Double.class).eq(longitude));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Coordinates values must be valid numbers: [latitude, longitude]", e);
        }

    }

    private BooleanExpression buildCoordinatesNearPredicate(PathBuilder<?> path, List<Object> values) {
        if (values.size() != 3) {
            throw new IllegalArgumentException(
                    "Coordinates near filter requires exactly 3 values: [latitude, longitude, radiusInKm]");
        }

        try {
            double latitude = Double.parseDouble(values.get(0).toString());
            double longitude = Double.parseDouble(values.get(1).toString());
            double radiusInKm = Double.parseDouble(values.get(2).toString());

            NumberPath<Double> latField = path.getNumber("latitude", Double.class);
            NumberPath<Double> lonField = path.getNumber("longtitude", Double.class);

            BooleanExpression boundingBox = buildBoundingBox(latField, lonField, latitude, longitude, radiusInKm);

            NumberExpression<Double> distanceExpr = distanceExpression(latField, lonField, latitude, longitude);

            return boundingBox.and(distanceExpr.loe(radiusInKm));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Coordinates near values must be valid numbers: [latitude, longitude, radiusInKm]", e);
        }
    }

    private BooleanExpression buildBoundingBox(NumberPath<Double> latField,
            NumberPath<Double> lonField,
            double latitude,
            double longitude,
            double radiusInKm) {

        // ~111km per degree latitude
        double latDelta = radiusInKm / 111.0;

        // longitude shrink near poles
        double lonDelta = radiusInKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        return latField.between(latitude - latDelta, latitude + latDelta)
                .and(lonField.between(longitude - lonDelta, longitude + lonDelta));
    }

    private NumberExpression<Double> distanceExpression(NumberExpression<Double> latField,
            NumberExpression<Double> lonField,
            double latitude,
            double longitude) {

        return Expressions.numberTemplate(Double.class,
                "6371 * acos(" +
                        "cos(radians({0})) * cos(radians({1})) * " +
                        "cos(radians({2}) - radians({3})) + " +
                        "sin(radians({0})) * sin(radians({1}))" +
                        ")",
                latitude, // {0}
                latField, // {1}
                lonField, // {2}
                longitude // {3}
        );
    }

}
