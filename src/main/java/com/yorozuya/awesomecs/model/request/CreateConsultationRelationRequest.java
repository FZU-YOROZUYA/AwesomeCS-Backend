package com.yorozuya.awesomecs.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationRelationRequest {
    private Double price;
    private String info;
}
