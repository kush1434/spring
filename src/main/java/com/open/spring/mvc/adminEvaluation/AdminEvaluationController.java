package com.open.spring.mvc.adminEvaluation;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/admin-evaluation")
public class AdminEvaluationController {

    @Autowired
    private AdminEvaluationRepository adminEvaluationRepository;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminEvaluationRequest {
        @JsonProperty("userId")
        private Integer userId;
        @JsonProperty("attendance")
        private Double attendance;
        @JsonProperty("work_habits")
        private Double workHabits;
        @JsonProperty("behavior")
        private Double behavior;
        @JsonProperty("timeliness")
        private Double timeliness;
        @JsonProperty("tech_sense")
        private Double techSense;
        @JsonProperty("tech_talk")
        private Double techTalk;
        @JsonProperty("tech_growth")
        private Double techGrowth;
        @JsonProperty("advocacy")
        private Double advocacy;
        @JsonProperty("communication")
        private Double communication;
        @JsonProperty("integrity")
        private Double integrity;
        @JsonProperty("organization")
        private Double organization;
    }

    @PostMapping("/post")
    public ResponseEntity<?> createEvaluation(@RequestBody AdminEvaluationRequest req) {
        if (req.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        AdminEvaluation evaluation = new AdminEvaluation(
            req.getUserId(),
            req.getAttendance(),
            req.getWorkHabits(),
            req.getBehavior(),
            req.getTimeliness(),
            req.getTechSense(),
            req.getTechTalk(),
            req.getTechGrowth(),
            req.getAdvocacy(),
            req.getCommunication(),
            req.getIntegrity(),
            req.getOrganization()
        );
        AdminEvaluation saved = adminEvaluationRepository.save(evaluation);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "id", saved.getId(),
            "createdAt", saved.getCreatedAt(),
            "evaluation", saved
        ));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllEvaluations() {
        List<AdminEvaluation> results = adminEvaluationRepository.findAll();
        return ResponseEntity.ok(Map.of(
            "count", results.size(),
            "results", results
        ));
    }
}
