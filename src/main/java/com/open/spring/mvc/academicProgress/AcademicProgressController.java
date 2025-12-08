package com.open.spring.mvc.academicProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
import smile.data.vector.DoubleVector;
import smile.io.Read;
import smile.regression.RandomForest;

@RestController
@RequestMapping("/api/academic-progress")
public class AcademicProgressController {

    @Autowired
    private AcademicProgressRepository repository;

    @Autowired
    private AcademicPredictionRepository predictionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RandomForest model;
    private Map<String, Map<String, Integer>> encoders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionRequest {
        private String assignmentType;
        private String difficultyLevel;
        private String topic;
        private String completionStatus;
        private Integer hoursStudied;
        private Integer attendance;
    }

    @GetMapping("/train")
    public ResponseEntity<?> train() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS academic_progress (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "student_id INTEGER, " +
                         "assignment_completion_rate REAL, " +
                         "average_assignment_score REAL, " +
                         "collegeboard_quiz_average REAL, " +
                         "office_hours_visits INTEGER, " +
                         "conduct INTEGER, " +
                         "work_habit INTEGER, " +
                         "github_contributions INTEGER, " +
                         "final_grade INTEGER)";
            jdbcTemplate.execute(sql);

            String csvPath = "src/main/java/com/open/spring/mvc/academicProgress/fake-records-new.csv";
            
            DataFrame data = Read.csv(csvPath, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            if (repository.count() == 0) {
                List<AcademicProgress> records = new ArrayList<>();
                for (int i = 0; i < data.nrows(); i++) {
                    Tuple row = data.get(i);
                    AcademicProgress ap = new AcademicProgress();
                    ap.setStudentId(getSafeLong(row, "student_id"));
                    ap.setAssignmentCompletionRate(getSafeDouble(row, "assignment_completion_rate"));
                    ap.setAverageAssignmentScore(getSafeDouble(row, "average_assignment_score"));
                    ap.setCollegeboardQuizAverage(getSafeDouble(row, "collegeboard_quiz_average"));
                    ap.setOfficeHoursVisits(getSafeInt(row, "office_hours_visits"));
                    ap.setConduct(getSafeInt(row, "conduct"));
                    ap.setWorkHabit(getSafeInt(row, "work_habit"));
                    ap.setGithubContributions(getSafeInt(row, "github_contributions"));
                    ap.setFinalGrade(getSafeInt(row, "final_grade"));
                    records.add(ap);
                }
                repository.saveAll(records);
            }

            data = data.select("final_grade", "assignment_completion_rate", "average_assignment_score", 
                             "collegeboard_quiz_average", "office_hours_visits", "conduct", 
                             "work_habit", "github_contributions");

            this.encoders = new HashMap<>();

            Formula formula = Formula.lhs("final_grade");
            
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

    private Integer getSafeInt(Tuple row, String field) {
        Object val = row.getAs(field);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    private Double getSafeDouble(Tuple row, String field) {
        Object val = row.getAs(field);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0.0;
    }

    private Long getSafeLong(Tuple row, String field) {
        Object val = row.getAs(field);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0L;
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, Object> features) {
        if (model == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Model not trained. Call /train first."));
        }

        try {
            String sql = "CREATE TABLE IF NOT EXISTS academic_prediction (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "assignment_completion_rate REAL, " +
                         "average_assignment_score REAL, " +
                         "collegeboard_quiz_average REAL, " +
                         "office_hours_visits INTEGER, " +
                         "conduct INTEGER, " +
                         "work_habit INTEGER, " +
                         "github_contributions INTEGER, " +
                         "predicted_score REAL, " +
                         "created_at INTEGER)";
            jdbcTemplate.execute(sql);

            String[] numericFeatures = {"assignment_completion_rate", "average_assignment_score", 
                                       "collegeboard_quiz_average", "office_hours_visits", 
                                       "conduct", "work_habit", "github_contributions"};
            
            List<BaseVector> vectors = new ArrayList<>();

            for (String col : numericFeatures) {
                Number val = (Number) features.get(col);
                if (val == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Missing feature: " + col));
                }
                vectors.add(DoubleVector.of(col, new double[]{ val.doubleValue() }));
            }

            DataFrame singleRow = DataFrame.of(vectors.toArray(new BaseVector[0]));
            Tuple instance = singleRow.stream().findFirst().orElseThrow();

            double prediction = model.predict(instance);

            AcademicPrediction record = new AcademicPrediction(
                ((Number) features.get("assignment_completion_rate")).doubleValue(),
                ((Number) features.get("average_assignment_score")).doubleValue(),
                ((Number) features.get("collegeboard_quiz_average")).doubleValue(),
                ((Number) features.get("office_hours_visits")).intValue(),
                ((Number) features.get("conduct")).intValue(),
                ((Number) features.get("work_habit")).intValue(),
                ((Number) features.get("github_contributions")).intValue(),
                prediction
            );
            predictionRepository.save(record);

            return ResponseEntity.ok(Map.of(
                "predicted_final_grade", prediction,
                "status", "success",
                "prediction_id", record.getId()
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
