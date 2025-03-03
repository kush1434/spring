package com.nighthawk.spring_portfolio.mvc.forum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data // Lombok annotation to generate boilerplate code (getters, setters, toString, etc.)
@Entity // JPA annotation to specify that this class is an entity
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-arguments constructor
@Getter // Lombok annotation to generate getters
@Setter // Lombok annotation to generate setters
public class Forum {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // Primary key

    @Column(name = "author", nullable=false)
    private String author; // Name of the associated person

    @Column(name = "title", nullable=false)
    private String title; // Title of the post

    @Column(name = "context", nullable=false)
    private String context; // Context of the post

    @Column(nullable=false)
    private String date;

    @Column(name = "views", nullable=false)
    private int views;


    public Forum(String author, String title, String context, String date ,int views) {
        this.author = author;
        this.title = title;
        this.context = context;
        this.date = date;
        this.views = views;
    }

    // Static method for creating Forum objects
    public static Forum createQuestion(String author, String title, String context, String date,int views) {
        Forum forum = new Forum();
        forum.setAuthor(author);
        forum.setTitle(title);
        forum.setContext(context);
        forum.setDate(date);
        forum.setViews(views);

        return forum;
    }

    public String echoMessage(String message) {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Forum{author='%s', title='%s', context='%s', date='%s', views='%s'}", author, title, context, date, views);
    }
}
