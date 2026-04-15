package com.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SubmitExamDTO {
    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;
    
    private List<AnswerDTO> answers;
    
    @Data
    public static class AnswerDTO {
        private Long questionId;
        private String answer;
    }
}
