package com.nighthawk.spring_portfolio.mvc.forum.forumBlogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/blogs")
public class forumBlogAPI {

    @Autowired
    private forumBlogRepository blogRepository; // Inject the repository


    @GetMapping("/increase/{title}")
    public String increaseViewCount(@PathVariable String title) {
        try {
            // Find the post by title
            forumBlogs forumBlogs = blogRepository.findByTitle(title);
            if (forumBlogs == null) {
                return "Error: Forum post with the given title not found.";
            }

            // Increment the view count
            forumBlogs.setViews(forumBlogs.getViews() + 1);

            // Save the updated post back to the repository
            blogRepository.save(forumBlogs);

            return "View count increased successfully!";
        } catch (Exception e) {
            System.out.println("An error occurred while increasing the view count: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while increasing the view count: " + e.getMessage();
        }
    }

    @PostMapping("/blog/remove")
    public String removeBlog(@RequestBody RequestBlogData requestBlogData) {
        System.out.println("Received message: " + requestBlogData.getTitle());
        try {
            String title = requestBlogData.getTitle();
            String author = requestBlogData.getAuthor();

            String fileName = title.replaceAll(" ", "_").toLowerCase() + ".txt";
            String filePath = "volumes/forumBlogs/" + fileName;
            // delete the body to the file
            java.nio.file.Files.delete(java.nio.file.Paths.get(filePath));
            return "Successfully removed blog from database";
        } catch (RestClientException | IOException e) {
            System.out.println("An error occurred while writing the blog: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while writing the blog: " + e.getMessage();
        }
    }

    @PostMapping("/blog/voteCount")
    public String getVote(@RequestBody RequestBlogVote requestBlogVote) {
        try {
            String title = requestBlogVote.getTitle();
            System.out.println(title);
            forumBlogs blog = blogRepository.findByTitle(title);
            if (blog == null) {
                return "{\"error\": \"Blog not found.\"}";
            }
            // Format to JSON format
            return "{\"votes\": " + blog.getVotes() + "}";
        } catch (Exception e) {
            System.out.println("An error occurred while getting the blog: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"An error occurred while getting the blog: " + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/blog/vote")
    public String postVote(@RequestBody RequestBlogVote requestBlogVote) {
        try {
            String title = requestBlogVote.getTitle();
            String vote = requestBlogVote.getVote();
            forumBlogs blog = blogRepository.findByTitle(title);
            if (blog == null) {
                return "Error: Blog not found.";
            }
            int currentVotes = blog.getVotes();
            System.out.println(vote);
            if (vote.equals("up")) {
                currentVotes++;
            } else if (vote.equals("down")) {
                currentVotes--;
            }
            blog.setVotes(currentVotes);
            blogRepository.save(blog);
            return "Successfully voted blog";
        } catch (Exception e) {
            System.out.println("An error occurred while voting the blog: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while voting the blog: " + e.getMessage();
        }
    }

    @PostMapping("/blog/post")
    public String getInputBlog(@RequestBody RequestBlogData requestBlogData) {
        System.out.println("Received message: " + requestBlogData.getTitle());
        try {
            String title = requestBlogData.getTitle();
            String body = requestBlogData.getBody();
            String author = requestBlogData.getAuthor();
            String date = java.time.LocalDate.now().toString();
            
            if (author == null || author.isEmpty()) {
                String[] authorEnding = {"Whale", "Pig", "Badger", "Warthog", "Fish", "Cow", "Chicken", "Rabbit", "Wolf", "Bear"};
                author = "Anonymous" + authorEnding[(int) (Math.random() * 10)];
                requestBlogData.setAuthor(author);
            }

            String fileName = title.replaceAll(" ", "_").toLowerCase() + ".txt";
            String filePath = "volumes/forumBlogs/" + fileName;
            String formattedTitle = title.toLowerCase();

            // Check if the title is already in the database
            if (blogRepository.findByTitle(title) != null) {
                return "Error: Title already exists.";
            }

            // Create a new ForumBlogs object and save it to the forumBlog database table
            forumBlogs forumTable = new forumBlogs(author, formattedTitle, filePath, date, 0);
            blogRepository.save(forumTable); // Use the repository to save

            // Save the body to the file
            Files.write(Paths.get(filePath), body.getBytes());

            return "Successfully added blog to database";
        } catch (RestClientException | IOException e) {
            System.out.println("An error occurred while writing the blog: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while writing the blog: " + e.getMessage();
        }
    }

    @PostMapping("/blog/view")
    public String getBlog(@RequestBody RequestBlogVote requestBlogView) {
        try {
            String title = requestBlogView.getTitle();
            String fileName = title.replaceAll(" ", "_").toLowerCase() + ".txt";
            String filePath = "volumes/forumBlogs/" + fileName;

            // check if the file exists
            if (!Files.exists(Paths.get(filePath))) {
                return "Error: Blog not found.";
            }

            // Read the body from the file
            String body = new String(Files.readAllBytes(Paths.get(filePath)));

            return body;
        } catch (IOException e) {
            System.out.println("An error occurred while reading the blog: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while reading the blog: " + e.getMessage();
        }
    }

    @GetMapping("/blog/get")
    public List<forumBlogs> getLatestBlogs() {
        // return a list of 5 of the latest blogs sorted by most recent date
        return blogRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getDate().compareTo(b1.getDate()))
                .limit(4)
                .toList();
    }

    public static class RequestBlogVote {
        private String title;
        private String vote;
    
        public String getTitle() {
            return title;
        }
    
        public void setTitle(String title) {
            this.title = title;
        }
    
        public String getVote() {
            return vote;
        }
    
        public void setVote(String vote) {
            this.vote = vote;
        }
    }

    public static class RequestBlogData {
        private String title;
        private String body;
        private String author;
        private int views;

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getBody() {
            return body;
        }
        public void setBody(String body) {
            this.body = body;
        }
        public void setAuthor(String author) {
            this.author = author;
        }
        public String getAuthor() {
            return author;
        }

       

        public int getViews() {
            return views;
        }
        
    }

}
