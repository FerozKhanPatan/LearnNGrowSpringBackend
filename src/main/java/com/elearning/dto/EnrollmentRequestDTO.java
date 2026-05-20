package com.elearning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestDTO {
    private Long userId;
    private String courseId;
    private String courseTitle;
    private String name;
    private String email;
    private String phoneNumber;
    private String message;
}
