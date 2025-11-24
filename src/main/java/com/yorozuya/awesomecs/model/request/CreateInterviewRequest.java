package com.yorozuya.awesomecs.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.JdkConstants;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateInterviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String domain;
    private String style;
}
