package com.nighthawk.spring_portfolio.mvc.person;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Convert;
import static jakarta.persistence.FetchType.EAGER;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.persistence.CascadeType;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nighthawk.spring_portfolio.mvc.userStocks.userStocksTable;
import com.vladmihalcea.hibernate.type.json.JsonType;

import io.github.cdimascio.dotenv.Dotenv;

import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmission;
import com.nighthawk.spring_portfolio.mvc.bathroom.Tinkle;
import com.nighthawk.spring_portfolio.mvc.student.StudentInfo;
import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGrade;

import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import com.nighthawk.spring_portfolio.mvc.bank.Bank;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Convert(attributeName = "person", converter = JsonType.class)
@JsonIgnoreProperties({"submissions"})
public class Person implements Comparable<Person> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SynergyGrade> grades;

    @ManyToMany(mappedBy = "students", cascade = CascadeType.MERGE)
    @JsonIgnore
    private List<AssignmentSubmission> submissions;

    @ManyToMany(fetch = EAGER)
    @JoinTable(
        name = "person_person_sections",
        joinColumns = @JoinColumn(name = "person_id"),
        inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private Collection<PersonSections> sections = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<PersonRole> roles = new ArrayList<>();

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    private Tinkle timeEntries;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "person")
    @JsonIgnore
    private StudentInfo studentInfo;

    @NotEmpty
    @Size(min = 1)
    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(unique = true, nullable = false)
    private String uid;

    @NotEmpty
    private String password;

    @NonNull
    @Size(min = 2, max = 30, message = "Name (2 to 30 chars)")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Column(length = 255, nullable = true)
    private String pfp;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean kasmServerNeeded = false;

    @Column(nullable = true)
    private String sid;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "person")
    @JsonIgnore
    private userStocksTable user_stocks;

    @Column
    private String balance;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "person")
    @JsonIgnore
    private Bank bank;

    // Method to update balance in both Person and Bank
    public void setBalance(String balance) {
        this.balance = balance;
        if (this.bank != null) {
            this.bank.setBalance(Double.parseDouble(balance)); // Update Bank balance
        }
    }

    // Method to update balance with a double value
    public void setBalance(double balance) {
        this.balance = String.valueOf(balance);
        if (this.bank != null) {
            this.bank.setBalance(balance); // Update Bank balance
        }
    }

    // Method to set balance as a string and return the updated balance
    public String setBalanceString(double updatedBalance) {
        this.balance = String.valueOf(updatedBalance); // Update the balance as a String
        if (this.bank != null) {
            this.bank.setBalance(updatedBalance); // Update Bank balance
        }
        return this.balance; // Return the updated balance as a String
    }

    public double getBalanceDouble() {
        return Double.parseDouble(this.balance);
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Map<String, Object>> stats = new HashMap<>();

    @PreRemove
    private void removePersonFromSubmissions() {
        if (submissions != null) {
            for (AssignmentSubmission submission : submissions) {
                submission.getStudents().remove(this);
            }
        }
    }

    public Person(String email, String uid, String password, String sid, String name, Date dob, String pfp, String balance, Boolean kasmServerNeeded, PersonRole role) {
        this.email = email;
        this.uid = uid;
        this.password = password;
        this.sid = sid;
        this.name = name;
        this.dob = dob;
        this.kasmServerNeeded = kasmServerNeeded;
        this.pfp = pfp;
        this.balance = balance;
        this.roles.add(role);
        this.submissions = new ArrayList<>();
        this.timeEntries = new Tinkle(this, "");
    }

    public boolean hasRoleWithName(String roleName) {
        for (PersonRole role : roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    public int getAge() {
        if (this.dob != null) {
            LocalDate birthDay = this.dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return Period.between(birthDay, LocalDate.now()).getYears();
        }
        return -1;
    }

    @Override
    public int compareTo(Person other) {
        return this.name.compareTo(other.name);
    }

    public static Person createPerson(String name, String email, String uid, String password, String sid, Boolean kasmServerNeeded, String balance, String dob, List<String> asList) {
        return createPerson(name, email, uid, password, sid, kasmServerNeeded, balance, dob, Arrays.asList("ROLE_USER", "ROLE_STUDENT"));
    }

    public static Person createPerson(String name, String uid, String email, String password, String sid, String pfp, Boolean kasmServerNeeded, String balance, String dob, List<String> roleNames) {
        Person person = new Person();
        person.setName(name);
        person.setUid(uid);
        person.setEmail(email);
        person.setPassword(password);
        person.setSid(sid);
        person.setKasmServerNeeded(kasmServerNeeded);
        person.setBalance(balance);
        person.setPfp(pfp);
        try {
            Date date = new SimpleDateFormat("MM-dd-yyyy").parse(dob);
            person.setDob(date);
        } catch (Exception e) {
            // handle exception
        }

        List<PersonRole> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            PersonRole role = new PersonRole(roleName);
            roles.add(role);
        }
        person.setRoles(roles);

        return person;
    }

    public static String startingBalance = "100000";
    public static Person[] init() {
        ArrayList<Person> people = new ArrayList<>();
        final Dotenv dotenv = Dotenv.load();
        final String adminPassword = dotenv.get("ADMIN_PASSWORD");
        final String defaultPassword = dotenv.get("DEFAULT_PASSWORD");
        people.add(createPerson("Thomas Edison", "toby", "toby@gmail.com", adminPassword, "1", "/images/toby.png", true, startingBalance, "01-01-1840", Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_TESTER", "ROLE_TEACHER")));
        people.add(createPerson("Alexander Graham Bell", "lex", "lexb@gmail.com", defaultPassword, "1", "/images/lex.png", true, startingBalance, "01-01-1847", Arrays.asList("ROLE_USER", "ROLE_STUDENT")));
        people.add(createPerson("Nikola Tesla", "niko", "niko@gmail.com", defaultPassword, "1", "/images/niko.png", true, startingBalance, "01-01-1850", Arrays.asList("ROLE_USER", "ROLE_STUDENT")));
        people.add(createPerson("Madam Curie", "madam", "madam@gmail.com", defaultPassword, "1", "/images/madam.png", true, startingBalance, "01-01-1860", Arrays.asList("ROLE_USER", "ROLE_STUDENT")));
        people.add(createPerson("Grace Hopper", "hop", "hop@gmail.com", defaultPassword, "123", "/images/hop.png", true, startingBalance, "12-09-1906", Arrays.asList("ROLE_USER", "ROLE_STUDENT")));
        people.add(createPerson("John Mortensen", "jm1021", "jmort1021@gmail.com", defaultPassword, "1", "/images/jm1021.png", true, startingBalance, "10-21-1959", Arrays.asList("ROLE_ADMIN", "ROLE_TEACHER")));
        people.add(createPerson("Alan Turing", "alan", "turing@gmail.com", defaultPassword, "2", "/images/alan.png", false, startingBalance, "06-23-1912", Arrays.asList("ROLE_USER", "ROLE_TESTER", "ROLE_STUDENT")));

        Collections.sort(people);
        for (Person person : people) {
            userStocksTable stock = new userStocksTable(null, "BTC,ETH", startingBalance, person.getEmail(), person, false, true, "");
            person.setUser_stocks(stock);

            // Initialize the Bank entity for each person
            Bank bank = new Bank();
            bank.setPerson(person);
            bank.setUsername(person.getEmail()); // Set the username using the person's email
            bank.setBalance(Double.parseDouble(startingBalance));
            bank.setLoanAmount(0.0);
            bank.setStocksOwned(new ArrayList<>());
            bank.setGamblingProfit(new ArrayList<>());
            bank.setAdventureGameProfit(new ArrayList<>());
            bank.setStocksProfit(new ArrayList<>());
            person.setBank(bank);
        }

        return people.toArray(new Person[0]);
    }

    public static void main(String[] args) {
        Person[] persons = init();
        for (Person person : persons) {
            System.out.println(person);
            System.out.println();
        }
    }

    public Date getDob() {
        return this.dob;
    }

    public String getPfp() {
        return this.pfp;
    }

    public Collection<PersonRole> getRoles() {
        return this.roles;
    }
}