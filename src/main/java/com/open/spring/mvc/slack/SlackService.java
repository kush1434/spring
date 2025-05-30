package com.open.spring.mvc.slack;

import java.time.*;
import java.util.regex.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SlackService {

    // Slack Incoming Webhook URL (replace this with your actual webhook URL)
    //private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";

    private LocalDate getWeekStartDateFromMessage(String message) {
        LocalDate today = LocalDate.now();
        Pattern fullDatePattern = Pattern.compile("Week of (\\d{1,2})/(\\d{1,2})/(\\d{4})");
        Pattern monthDayPattern = Pattern.compile("Week of (\\d{1,2})/(\\d{1,2})");
        Pattern ordinalPattern = Pattern.compile("Week of the (\\d{1,2})(st|nd|rd|th)");

        Matcher m = fullDatePattern.matcher(message);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3));
            return LocalDate.of(year, month, day);
        }
        m = monthDayPattern.matcher(message);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            int year = today.getYear();
            return LocalDate.of(year, month, day);
        }
        m = ordinalPattern.matcher(message);
        if (m.find()) {
            int day = Integer.parseInt(m.group(1));
            int month = today.getMonthValue();
            int year = today.getYear();
            return LocalDate.of(year, month, day);
        }
        // Default: use current week's Monday
        return today.with(DayOfWeek.SUNDAY);
    }
}
