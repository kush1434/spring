// define package and import necessary libraries
package com.nighthawk.spring_portfolio.mvc.rpg.adventureAnswer;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.rpg.adventureQuestion.AdventureQuestion;
import com.nighthawk.spring_portfolio.mvc.rpg.adventureQuestion.AdventureQuestionJpaRepository;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// define rest controller and set base endpoint mapping
@RestController
@RequestMapping("/rpg_answer")
// enable cross-origin requests for the specified frontend
@CrossOrigin(origins = "http://127.0.0.1:5501") 
public class AdventureAnswerApiController {

    // load environment variables for api configuration
    private final Dotenv dotenv = Dotenv.load();
    private final String apiUrl = dotenv.get("GAMIFY_API_URL"); // store api url
    private final String apiKey = dotenv.get("GAMIFY_API_KEY"); // store api key
    @Autowired
    private PersonJpaRepository repository;
    // autowire jpa repositories for database interactions
    @Autowired
    private AdventureAnswerJpaRepository answerJpaRepository;
    @Autowired
    private PersonJpaRepository personJpaRepository;
    @Autowired
    private AdventureQuestionJpaRepository questionJpaRepository;

    // define dto class to encapsulate answer data for api requests
    @Getter 
    public static class AnswerDto {
        private String content; // store the answer content
        private Long questionId; // associate with a question
        private Long personId; // associate with a person
        private Long chatScore; // store chat score for the answer
    }

