package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.AwesomeCsApplication;
import com.yorozuya.awesomecs.model.request.CreatePostRequest;
import com.yorozuya.awesomecs.model.request.UpdatePostRequest;
import com.yorozuya.awesomecs.service.PostsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest(classes = AwesomeCsApplication.class)
public class PostServiceTest {

    @Autowired
    @Qualifier("PostsService")
    PostsService postsService;

//    生成用户id的函数
    private Long generateUserId(Long len){
        Random random = new SecureRandom();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<len;i++){
            sb.append(random.nextInt(10));
        }
        return Long.parseLong(sb.toString());
    }
//    创建博客
    @Test
    public void createPostTest(){
        List<String> tags = new ArrayList<>();
        tags.add(null);
        CreatePostRequest cpr = new CreatePostRequest();
        cpr.setCategory(null);
        cpr.setTitle(null);
        cpr.setContent(null);
        cpr.setTags(tags);
        cpr.setSummary(null);
        cpr.setStatus(1);
        Long uid=generateUserId(9L);
        Long p_id=postsService.createPost(cpr,uid);
        Assertions.assertTrue(p_id>0L,"博客创建失败");
    }
//    更新博客
    @Test
    public void updatePostTest(){
        Long pid=1993961616386842624L;
        Long uid=977647889L;
        UpdatePostRequest upd = new UpdatePostRequest();
        upd.setCategory(null);
        upd.setTitle("update2");
        upd.setContent(null);
        upd.setTags(null);
        upd.setSummary(null);
        /**
         * 使用基本类型的包装类来封装
         * 当不修改此项数据修改时就可以设置为空
         * */
        upd.setStatus(null);
        postsService.updatePost(pid,upd,uid);
    }
//    列举符合条件的博客
    @Test
    public void queryBlogsTest(){

    }
//    列举热门博客
    @Test
    public void queryPopularPostsTest(){

    }
//    查看博客细节
    @Test
    public void getPostDetail(){

    }
    //    删除博客
    @Test
    public void deletePostTest(){

    }
}
