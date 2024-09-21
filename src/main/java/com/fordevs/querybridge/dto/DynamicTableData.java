package com.fordevs.querybridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO class to hold dynamic table data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicTableData {
    private String tableName;
    private List<Map<String, Object>> rows;
    private int totalRows;
    private List<Map<String, Object>> columns;
    private String credentialsKeys;
    private String credentials;
    private int currentPage;
    private int pageSize;
}
