package com.fordevs.dynamicqueryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class to hold database credentials.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseCredentials {
    private String databaseManager;
    private String host;
    private Integer port;
    private String databaseName;
    private String userName;
    private String password;
}
