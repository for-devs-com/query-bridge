package com.fordevs.dynamicqueryengine.config;

import com.fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import com.fordevs.dynamicqueryengine.service.DatabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Configuration
public class DynamicQueryEngineAutoConfiguration {

    @Bean
    public DatabaseService databaseService() {
        return new DatabaseService() {
            @Override
            public ResponseEntity<String> setDatabaseConnection(DatabaseCredentials databaseCredentials) {
                return null;
            }

            @Override
            public ResponseEntity<List<String>> listTables() {
                return null;
            }

            @Override
            public ResponseEntity<List<Map<String, Object>>> listColumns(String tableName) {
                return null;
            }

            @Override
            public ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size) {
                return null;
            }

            @Override
            public ResponseEntity<List<Map<String, Object>>> executeQuery(String query) {
                return null;
            }
        };
    }

    @Bean
    public DynamicDataSourceManager dynamicDataSourceManager() {
        return new DynamicDataSourceManager();
    }
}