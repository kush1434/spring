package com.open.spring.mvc.certificate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;

import com.open.spring.mvc.user.User;

import java.util.Date;

@Entity
public class UserCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = User.class, optional = false)
    private User user;

    @ManyToOne(targetEntity = Certificate.class, optional = false)
    private Certificate certificate;

    @Column(nullable = false)
    private Date dateIssued;

    public UserCertificate() {
    }

    public UserCertificate(User user, Certificate certificate, Date dateIssued) {
        this.user = user;
        this.certificate = certificate;
        this.dateIssued = dateIssued;
    }
}
