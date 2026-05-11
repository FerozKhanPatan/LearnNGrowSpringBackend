package com.elearning.repository;

import com.elearning.model.LiveProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveProjectRepository extends JpaRepository<LiveProject, Long> {
    
    List<LiveProject> findByCourseId(Long courseId);
}
