package com.elearning.controller;

import com.elearning.model.LiveProject;
import com.elearning.repository.LiveProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class LiveProjectController {

    @Autowired
    private LiveProjectRepository liveProjectRepository;

    @GetMapping
    public ResponseEntity<List<LiveProject>> getAllProjects() {
        try {
            List<LiveProject> projects = liveProjectRepository.findAll();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LiveProject>> getProjectsByCourseId(@PathVariable Long courseId) {
        try {
            List<LiveProject> projects = liveProjectRepository.findByCourseId(courseId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
