package com.open.spring.mvc.academicProgress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.vector.BaseVector;
import smile.data.vector.IntVector;
import smile.data.vector.StringVector;
import smile.io.Read;
import smile.regression.RandomForest;

@RestController
@RequestMapping("/api/academic-progress")
public class AcademicProgressController {

    @Autowired
    private AcademicProgressRepository repository;

    private RandomForest model;
    private Map<String, Map<String, Integer>> encoders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionRequest {
        private String assignmentType;
        private String difficultyLevel;
        private String topic;
    }

    @GetMapping("/train")
    public ResponseEntity<?> train() {
        try {
            String csvPath = "src/main/java/com/open/spring/mvc/academicProgress/fake-records.csv";
            
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            data = data.select("score", "assignment_type", "difficulty_level", "topic", "completion_status");

            this.encoders = new HashMap<>();

            String[] stringCols = {"assignment_type", "difficulty_level", "topic", "completion_status"};
            
            for (String col : stringCols) {
                StringVector vec = data.stringVector(col);
                int[] encoded = new int[vec.size()];
                Map<String, Integer> map = new HashMap<>();
                int id = 0;
                
                for (int i = 0; i < vec.size(); i++) {
                    String val = vec.get(i);
                    if (!map.containsKey(val)) {
                        map.put(val, id++);
                    }
                    encoded[i] = map.get(val);
                }
                
                encoders.put(col, map);

                data = data.drop(col).merge(IntVector.of(col, encoded));
            }

            Formula formula = Formula.lhs("score");
            
            this.model = RandomForest.fit(formula, data);

            return ResponseEntity.ok(Map.of(
                "message", "Model trained successfully",
                "rows_processed", data.nrows(),
                "columns", data.names(),
                "model_type", "RandomForest Regression"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Training failed: " + e.getMessage()));
        }
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, Object> features) {
        if (model == null || encoders == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Model not trained. Call /train first."));
        }

        try {

            String[] featureNames = {"assignment_type", "difficulty_level", "topic", "completion_status"};
            
            BaseVector[] vectors = new BaseVector[featureNames.length];

            for (int i = 0; i < featureNames.length; i++) {
                String col = featureNames[i];
                String val = (String) features.get(col);

                if (val == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Missing feature: " + col));
                }

                if (!encoders.containsKey(col) || !encoders.get(col).containsKey(val)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Unknown value for feature '" + col + "': " + val));
                }

                int encodedVal = encoders.get(col).get(val);
                vectors[i] = IntVector.of(col, new int[]{ encodedVal });
            }

            DataFrame singleRow = DataFrame.of(vectors);
            Tuple instance = singleRow.stream().findFirst().orElseThrow();

            double prediction = model.predict(instance);

            return ResponseEntity.ok(Map.of(
                "predicted_score", prediction,
                "status", "success"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Prediction failed: " + e.getMessage()));
        }
    }

    @GetMapping("/records")
    public ResponseEntity<List<AcademicProgress>> getRecords() {
        return ResponseEntity.ok(repository.findAll());
    }
}
