package com.elearning.dto;

import com.elearning.model.Enrollment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private String status;
    private Integer progress;
    private String phoneNumber;
    private LocalDateTime enrollmentDate;
    private String message;

    public static EnrollmentResponseDTO fromEntity(Enrollment enrollment) {
        EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUser() != null ? enrollment.getUser().getId() : null);
        dto.setUserName(enrollment.getUser() != null ? enrollment.getUser().getName() : null);
        dto.setCourseId(enrollment.getCourse() != null ? enrollment.getCourse().getId() : null);
        dto.setCourseTitle(enrollment.getCourseTitle());
        dto.setStatus(enrollment.getStatus() != null ? enrollment.getStatus().toString() : null);
        dto.setProgress(enrollment.getProgress());
        dto.setPhoneNumber(enrollment.getPhoneNumber());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setMessage(enrollment.getMessage());
        return dto;
    }
}
