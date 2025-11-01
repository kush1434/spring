package com.open.spring.mvc.stats;

import lombok.Data;

// This class is a Data Transfer Object (DTO)
// It will be used as the @RequestBody for update requests.
@Data
public class StatsUpdateDto {
    // The name of the column to update (e.g., "frontend", "backend")
    private String column;
    // The new value to set for that column
    private double value;
}