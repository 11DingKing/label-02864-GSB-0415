package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Integer type; // 1-单选, 2-多选, 3-判断, 4-简答
    private String content;
    private String referenceAnswer; // 简答题参考答案
    private String keywords; // 简答题关键词，多个用逗号分隔
    private Integer score;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private List<QuestionOption> options;
}
