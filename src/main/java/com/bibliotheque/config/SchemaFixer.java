package com.bibliotheque.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaFixer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixSchema() {
        try {
            // Drop legacy columns that cause constraint violations
            String[] columnsToDrop = {"borrowed_at", "due_at", "returned_at", "updated_at"};
            
            for (String col : columnsToDrop) {
                try {
                    jdbcTemplate.execute("ALTER TABLE loans DROP COLUMN " + col);
                    System.out.println("SchemaFixer: Dropped column '" + col + "' from 'loans' table.");
                } catch (Exception e) {
                    // Ignore if column doesn't exist
                }
            }
        } catch (Exception e) {
            System.out.println("SchemaFixer: Error during schema fix: " + e.getMessage());
        }
    }
}
