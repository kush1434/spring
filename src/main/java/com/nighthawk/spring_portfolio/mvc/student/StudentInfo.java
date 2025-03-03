package com.nighthawk.spring_portfolio.mvc.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Annotations to simplify writing code (ie constructors, setters)
@NoArgsConstructor
@AllArgsConstructor
@Entity // Annotation to simplify creating an entity, which is a lightweight persistence domain object. Typically, an entity represents a table in a relational database, and each entity instance corresponds to a row in that table.
@Table(name = "students" , uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class StudentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id") // No UNIQUE constraint added here
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Person person;
    


    private String username;

    private int tableNumber;

    private String course;

    private ArrayList<String> tasks;
    
    private ArrayList<String> completed;

    private int trimester;

    private int period;
    
    @Column
    private String person_name;

    @Column
    private String dailyActivity;

    private Double averageRating;    

    public StudentInfo(String username, int tableNumber, String course, ArrayList<String> tasks, ArrayList<String> completed, int trimester, int period, String person_name) {
        this.username = username;
        this.tableNumber = tableNumber;
        this.course = course;
        this.tasks = tasks;
        this.completed = completed;
        this.trimester = trimester;
        this.period = period;
        this.person_name = person_name;
    }

    public static StudentInfo[] init(Person[] persons)
    {
        ArrayList<StudentInfo> studentInfos = new ArrayList<>();
        for(Person person: persons)
        {
            studentInfos.add(new StudentInfo("temp", 0, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), null, 0, 0, "temp"));

        }
        return studentInfos.toArray(new StudentInfo[0]);
    }
    @Service
    public static class StudentService {

        @Autowired
        private StudentInfoJPARepository studentJPARepository;

        @PostConstruct
        public void initialization() { 
            if (studentJPARepository == null) {
                throw new RuntimeException("studentJPARepository is not initialized!");
            }
            List<StudentInfo> students = new ArrayList<>();
            //students.add(new StudentInfo("nitinsandiego", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), null, 2, 1));
            //students.add(new StudentInfo("Akhil353", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), null, 2, 3));
            //students.add(new StudentInfo("SrinivasNampalli", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), null, 2, 1));
            //students.add(new StudentInfo("adityasamavedam", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1", "Task 2")), null, 2, 3));
            students.add(new StudentInfo("nitinsandiego", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Nitin"));
            students.add(new StudentInfo("SrinivasNampalli", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Srini"));
            students.add(new StudentInfo("SriS126", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Sri"));
            students.add(new StudentInfo("SGTech08", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Saathvik"));
            students.add(new StudentInfo("AidanLau10", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Aidan"));
            students.add(new StudentInfo("tanav-kambhampati", 2, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Tanav"));
            students.add(new StudentInfo("hanlunli", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Hanlun"));
            students.add(new StudentInfo("DavidL0914", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "David"));
            students.add(new StudentInfo("sharonkodali", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Sharon"));
            students.add(new StudentInfo("miggysp", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Miheer"));
            students.add(new StudentInfo("beijanm", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Beijan"));
            students.add(new StudentInfo("eshaank1", 1, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Eshaan"));
            students.add(new StudentInfo("7mwang", 3, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Matthew"));
            students.add(new StudentInfo("alishahussain", 3, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Alisha"));
            students.add(new StudentInfo("iwu78", 3, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Ian"));
            students.add(new StudentInfo("trevorhuang1", 3, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Trevor"));
            students.add(new StudentInfo("JoshThinh", 3, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Josh"));
            students.add(new StudentInfo("Dabear14", 3, "CSA", new ArrayList<String>(Arrays.asList("Task 1")), new ArrayList<String>(Arrays.asList("Task 1")), 2, 1, "Dinesh"));

            for (StudentInfo student : students) {
                Optional<StudentInfo> existingStudent = studentJPARepository.findByUsername(student.getUsername());
                if (existingStudent.isEmpty()) {
                    studentJPARepository.save(student);
                }
            }
        }

        public Iterable<StudentInfo> findAll() {
            return studentJPARepository.findAll();
        }

        public List<StudentInfo> findByUsernameCourseTrimesterPeriod(String username, String course, int trimester, int period) {
            return studentJPARepository.findByUsernameCourseTrimesterPeriod(username, course, trimester, period);
        }

        public StudentInfo createStudent(StudentInfo student) {
            // Check if a student with the same username already exists to avoid duplicates
            Optional<StudentInfo> existingStudent = studentJPARepository.findByUsername(student.getUsername());
            if (existingStudent.isPresent()) {
                throw new RuntimeException("A student with this username already exists.");
            }
            return studentJPARepository.save(student);
        }

        public void deleteById(Long id) {
            studentJPARepository.deleteById(id);
        }

        public Optional<StudentInfo> findByUsername(String username) {
            return studentJPARepository.findByUsername(username);
        }
        
        public List<StudentInfo> findTeam(String course, int trimester, int period, int table) {
            return studentJPARepository.findTeam(course, trimester, period, table);
        }

        public List<StudentInfo> findPeriod(String course, int trimester, int period) {
            return studentJPARepository.findPeriod(course, trimester, period);
        }   
    }
}