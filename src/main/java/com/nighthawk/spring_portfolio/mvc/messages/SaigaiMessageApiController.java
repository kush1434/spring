package com.nighthawk.spring_portfolio.mvc.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;



import java.util.ArrayList;
import java.util.List;

@RestController // annotation to simplify the creation of RESTful web services
@RequestMapping("/api/saigai/messages")  // all requests in file begin with this URI
@CrossOrigin(origins = {"http://127.0.0.1:4100","https://nighthawkcoders.github.io/portfolio_2025/"}, allowCredentials = "true")
public class SaigaiMessageApiController {
     private static final Logger logger = LoggerFactory.getLogger(SaigaiMessageApiController.class);
    // Autowired enables Control to connect URI request and POJO Object to easily for Database CRUD operations
    @Autowired
    private SaigaiMessageJpaRepository messageRepository;

    /* GET List of Jokes
     * @GetMapping annotation is used for mapping HTTP GET requests onto specific handler methods.
     */
    @GetMapping("/")
    public ResponseEntity<List<SaigaiMessage>> getMessages() {
        // ResponseEntity returns List of Jokes provide by JPA findAll()
        return new ResponseEntity<>( messageRepository.findAll(), HttpStatus.OK);
    }

    /* Update Like
     * @PutMapping annotation is used for mapping HTTP PUT requests onto specific handler methods.
     * @PathVariable annotation extracts the templated part {id}, from the URI
     */
    @PostMapping("/saigai/message")
    public ResponseEntity<SaigaiMessage>  createMessage(@RequestBody SaigaiMessage message) {

         if (message.getComments() == null) {
            message.setComments(new ArrayList<>());
        }

       
        for (SaigaiMessage oldmessage :  messageRepository.findAll()) {
            if(oldmessage.compareTo(message)== 1){
                return ResponseEntity.status(409).body(oldmessage);
            }
        }
        SaigaiMessage savedMessage = messageRepository.save(message);
        return ResponseEntity.status(201).body(savedMessage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        if (messageRepository.existsById(id)) {
            logger.info("Comment exists, attempting to delete...");
            messageRepository.deleteById(id);
            logger.info("Comment deleted successfully.");
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaigaiMessage> getMessageById(@PathVariable Long id) {
        return messageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