    // added endpoint to match python user to java user
    @GetMapping("/person/{uid}")
    public ResponseEntity<Person> getPersonByUid(@PathVariable String uid) {
        Person person = repository.findByUid(uid);
        if (person != null) { // Good ID
            return new ResponseEntity<>(person, HttpStatus.OK); // OK HTTP response: status code, headers, and body
        }
        // Bad ID
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // endpoint to get the number of questions answered by a specific person
    @GetMapping("/getQuestionsAnswered/{personid}")
    public ResponseEntity<Integer> getQuestionsAnswered(@PathVariable Integer personid) {
        // fetch all answers by the given person id
        List<AdventureAnswer> useranswers = answerJpaRepository.findByPersonId(personid);

        // count the total answers
        Integer questionsAnswered = useranswers.size();

        // return the count with an ok status
        return new ResponseEntity<>(questionsAnswered, HttpStatus.OK);
    }

    // endpoint to get a list of all questions
    @GetMapping("getQuestions")
    public ResponseEntity<List<AdventureQuestion>> getQuestions() {
        // fetch all questions ordered alphabetically by title
        List<AdventureQuestion> questions = questionJpaRepository.findAllByOrderByTitleAsc();

        // return the list of questions with an ok status
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    // endpoint to get a specific question by its id
    @GetMapping("getQuestion/{questionid}") 
    public ResponseEntity<AdventureQuestion> getQuestion(@PathVariable Integer questionid) {
        // fetch the question by its id
        AdventureQuestion question = questionJpaRepository.findById(questionid);

        // return the question with an ok status
        return new ResponseEntity<>(question, HttpStatus.OK);
    }

    // endpoint to get the total chat score for a specific person
    @GetMapping("/getChatScore/{personid}")
    public ResponseEntity<Long> getChatScore(@PathVariable Integer personid) {
        // fetch all answers by the person id
        List<AdventureAnswer> personsanswers = answerJpaRepository.findByPersonId(personid);
        Long totalChatScore = 0L; // initialize total chat score to zero

        // loop through each answer and sum the chat scores
        for (AdventureAnswer personanswer : personsanswers) {
            Long questionChatScore = personanswer.getChatScore();
            totalChatScore += questionChatScore;
        }

        // return the total chat score with an ok status
        return new ResponseEntity<>(totalChatScore, HttpStatus.OK);
    }

    // endpoint to get the balance of a specific person
    @GetMapping("/getBalance/{personid}") 
    public ResponseEntity<Double> getBalance(@PathVariable Long personid) {
        // fetch the person by their id
        Optional<Person> optional = personJpaRepository.findById(personid);
        Person personOpt = optional.get(); // get the person object from optional

        // retrieve the balance of the person
        Double balance = personOpt.getBalanceDouble();

        // return the balance with an ok status
        return new ResponseEntity<>(balance, HttpStatus.OK);
    }

    // endpoint to submit a new answer
    @PostMapping("/submitAnswer")
    public ResponseEntity<AdventureAnswer> postAnswer(@RequestBody AnswerDto answerDto) {
        // fetch the question and person associated with the answer
        Optional<AdventureQuestion> questionOpt = questionJpaRepository.findById(answerDto.getQuestionId());
        Optional<Person> personOpt = personJpaRepository.findById(answerDto.getPersonId());
        
        // log the api key for debugging
        System.out.println("API Key: " + apiKey);

        // if either the question or person is not found, return not found status
        if (questionOpt.isEmpty() || personOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // extract question and person objects
        AdventureQuestion question = questionOpt.get();
        Person person = personOpt.get();

        // log the question for debugging
        System.out.println(question);

        // rubric for grading the answer
        String rubric = "Correctness and Completeness (500 points): 500 - completely correct, "
                        + "450 - minor issues or unhandled edge cases, 400 - several small errors, "
                        + "350 - partial with multiple issues, below 300 - major issues/incomplete; "
                        + "Efficiency and Optimization (200 points): 200 - optimal or near-optimal, "
                        + "180 - minor optimization needed, 160 - functional but inefficient, "
                        + "140 - improvements needed, below 140 - inefficient; Code Structure and Organization "
                        + "(150 points): 150 - well-organized, 130 - mostly organized, 110 - readable but lacks structure, "
                        + "90 - hard to follow, below 90 - unorganized; Readability and Documentation (100 points): "
                        + "100 - clear, well-documented, 85 - readable but limited comments, 70 - somewhat readable, "
                        + "50 - minimally readable, below 50 - poor readability; Error Handling and Edge Cases "
                        + "(50 points): 50 - handles all cases, 40 - most cases covered, 30 - some cases covered, "
                        + "20 - minimal handling, below 20 - little attention; Extra Credit (100 points): "
                        + "impressive/innovative elements. Give me an integer score from 1-1000 AND ONLY RESPOND WITH A NUMBER AND NO TEXT.";

        // calculate the chat score for the answer based on the rubric
        Long chatScore = getChatScore(answerDto.getContent(), rubric);

        // create a new answer object
        AdventureAnswer answer = new AdventureAnswer(answerDto.getContent(), question, person, chatScore);
        answerJpaRepository.save(answer); // save the answer to the database

        double questionPoints = question.getPoints();
        double updatedBalance = person.getBalanceDouble() + questionPoints;
        person.setBalanceString(updatedBalance, "adventure");
        
        personJpaRepository.save(person); // save the updated person object

        // return the saved answer with an ok status
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    // helper method to calculate chat score using an external api
    private Long getChatScore(String content, String rubric) {
        OkHttpClient client = new OkHttpClient(); // initialize http client
        ObjectMapper mapper = new ObjectMapper(); // initialize object mapper

        // construct json request body for the api
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", "gpt-3.5-turbo"); // specify the model

        // construct the messages array for the api request
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system"); // set the role as system
        systemMessage.put("content", rubric); // include the rubric in the system message

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user"); // set the role as user
        userMessage.put("content", content); // include the answer content in the user message

        requestBody.put("temperature", 0.0); // set the model's temperature to zero

        // create the api request
        okhttp3.RequestBody body = okhttp3.RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl) // set the api url
                .post(body) // set the request method to post
                .addHeader("Authorization", "Bearer " + apiKey) // add the authorization header
                .build();

        // execute the api request and process the response
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // log the response for debugging
                System.out.println(response);
                // parse the response body
                JsonNode jsonNode = mapper.readTree(response.body().string());
                // extract the chat score from the response
                String chatGptResponse = jsonNode.get("choices").get(0).get("message").get("content").asText();
                return Long.parseLong(chatGptResponse); // return the chat score as a long
            } else {
                // log an error message if the request fails
                System.err.println("Request failed: " + response);
            }
        } catch (IOException e) {
            // handle exceptions during the api call
            e.printStackTrace();
        }
        return 0L; // return zero if the api call fails
    }

    @GetMapping("/leaderboard")
    public List<AdventureLeaderboardDto> getLeaderboard() {
    List<AdventureLeaderboardDto> leaderboardEntries = answerJpaRepository.findTop10PersonsByTotalScore();
    for (AdventureLeaderboardDto entry : leaderboardEntries) {
        Optional<Person> person = personJpaRepository.findById(entry.getId());
        String Name = person.isPresent() ? person.get().getName() : "Unknown";
        entry.setuserName(Name);
    }
    return leaderboardEntries;
    }
}