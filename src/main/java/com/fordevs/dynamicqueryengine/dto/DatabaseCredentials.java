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
    private String databaseType;
    private String host;
    private int port; //use `int` when a value cannot be `null`
    private String databaseName;
    private String userName;
    private String password;
}
