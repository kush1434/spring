package com.nighthawk.spring_portfolio.mvc.rpg.adventureRubric;

import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AdventureRubric {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    // rubric uid
    @Column(unique = true, nullable = false)
    private String ruid;

    @Lob 
    @Column(columnDefinition = "TEXT") 
    private String criteria;

    public AdventureRubric (String ruid, String criteria) {
        this.ruid = ruid;
        this.criteria = criteria;
    }    

    public static AdventureRubric createRubric(String ruid, String criteria) {
        AdventureRubric rubric = new AdventureRubric();
        rubric.setRuid(ruid);
        rubric.setCriteria(criteria);

        return rubric;
    }

    public static AdventureRubric[] init() {
        ArrayList<AdventureRubric> rubrics = new ArrayList<>();
        
                rubrics.add(createRubric("Linux ls command", 
                "Accuracy of Explanation (500 points): 500 - fully accurate description of the 'ls' command's functionality, options, "
                + "and its importance in file management, 450 - mostly accurate but missing a few details, 400 - mostly correct but some details missing, "
                + "350 - significant misunderstanding or missing core aspects, below 350 - incorrect or incomplete explanation; "
                + "Completeness of the Answer (400 points): 400 - fully explains 'ls' command usage, options, and why it is essential for managing files, "
                + "350 - mostly complete but missing some details, 300 - basic coverage, 250 - lacks key examples or missing important functionality, "
                + "below 250 - minimal or incomplete explanation; Clarity and Structure (300 points): 300 - well-organized and clear, easy to follow, "
                + "250 - mostly clear but some organizational issues, 200 - somewhat disorganized or unclear, 150 - difficult to follow, below 150 - poorly structured; "
                + "Depth and Insight (200 points): 200 - deep understanding with examples, real-world scenarios, and advanced usage, "
                + "150 - some depth but lacks advanced insight, 100 - basic explanation with little depth, 50 - superficial, below 50 - minimal understanding; "
                + "Relevance to the Question (100 points): 100 - fully relevant, 85 - mostly relevant with minor digressions, 70 - some off-topic content, "
                + "50 - significant off-topic material, below 50 - irrelevant or inadequate. Give me an integer score from 1-1500"));
            
        return rubrics.toArray(new AdventureRubric[0]);
    }
}

