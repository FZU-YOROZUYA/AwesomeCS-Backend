package com.yorozuya.awesomecs.controller;


import com.yorozuya.awesomecs.service.StudyPathRecommendationsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Slf4j
public class StudyPathRecommendController {

    @Resource
    private StudyPathRecommendationsService  studyPathRecommendationsService;

}
