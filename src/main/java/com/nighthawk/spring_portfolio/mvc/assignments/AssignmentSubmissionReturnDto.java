package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.List;

import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmissionAPIController.AssignmentReturnDto;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmissionAPIController.PersonSubmissionDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentSubmissionReturnDto {
    public Long id;
    public AssignmentReturnDto assignment;
    public List<PersonSubmissionDto> students;
    public String content;
    public String comment;
    public Double grade;
    public String feedback;
    public Boolean isLate;

    public AssignmentSubmissionReturnDto(AssignmentSubmission submission) {
        this.id = submission.getId();
        this.assignment = new AssignmentReturnDto(submission.getAssignment());
        this.students = submission.getStudents().stream().map(PersonSubmissionDto::new).toList();
        this.content = submission.getContent();
        this.comment = submission.getComment();
        this.grade = submission.getGrade();
        this.feedback = submission.getFeedback();
        this.isLate = submission.getIsLate();
    }
}