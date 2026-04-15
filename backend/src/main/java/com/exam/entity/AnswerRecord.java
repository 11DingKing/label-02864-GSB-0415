package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("answer_record")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long questionId;
    private String answer;
    private Integer isCorrect;
    private Integer score;
}
