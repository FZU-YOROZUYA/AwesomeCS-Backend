package com.yorozuya.awesomecs.service.impl;

import com.yorozuya.awesomecs.AwesomeCsApplication;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;
import com.yorozuya.awesomecs.repository.mapper.PostLikesMapper;
import com.yorozuya.awesomecs.repository.mapper.PostsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AwesomeCsApplication.class)
@Slf4j
public class PostsServiceImplTest {
    @Resource
    private PostsServiceImpl postsService;

    @Test
    public void testListPosts_returnsPageResponse() {
        PageResponse<PostSummaryResponse> resp = postsService.listPosts(1, 10, null, null, null);
        System.out.println(resp);
    }

    @Test
    void listPosts() {
    }

    @Test
    void getPostsCount() {
        String postsCount = postsService.getPostsCount(null);
        Assertions.assertNotNull(postsCount);
        log.info("postsCount: {}", postsCount);
        String postsCount1 = postsService.getPostsCount("frontend");
        Assertions.assertNotNull(postsCount1);
        log.info("postsCount1: {}", postsCount1);
    }

    @Test
    void listPopularPosts() {
    }

    @Test
    void getPostDetail() {
    }

    @Test
    void createPost() {
    }

    @Test
    void updatePost() {
    }

    @Test
    void deletePost() {
    }

    @Test
    void listUserLikedPosts() {
    }

    @Test
    void listPostsByUser() {
    }

    @Test
    void getTotalLikesByUser() {
    }

    @Test
    void getTotalViewsByUser() {
    }

    @Test
    void getTotalPostsByUser() {
    }
}
