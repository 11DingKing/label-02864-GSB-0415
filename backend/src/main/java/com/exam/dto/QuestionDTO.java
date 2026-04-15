package com.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    
    @NotNull(message = "考试ID不能为空")
    private Long examId;
    
    @NotNull(message = "题目类型不能为空")
    private Integer type;
    
    @NotBlank(message = "题目内容不能为空")
    private String content;
    
    private String referenceAnswer; // 简答题参考答案
    
    private String keywords; // 简答题关键词，多个用逗号分隔
    
    @NotNull(message = "分值不能为空")
    private Integer score;
    
    private Integer sortOrder;
    
    private List<OptionDTO> options;
    
    @Data
    public static class OptionDTO {
        private Long id;
        private String optionKey;
        private String content;
        private Integer isCorrect;
        private Integer sortOrder;
    }
}
