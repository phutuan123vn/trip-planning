package com.kms.tripplanning.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequest {

    private Map<String, List<String>> filters;

    private String sortBy;

    private String sortDirection;

    private int page;

    private int size;
}
