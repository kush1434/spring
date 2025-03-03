package com.nighthawk.spring_portfolio.mvc.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.*;

@Entity
@Data  // Annotations to simplify writing code (ie constructors, setters)
@NoArgsConstructor
@AllArgsConstructor
public class SagaiMessage implements Comparable<SagaiMessage> {

    
    private static final Logger logger = LoggerFactory.getLogger(SagaiMessage.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String content;

    
    @Column(unique=false)
    private String subject;


    @JsonManagedReference
    @OneToMany(mappedBy = "message", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SagaiComment> comments = new ArrayList<>();

    // Additional constructor if you want to set content only
    public SagaiMessage(String content) {
        this.content = content;
        this.comments = new ArrayList<>(); // Ensure comments is initialized
    }

    /** Custom getter to return number of comments 
    */
    public int getNumberOfComment() {
        if (this.comments != null) {
            return comments.size();
        }
        return 0;
    }

    @Override
    public int compareTo(SagaiMessage other){
        String prompt = createPrompt(this.content, other.content);
        return callGroqAPI(prompt);
       //return this.content.compareTo(other.content);
    }
    
    private String createPrompt(String question1, String question2) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare if these two questions are similar. Question 1:").append(question1).append("?.");
        prompt.append("Question 2:").append(question2).append("?.");
        prompt.append(" Answer in one word as TRUE or FALSE.");
        return prompt.toString();
    }
    
      private int callGroqAPI(String prompt) {
        logger.warn("prompt= {}", prompt);
        String GROQ_API_KEY = "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"; 
        String API_URL = "https://api.groq.com/openai/v1/chat/completions"; 
        RestTemplate restTemplate = new RestTemplate();
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + GROQ_API_KEY);
        
        // Prepare request body
        String requestBody = String.format("{\"model\": \"llama3-8b-8192\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}", prompt);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        
        // Call the API
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                String result =  jsonNode.get("choices").get(0).get("message").get("content").asText();
                logger.warn("result is {}", result);
                if("TRUE".equals(result)){
                    logger.info("result is true");
                    return 1;
                }
            } else {
                logger.error("Error calling Groq API: {}", response.getStatusCode());
                logger.info("result is false");
                return 0;
            }
        } catch (Exception e) {
            logger.error("Exception while calling Groq API: {}", e.getMessage());
            logger.info("result is false");
            return 0;
        }
        logger.info("result is false");
        return 0;
    }


     /** 
      * This method is an static message to create a Default message by message, comment and subject
     * @param message the message content
     * @param comment the comment content
     * @param subject a subject like other, primitiveType
     * @return the SagaiMessage object;
     */
    public static SagaiMessage createMessage(String msg, String comnt, String subject) {
        SagaiMessage message = new SagaiMessage();
        message.setContent(msg);
        message.setSubject(subject);
        if(comnt!=null) {
            List<SagaiComment> comments = new ArrayList<>();
            SagaiComment comment = new SagaiComment();
            comment.setContent(comnt);
            comments.add(comment);
            message.setComments(comments);
        }
        return message;
    }

    
       
    /** Static method to initialize an array list of Message objects 
     * @return Message[], an array of Message objects
     */
    public static SagaiMessage[] init() {
        ArrayList<SagaiMessage> messages = new ArrayList<>();
        messages.add(createMessage("What is an object in java", "An object is an instance of a class with properties and methods.", "other"));
        messages.add(createMessage("What is a Primitive Type?", 
        "A primitive type is a fundamental data type built into the programming language, used to store simple values. " +
        "Java has eight primitive types: byte, short, int, long, float, double, char, and boolean. " +
        "These types are not objects and are stored directly in memory for efficiency. Unlike objects, primitive types do not have methods.", 
        "primitiveType"));
        messages.add(createMessage("What are Objects?", 
        "Objects are instances of classes in object-oriented programming. They encapsulate both state (fields/attributes) " +
        "and behavior (methods) and are created based on class definitions. Objects allow for code reusability, " +
        "modularity, and abstraction, enabling developers to structure complex systems efficiently.", 
        "objects"));
        messages.add(createMessage("What are Boolean Expressions and if Statements?", 
        "Boolean expressions evaluate to either true or false and are commonly used for decision-making in programming. " +
        "An if statement checks a boolean condition and executes a block of code if the condition is true. " +
        "Optional else and else-if statements allow for multiple conditional branches, helping control program flow dynamically.", 
        "booleanAndIf"));
        messages.add(createMessage("What is Iteration?", 
        "Iteration refers to repeating a block of code multiple times using loops. Java provides several loop structures: " +
        "for, while, and do-while loops. These allow developers to execute repetitive tasks efficiently, such as processing " +
        "arrays, managing user input, or running algorithms that require multiple passes.", 
        "iteration"));
        messages.add(createMessage("What are Classes?", 
        "Classes serve as blueprints for creating objects in Java. They define attributes (variables) and behaviors (methods) " +
        "that objects instantiated from the class will possess. Classes support encapsulation, inheritance, and polymorphism, " +
        "which are key principles of object-oriented programming. By using classes, developers can model real-world entities " +
        "and create structured, reusable code.", 
        "classes"));
        messages.add(createMessage("What is an Array?", 
        "An array is a fixed-size collection of elements of the same data type, stored in contiguous memory locations. " +
        "Arrays allow efficient access and manipulation of data using indexed positions. Java arrays are zero-based, meaning " +
        "the first element is accessed at index 0. Though efficient, their fixed size can be a limitation, requiring developers " +
        "to determine the required size beforehand.", 
        "array"));
        messages.add(createMessage("What is an ArrayList?", 
        "An ArrayList is a dynamic data structure in Java that allows resizing as elements are added or removed. " +
        "Unlike arrays, ArrayLists do not have a fixed size, making them more flexible. They are part of the Java Collections Framework " +
        "and provide built-in methods for adding, removing, and searching elements, improving ease of use compared to traditional arrays.", 
        "arrayList"));
        messages.add(createMessage("What is a 2D Array?", 
        "A 2D array is an array of arrays, often used to represent grids, matrices, or tables in programming. " +
        "Each element in a 2D array is accessed using two indices: one for the row and one for the column. " +
        "2D arrays are useful for applications such as image processing, game development, and data manipulation in scientific computing.", 
        "2DArray"));
        messages.add(createMessage("What is Inheritance?", 
        "Inheritance is a fundamental concept of object-oriented programming that allows one class (the subclass) to inherit fields " +
        "and methods from another class (the superclass). This promotes code reuse and hierarchy structuring. " +
        "Java supports single inheritance, meaning a class can extend only one other class, but it can implement multiple interfaces " +
        "to achieve similar functionality.", 
        "inheritance"));
        messages.add(createMessage("What is Recursion?", 
        "Recursion is a programming technique where a method calls itself to solve smaller instances of a problem. " +
        "Each recursive call reduces the problem size until a base case is reached, preventing infinite recursion. " +
        "Recursion is commonly used in algorithms like factorial computation, Fibonacci sequence generation, and tree traversal. " +
        "However, improper use can lead to stack overflow errors due to excessive function calls.", 
        "recursion"));
        return messages.toArray(new SagaiMessage[0]);
    }

      /** Static method to print Message objects from an array
     * @param args, not used
     */
    public static void main(String[] args) {
        // obtain Message from initializer
        SagaiMessage messages[] = init();

        // iterate using "enhanced for loop"
        for( SagaiMessage message : messages) {
            System.out.println(message);  // print object
        }
    }
}
