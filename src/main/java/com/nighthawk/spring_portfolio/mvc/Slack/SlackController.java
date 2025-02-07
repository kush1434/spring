package com.nighthawk.spring_portfolio.mvc.Slack;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SlackController {

    /* 
    My slack bot's API key :(
    I would never actually leak my api key if it were for something more serious like a paid service or
    something more linked to account security, but to save me and the rest of the class from having to
    paste the key in their .envs manually I put my key here publicly
    */
    private String slackToken = "xoxp-7892664186276-7887305704597-7924387129461-e2333e0f3c20a3ddb2ba833ec37f4e52";
    
    // Rest template for API handling
    @Autowired
    private CalendarEventController calendarEventController;
    
    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SlackMessageRepository messageRepository;

    // Constructor
    public SlackController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Deprecated feature sadly
    // Was used to get a list of every slack message
    @GetMapping("/slack/")
    public ResponseEntity<List<SlackMessage>> returnSlackData() {
        List<SlackMessage> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }

    // Deprecated feature sadly
    // Was used to link the user id of the message sender to their actual name
    @PostMapping("/slack/getUsername")
    public ResponseEntity<String> getUsername(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid user ID");
        }

        String url = "https://slack.com/api/users.info?user=" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + slackToken);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null || !(boolean) body.get("ok")) {
            return ResponseEntity.status(400).body("Failed to fetch user info");
        }

        Map<String, Object> user = (Map<String, Object>) body.get("user");
        String username = (String) ((Map<String, Object>) user.get("profile")).get("real_name");

        return ResponseEntity.ok(username);
    }

    // Main message receiver function
    @PostMapping("/slack/events")
    public ResponseEntity<String> handleSlackEvent(@RequestBody SlackEvent payload) {
        if (payload.getChallenge() != null) {
            return ResponseEntity.ok(payload.getChallenge());
        }
    
    
        try {
            SlackEvent.Event messageEvent = payload.getEvent();
            String eventType = messageEvent.getType();
    
    
            // Distinguishing messages from other events
            if ("message".equals(eventType)) {
                ObjectMapper objectMapper = new ObjectMapper();
                String messageContent = objectMapper.writeValueAsString(messageEvent);
    
                // Mapping the message's content to key-value pairs
                Map<String, String> messageData = objectMapper.readValue(messageContent, Map.class);
    
                // Saving message to DB
                messageService.saveMessage(messageContent);
                System.out.println("Message saved to database: " + messageContent);
    
                // Direct call to the CalendarEventController method
                calendarEventController.addEventsFromSlackMessage(messageData);
                System.out.println("Message processed by CalendarEventController");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
    
        return ResponseEntity.ok("OK");
    }


}