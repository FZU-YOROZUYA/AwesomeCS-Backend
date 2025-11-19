package com.yorozuya.awesomecs.controller;


import com.yorozuya.awesomecs.service.StudyPathRecommendationsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin
@Slf4j
public class StudyPathRecommendController {

    @Resource
    private StudyPathRecommendationsService  studyPathRecommendationsService;


    @PostMapping("/")
    public Flux<String> getStudyPathRecommendations() {
        return null;
    }

}
