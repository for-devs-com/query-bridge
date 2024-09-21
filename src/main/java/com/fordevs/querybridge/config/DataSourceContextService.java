package com.fordevs.querybridge.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class to manage the current JdbcTemplate context using ThreadLocal.
 * This allows each thread to have its own JdbcTemplate instance.
 */
@Service
public class DataSourceContextService {

    // ThreadLocal to store the current JdbcTemplate instance for each thread
    private static final ThreadLocal<JdbcTemplate> currentTemplate = new ThreadLocal<>();

    /**
     * Sets the current JdbcTemplate for the current thread.
     *
     * @param jdbcTemplate the JdbcTemplate to set
     */
    public static void setCurrentTemplate(JdbcTemplate jdbcTemplate) {
        currentTemplate.set(jdbcTemplate);
    }

    /**
     * Gets the current JdbcTemplate for the current thread.
     *
     * @return the current JdbcTemplate, or null if not set
     */
    public static JdbcTemplate getCurrentTemplate() {
        return currentTemplate.get();
    }

    /**
     * Clears the current JdbcTemplate for the current thread.
     */
    public static void clear() {
        currentTemplate.remove();
    }
}
