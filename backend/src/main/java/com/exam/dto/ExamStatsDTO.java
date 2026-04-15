package com.exam.dto;

import lombok.Data;

@Data
public class ExamStatsDTO {
    private Integer total;
    private Integer passCount;
    private Double passRate;
    private Double avgScore;
    private Integer maxScore;
    private Integer minScore;
}
