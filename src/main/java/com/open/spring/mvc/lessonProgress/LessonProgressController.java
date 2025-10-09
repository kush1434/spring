package com.open.spring.mvc.lessonProgress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lesson-progress")
@CrossOrigin(origins = "*") // Allow Jekyll frontend to call backend
public class LessonProgressController {

    @Autowired
    private LessonProgressRepository progressRepo;

    // --- CREATE ---
    @PostMapping
    public LessonProgress createProgress(@RequestBody LessonProgress progress) {
        return progressRepo.save(progress);
    }

    // --- READ ---
    @GetMapping("/{userId}/{lessonKey}")
    public LessonProgress getProgress(@PathVariable String userId, @PathVariable String lessonKey) {
        Optional<LessonProgress> progress = progressRepo.findByUserIdAndLessonKey(userId, lessonKey);
        return progress.orElseGet(() -> {
            LessonProgress newProgress = new LessonProgress();
            newProgress.setUserId(userId);
            newProgress.setLessonKey(lessonKey);
            return progressRepo.save(newProgress);
        });
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public LessonProgress updateProgress(@PathVariable Long id, @RequestBody LessonProgress updatedProgress) {
        return progressRepo.findById(id).map(existing -> {
            existing.setTotalTimeMs(updatedProgress.getTotalTimeMs());
            existing.setLastVisited(updatedProgress.getLastVisited());
            existing.setCompleted(updatedProgress.getCompleted());
            existing.setBadges(updatedProgress.getBadges());
            existing.setReflectionText(updatedProgress.getReflectionText());
            existing.setCurrentFlashcardIndex(updatedProgress.getCurrentFlashcardIndex());
            existing.setFlashcardProgress(updatedProgress.getFlashcardProgress());
            return progressRepo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Progress not found"));
    }

    // --- LIST (for admin/testing) ---
    @GetMapping
    public List<LessonProgress> getAll() {
        return progressRepo.findAll();
    }
}
