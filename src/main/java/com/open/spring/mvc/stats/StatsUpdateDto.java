package com.open.spring.mvc.stats;

import lombok.Data;

@Data
public class StatsUpdateDto {
    // We add username here, so it's part of the JSON body
    private String username;
    
    // The name of the column to update (e.g., "frontend", "backend")
    private String column;
    
    // The new value to set for that column
    private double value;
}