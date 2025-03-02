package com.nighthawk.spring_portfolio.mvc.blackjack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

/**
 * REST API Controller for the Blackjack game.
 * Handles player actions including starting the game, hitting, and standing.
 */
@RestController
@RequestMapping("/api/casino/blackjack")
public class BlackjackApiController {

    private static final Logger LOGGER = Logger.getLogger(BlackjackApiController.class.getName());

    @Autowired
    private BlackjackJpaRepository repository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    /**
     * Starts a new Blackjack game for a player.
     *
     * @param request A map containing "uid" (User ID) and "betAmount" (Bet amount for the game).
     * @return A `ResponseEntity` with the game state if successful, or an error message.
     */
    @PostMapping("/start")
    public ResponseEntity<Blackjack> startGame(@RequestBody Map<String, Object> request) {
        try {
            String uid = request.get("uid").toString();
            double betAmount = Double.parseDouble(request.get("betAmount").toString());

            Person person = personJpaRepository.findByUid(uid);
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Create a new game instance
            Blackjack game = new Blackjack();
            game.setPerson(person);
            game.setStatus("ACTIVE");
            game.setBetAmount(betAmount);
            game.initializeDeck();
            game.dealInitialHands();

            repository.save(game);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Handles the "Hit" action in Blackjack.
     * The player requests an additional card.
     *
     * @param request A map containing "uid" (User ID).
     * @return A `ResponseEntity` with the updated game state or an error message.
     */
    @PostMapping("/hit")
    public ResponseEntity<Object> hit(@RequestBody Map<String, Object> request) {
        try {
            String uid = request.get("uid").toString();
            Person person = personJpaRepository.findByUid(uid);

            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found");
            }

            Optional<Blackjack> optionalGame = repository.findFirstByPersonAndStatusOrderByIdDesc(person, "ACTIVE");
            if (optionalGame.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active game found");
            }

            Blackjack game = optionalGame.get();
            game.loadGameState();

            List<String> playerHand = (List<String>) game.getGameStateMap().get("playerHand");
            List<String> deck = (List<String>) game.getGameStateMap().get("deck");

            if (deck == null || deck.isEmpty()) {
                return ResponseEntity.ok("Deck is empty");
            }

            // Draw a card from the deck and add it to the player's hand
            String drawnCard = deck.remove(0);
            playerHand.add(drawnCard);
            int playerScore = game.calculateScore(playerHand);

            // Update game state
            game.getGameStateMap().put("playerHand", playerHand);
            game.getGameStateMap().put("playerScore", playerScore);
            game.getGameStateMap().put("deck", deck);
            game.persistGameState();

            // If player busts, update balance and end game
            if (playerScore > 21) {
                double updatedBalance = person.getBalanceDouble() - game.getBetAmount();
                person.setBalanceString(updatedBalance);
                game.setStatus("INACTIVE");
                personJpaRepository.save(person);
            }

            repository.save(game);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing hit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    /**
     * Handles the "Stand" action in Blackjack.
     * The dealer plays its turn and the game result is determined.
     *
     * @param request A map containing "uid" (User ID).
     * @return A `ResponseEntity` with the updated game state or an error message.
     */
    @PostMapping("/stand")
    public ResponseEntity<Object> stand(@RequestBody Map<String, Object> request) {
        try {
            String uid = request.get("uid").toString();
            Person person = personJpaRepository.findByUid(uid);

            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found");
            }

            Optional<Blackjack> optionalGame = repository.findFirstByPersonAndStatusOrderByIdDesc(person, "ACTIVE");
            if (optionalGame.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active game found");
            }

            Blackjack game = optionalGame.get();
            game.loadGameState();

            List<String> dealerHand = (List<String>) game.getGameStateMap().get("dealerHand");
            List<String> deck = (List<String>) game.getGameStateMap().get("deck");
            int playerScore = (int) game.getGameStateMap().getOrDefault("playerScore", 0);
            int dealerScore = (int) game.getGameStateMap().getOrDefault("dealerScore", 0);
            double betAmount = game.getBetAmount();

            // Dealer must draw until score is at least 17
            while (dealerScore < 17 && deck != null && !deck.isEmpty()) {
                String drawnCard = deck.remove(0);
                dealerHand.add(drawnCard);
                dealerScore = game.calculateScore(dealerHand);
            }

            // Update game state
            game.getGameStateMap().put("dealerHand", dealerHand);
            game.getGameStateMap().put("dealerScore", dealerScore);
            game.getGameStateMap().put("deck", deck);

            // Determine game result and update player balance
            String result;
            if (playerScore > 21) {
                result = "LOSE";
                double updatedBalance = person.getBalanceDouble() - betAmount;
                person.setBalanceString(updatedBalance);
            } else if (dealerScore > 21 || playerScore > dealerScore) {
                result = "WIN";
                double updatedBalance = person.getBalanceDouble() + betAmount;
                person.setBalanceString(updatedBalance);
            } else if (playerScore < dealerScore) {
                result = "LOSE";
                double updatedBalance = person.getBalanceDouble() - betAmount;
                person.setBalanceString(updatedBalance);
            } else {
                result = "DRAW"; // No balance change on draw
            }

            // Save game result and mark game as inactive
            game.getGameStateMap().put("result", result);
            game.setStatus("INACTIVE");
            repository.save(game);
            personJpaRepository.save(person);

            return ResponseEntity.ok(game);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing stand", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}