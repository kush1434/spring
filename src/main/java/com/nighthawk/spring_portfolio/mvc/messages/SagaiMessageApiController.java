package com.nighthawk.spring_portfolio.mvc.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;



import java.util.ArrayList;
import java.util.List;

@RestController // annotation to simplify the creation of RESTful web services
@RequestMapping("/api/sagai/messages")  // all requests in file begin with this URI
@CrossOrigin(origins = {"http://127.0.0.1:4100","https://nighthawkcoders.github.io/portfolio_2025/"}, allowCredentials = "true")
public class SagaiMessageApiController {
     private static final Logger logger = LoggerFactory.getLogger(SagaiMessageApiController.class);
    // Autowired enables Control to connect URI request and POJO Object to easily for Database CRUD operations
    @Autowired
    private SagaiMessageJpaRepository messageRepository;

    @Autowired
    private SagaiCommentJpaRepository commentRepository;


    /* GET List of Jokes
     * @GetMapping annotation is used for mapping HTTP GET requests onto specific handler methods.
     */
    @GetMapping("/")
    public ResponseEntity<List<SagaiMessage>> getMessages() {
        // ResponseEntity returns List of Jokes provide by JPA findAll()

        //create default messages 
        createInitMessages();
        return new ResponseEntity<>( messageRepository.findAll(), HttpStatus.OK);
    }

     /** 
      * This method is to create the default content for the college board Review.
      * This method will check if the database already has more then 10 message then skips.
      * This method will iterate through each message from Init() and save the message to the database.
      * This method will also itterate through each comment in the message and save  to the Database.
     */
    private void createInitMessages(){

        //check if the database already has more then 10 message then skips.
        List<SagaiMessage> dataMessages = messageRepository.findAll();
        System.out.println("messages size" +dataMessages.size());  // print object
        if(dataMessages.size() >=10){
            return;
        }
        //Get the list of messages we use for testing.
        SagaiMessage messages[] = SagaiMessage.init();
        //In case the message already exist then skip saving.
        try{
            for( SagaiMessage message : messages) {
                System.out.println(message);  // print object
                SagaiMessage msg = messageRepository.save(message); 
                List<SagaiComment> comments = message.getComments();
                for( SagaiComment comment : comments) {
                    comment.setMessage(msg);
                    commentRepository.save(comment);
                }

            }
        }catch(Exception e){
            System.out.println("message already saved.");  // print object
        }
          
    }

    /* Update Like
     * @PutMapping annotation is used for mapping HTTP PUT requests onto specific handler methods.
     * @PathVariable annotation extracts the templated part {id}, from the URI
     */
    @PostMapping("/sagai/message")
    public ResponseEntity<SagaiMessage>  createMessage(@RequestBody SagaiMessage message) {

         if (message.getComments() == null) {
            message.setComments(new ArrayList<>());
        }

       
        for (SagaiMessage oldmessage :  messageRepository.findAll()) {
            if(oldmessage.compareTo(message)== 1){
                return ResponseEntity.status(409).body(oldmessage);
            }
        }
        SagaiMessage savedMessage = messageRepository.save(message);
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
    public ResponseEntity<SagaiMessage> getMessageById(@PathVariable Long id) {
        return messageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
