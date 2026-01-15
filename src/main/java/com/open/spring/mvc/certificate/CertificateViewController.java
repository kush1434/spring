package com.open.spring.mvc.certificate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mvc/certificates")
@Slf4j
public class CertificateViewController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private UserCertificateRepository userCertificateRepository;

    @GetMapping("")
    public String getCertificates(Model model) {
        return "certificates/certificateManager";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createCertificate(@RequestParam String title) {
        Certificate newCertificate = new Certificate();
        newCertificate.setTitle(title);
        certificateRepository.save(newCertificate);
        return "redirect:/mvc/certificates";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteCertificate(@PathVariable Long id) {
        // First, delete associated user certificates
        List<UserCertificate> userCertificates = userCertificateRepository.findByCertificateId(id);
        userCertificateRepository.deleteAll(userCertificates);
        
        // Then, delete the certificate
        certificateRepository.deleteById(id);
        return "redirect:/mvc/certificates";
    }
}
