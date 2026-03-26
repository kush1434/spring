package com.open.spring.mvc.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FaceRecognitionService {

    @Autowired
    private PersonJpaRepository repository;

    @Autowired
    private FaceRecognitionPythonService faceRecognitionPythonService;

    /**
     * Identifies a person from a query image against candidates in the database.
     * Following SRP, this service orchestrates the process of gathering candidates 
     * and calling the identification subprocess.
     */
    public Map<String, Object> identify(String queryImage, Double threshold) {
        List<Person> peopleWithFaces = repository.findByFaceDataIsNotNull();
        List<Map<String, String>> candidates = new ArrayList<>();

        for (Person p : peopleWithFaces) {
            if (p.getFaceData() != null && !p.getFaceData().isBlank()) {
                Map<String, String> candidate = new HashMap<>();
                candidate.put("uid", p.getUid());
                candidate.put("faceData", p.getFaceData());
                candidate.put("name", p.getName());
                candidates.add(candidate);
            }
        }

        if (candidates.isEmpty()) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("match", false);
            emptyResult.put("message", "No candidates with face data found in database");
            return emptyResult;
        }

        return faceRecognitionPythonService.identifyFace(queryImage, candidates, threshold != null ? threshold : 0.40);
    }
}
