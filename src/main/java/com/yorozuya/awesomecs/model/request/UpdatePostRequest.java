package com.yorozuya.awesomecs.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    private String title;
    private String content;
    private String summary;
    private String category;
    private List<String> tags;
    private Integer status;
}
