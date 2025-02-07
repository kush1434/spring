package com.nighthawk.spring_portfolio.mvc.Slack;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "calendar_events")
public class CalendarEvent {
    // POJO
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String title;
    private String description;
    private String type; // Added new column for event type
    private String period; // New field for the period (e.g., Period 1, Period 3)

    // Default constructor
    public CalendarEvent() {
    }

    // Constructor with fields
    public CalendarEvent(LocalDate date, String title, String description, String type, String period) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.type = type;
        this.period = period; // Initialize period
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
