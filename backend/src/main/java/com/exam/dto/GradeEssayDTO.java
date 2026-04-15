package com.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GradeEssayDTO {
    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;
    
    private List<EssayGradeDTO> grades;
    
    @Data
    public static class EssayGradeDTO {
        @NotNull(message = "答题记录ID不能为空")
        private Long answerRecordId;
        @NotNull(message = "最终得分不能为空")
        private Integer finalScore;
        private String teacherComment;
    }
}
