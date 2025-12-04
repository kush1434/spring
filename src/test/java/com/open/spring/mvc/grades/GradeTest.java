package com.open.spring.mvc.grades;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GradeTest {

    @Test
    public void testGithubIdAlias() {
        Grade grade = new Grade();
        String githubId = "gh_12345";

        // Test setter
        grade.setGithubId(githubId);
        assertEquals(githubId, grade.getStudentId(), "Setting GithubId should update studentId");

        // Test getter
        assertEquals(githubId, grade.getGithubId(), "getGithubId should return studentId");

        // Test direct studentId set
        String newId = "gh_67890";
        grade.setStudentId(newId);
        assertEquals(newId, grade.getGithubId(), "Setting studentId should update getGithubId");

        // Test course field
        String course = "CSA";
        grade.setCourse(course);
        assertEquals(course, grade.getCourse(), "getCourse should return set course");

        // Test classes alias
        String classes = "CSSE";
        grade.setClasses(classes);
        assertEquals(classes, grade.getCourse(), "setClasses should update course");
        assertEquals(classes, grade.getClasses(), "getClasses should return course");
    }
}
