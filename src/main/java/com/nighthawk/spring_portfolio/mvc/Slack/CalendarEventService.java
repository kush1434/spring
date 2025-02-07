package com.nighthawk.spring_portfolio.mvc.Slack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private SlackService slackService; // Assuming you have a Slack service to send messages

    // Save a new event
    public CalendarEvent saveEvent(CalendarEvent event) {
        return calendarEventRepository.save(event);
    }

    // Get events by a specific date
    public List<CalendarEvent> getEventsByDate(LocalDate date) {
        return calendarEventRepository.findByDate(date);
    }

    // Update event by title
    public boolean updateEventByTitle(String title, String newTitle, String description) {
        CalendarEvent event = getEventByTitle(title);
        if (event != null) {
            try {
                // Attempt to send the Slack notification
                String oldEventDetails = "Old Event: " + event.getTitle() + " on " + event.getDate();
                String newEventDetails = "New Event: " + newTitle + " on " + event.getDate();
                slackService.sendMessage("Event Updated: " + oldEventDetails + " -> " + newEventDetails);
            } catch (Exception e) {
                // Log the Slack error and continue with the update
                System.err.println("Slack notification failed: " + e.getMessage());
            }

            // Perform the event update
            event.setTitle(newTitle);
            event.setDescription(description);
            calendarEventRepository.save(event);
            return true;
        }
        return false;
    }
    
    // Delete event by title
    public boolean deleteEventByTitle(String title) {
        CalendarEvent event = getEventByTitle(title);
        if (event != null) {

            // Perform the delete
            calendarEventRepository.delete(event);
            return true;
        }
        return false;
    }

    // Get events within a date range
    public List<CalendarEvent> getEventsWithinDateRange(LocalDate startDate, LocalDate endDate) {
        return calendarEventRepository.findByDateBetween(startDate, endDate);
    }

    // Retrieve all events
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    // Get event by title
    public CalendarEvent getEventByTitle(String title) {
        return calendarEventRepository.findByTitle(title).orElse(null);
    }

    // Parse Slack message and create events
    public void parseSlackMessage(Map<String, String> jsonMap, LocalDate weekStartDate) {
        String text = jsonMap.get("text");
        List<CalendarEvent> events = extractEventsFromText(text, weekStartDate);
        for (CalendarEvent event : events) {
            saveEvent(event); // Save each parsed event
        }
    }

    // Extract events and calculate date for each day of the week
    private List<CalendarEvent> extractEventsFromText(String text, LocalDate weekStartDate) {
        List<CalendarEvent> events = new ArrayList<>();
        Pattern dayPattern = Pattern.compile("\\[(Mon|Tue|Wed|Thu|Fri|Sat|Sun)(?: - (Mon|Tue|Wed|Thu|Fri|Sat|Sun))?\\]:\\s*(\\*\\*|\\*)?\\s*(.+)");
        Pattern descriptionPattern = Pattern.compile("(\\*\\*|\\*)?\\s*\\u2022\\s*(.+)");
    
        boolean hasPeriod1 = text.toLowerCase().contains("period 1");
        boolean hasPeriod3 = text.toLowerCase().contains("period 3");
    
        String[] lines = text.split("\\n");
        CalendarEvent lastEvent = null;
        
        for (String line : lines) {
            Matcher dayMatcher = dayPattern.matcher(line);
    
            if (dayMatcher.find()) {
                String startDay = dayMatcher.group(1);
                String endDay = dayMatcher.group(2) != null ? dayMatcher.group(2) : startDay;
                String asterisks = dayMatcher.group(3);
                String currentTitle = dayMatcher.group(4).trim();
                String period;
                // Append period info if found anywhere in the text
                if (hasPeriod1) {
                    period = "1";
                } 
                else if (hasPeriod3) {
                    period = "3";
                }
                else {period = "0";}
                
    
                String type = "daily plan";
                if ("*".equals(asterisks)) {
                    type = "check-in";
                } else if ("**".equals(asterisks)) {
                    type = "grade";
                }
    
                for (LocalDate date : getDatesInRange(startDay, endDay, weekStartDate)) {
                    lastEvent = new CalendarEvent(date, currentTitle, "", type, period);
                    events.add(lastEvent);
                }
            } else {
                Matcher descMatcher = descriptionPattern.matcher(line);
                if (descMatcher.find() && lastEvent != null) {
                    String description = descMatcher.group(2).trim();
                    String asterisks = descMatcher.group(1);
    
                    String type = lastEvent.getType();
                    if ("*".equals(asterisks)) {
                        type = "check-in";
                    } else if ("**".equals(asterisks)) {
                        type = "grade";
                    }
    
                    lastEvent.setDescription(lastEvent.getDescription() + (lastEvent.getDescription().isEmpty() ? "" : ", ") + description);
                    lastEvent.setType(type);
                }
            }
        }
        return events;
    }

    // Helper to generate dates in the range [startDay - endDay]
    private List<LocalDate> getDatesInRange(String startDay, String endDay, LocalDate weekStartDate) {
        List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        int startIndex = days.indexOf(startDay);
        int endIndex = days.indexOf(endDay);

        List<LocalDate> dateRange = new ArrayList<>();
        if (startIndex != -1 && endIndex != -1) {
            for (int i = startIndex; i <= endIndex; i++) {
                dateRange.add(weekStartDate.plusDays(i - weekStartDate.getDayOfWeek().getValue() + 1));
            }
        }
        return dateRange;
    }
}


