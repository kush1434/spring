package com.open.spring.mvc.slack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class MessageService {

    @Autowired
    private SlackMessageRepository messageRepository;
    public void saveMessage(String messageContent) {
        // Create a new SlackMessage entity with the current timestamp and the message content as a blob
        SlackMessage message = new SlackMessage(LocalDateTime.now(), messageContent);
        // Save to the database
        messageRepository.save(message);
    }
}
