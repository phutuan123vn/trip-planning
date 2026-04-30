package com.kms.tripplanning.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBuilderHelper {

    private Map<String, List<Object>> filters;

    public FilterBuilderHelper() {
        this.filters = new HashMap<>();
    }

    public void addFilter(String key, Object... values) {
        if (values != null && values.length > 0) {
            filters.put(key, List.of(values));
        }
    }

    public void removeFilter(String key) {
        filters.remove(key);
    }

    public Map<String, List<Object>> build() {
        return filters;
    }
}
