package com.open.spring.mvc.resume;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String username; // Linking resume to User (simplified for now)

    private String professionalSummary;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<JobExperience> experiences;
}
