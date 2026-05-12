package com.kms.tripplanning.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class FilterBuilderHelperTest {

    private FilterBuilderHelper helper;

    @BeforeEach
    void setUp() {
        helper = new FilterBuilderHelper();
    }

    @Test
    void addFilter_withVarargs_shouldAddFilter() {
        helper.addFilter("name", "Beach", "Mountain");
        Map<String, List<Object>> filters = helper.build();

        assertThat(filters).containsKey("name");
        assertThat(filters.get("name")).containsExactly("Beach", "Mountain");
    }

    @Test
    void addFilter_withList_shouldAddFilter() {
        helper.addFilter("category", List.of("1", "2"));
        Map<String, List<Object>> filters = helper.build();

        assertThat(filters).containsKey("category");
        assertThat(filters.get("category")).containsExactly("1", "2");
    }

    @Test
    void removeFilter_shouldRemoveFilter() {
        helper.addFilter("name", "Beach");
        helper.removeFilter("name");
        
        Map<String, List<Object>> filters = helper.build();
        assertThat(filters).isEmpty();
    }

    @Test
    void buildSort_shouldReturnUnsorted_whenSortByIsNull() {
        Sort sort = FilterBuilderHelper.buildSort(null, "asc");
        assertThat(sort.isSorted()).isFalse();
    }

    @Test
    void buildSort_shouldReturnAscendingSort_whenDirectionIsAsc() {
        Sort sort = FilterBuilderHelper.buildSort("name", "asc");
        
        assertThat(sort.isSorted()).isTrue();
        assertThat(sort.getOrderFor("name").isAscending()).isTrue();
    }

    @Test
    void buildSort_shouldReturnDescendingSort_whenDirectionIsDesc() {
        Sort sort = FilterBuilderHelper.buildSort("name", "desc");
        
        assertThat(sort.isSorted()).isTrue();
        assertThat(sort.getOrderFor("name").isAscending()).isFalse();
    }
}
