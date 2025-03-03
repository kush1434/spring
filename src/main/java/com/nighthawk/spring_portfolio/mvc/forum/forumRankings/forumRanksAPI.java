package com.nighthawk.spring_portfolio.mvc.forum.forumRankings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
public class forumRanksAPI {

    @Autowired
    private forumRankRepository rankRepository; // Inject the repository

    @PostMapping("/rank/update")
    public String updateRank(@RequestBody RequestBodyData requestBodyData) {
        if (requestBodyData.getAuthor() == null || requestBodyData.getAuthor().isEmpty()) {
            return "Error: Author is required.";
        }

        try {
            String author = requestBodyData.getAuthor();
            int rankInt = requestBodyData.getRankInt();

            forumRankings currentRank = rankRepository.findByAuthor(author);
            if (currentRank != null) {
                currentRank.setRankInt(currentRank.getRankInt() + rankInt);
            } else {
                // get the current rank for the author and add the rankInt to that value
                currentRank = new forumRankings(null, author, rankInt);
            }
            if (currentRank.getRankInt() < 0) {
                currentRank.setRankInt(0);
            }
            if (currentRank.getRankInt() > 100) {
                currentRank.setRankInt(100);
            }
            
            rankRepository.save(currentRank);
            return "Successfully added to database\n" + author + ": " + currentRank.getRankInt();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }

    public static class RequestBodyData {
        private String author;
        private int rankInt;

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public int getRankInt() {
            return rankInt;
        }

        public void setRankInt(int rankInt) {
            this.rankInt = rankInt;
        }
    }
}