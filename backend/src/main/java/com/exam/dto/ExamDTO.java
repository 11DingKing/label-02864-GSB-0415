package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamDTO {
    private Long id;
    
    @NotBlank(message = "考试标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "考试时长不能为空")
    private Integer duration;
    
    private Integer totalScore = 100;
    private Integer passScore = 60;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
