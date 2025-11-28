package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.AwesomeCsApplication;
import com.yorozuya.awesomecs.model.domain.PostLikes;
import com.yorozuya.awesomecs.model.domain.Posts;
import com.yorozuya.awesomecs.model.request.CreatePostRequest;
import com.yorozuya.awesomecs.model.request.UpdatePostRequest;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;
import com.yorozuya.awesomecs.repository.mapper.PostLikesMapper;
import com.yorozuya.awesomecs.repository.mapper.PostsMapper;
import com.yorozuya.awesomecs.service.PostsService;
import jakarta.annotation.Resource;
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

    @Resource
    PostsMapper postsMapper;

    @Resource
    PostLikesMapper postLikesMapper;

//    生成用户id的函数
    private Long generate(int len){
        Random random = new SecureRandom();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<len;i++){
            sb.append(random.nextInt(10));
        }
        return Long.parseLong(sb.toString());
    }

//    从已有的博客列表中随机挑选一个博客加入点赞列表
    private boolean clickLike(int count){
        int success=0;
        //获取所有的博客列表
        List<Posts> posts=postsMapper.selectList(null);
        int len=posts.size();
        for(int i=0;i<count;i++){
            int selectedIndex=new Random().nextInt(len);
            PostLikes ps=new PostLikes();
            ps.setPostId(posts.get(selectedIndex).getId());//从所有博客中随机选取一个
            ps.setUserId(generate(9));//随机产生一个用户
            success+=postLikesMapper.insert(ps);
        }
        return success==count;
    }
//    创建博客
    @Test
    public void createPostTest(){
        List<String> tags = new ArrayList<>();
        tags.add(null);
        CreatePostRequest cpr = new CreatePostRequest();
        cpr.setCategory(null);
        cpr.setTitle("test");

        cpr.setTags(tags);
        cpr.setSummary(null);
        cpr.setStatus(1);
        Long uid=generate(9);
        for(int i=0;i<9;i++){
            cpr.setContent("test"+i);
            Long p_id=postsService.createPost(cpr,generate(9));
            Assertions.assertTrue(p_id>0L,"博客创建失败");
        }


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
        Integer page=1;
        Integer pageSize=10;
//      无特殊查询
        PageResponse<PostSummaryResponse> p= postsService.listPosts(page,pageSize,null,null,null);
        Assertions.assertTrue(p.getList().size()>1,"查询结果错误1");
        p= postsService.listPosts(page,pageSize,"test",null,null);
        Assertions.assertTrue(p.getList().size()>1,"查询结果错误2");
        p= postsService.listPosts(page,pageSize,null,"test",null);
        Assertions.assertTrue(p.getList().size()==1,"查询结果错误3");
        p= postsService.listPosts(page,pageSize,null,null,"test");
        Assertions.assertTrue(p.getList().size()==1,"查询结果错误4");
        p= postsService.listPosts(page,pageSize,"test","test","test");
        Assertions.assertTrue(p.getList().size()==1,"查询结果错误5");
    }
//    列举热门博客
    @Test
    public void queryPopularPostsTest(){
//        Assertions.assertTrue(clickLike(100)==true,"点赞失败");
        Integer page=1;
        Integer pageSize=10;
        PageResponse<PostSummaryResponse> p= postsService.listPopularPosts(page,pageSize);
        for(PostSummaryResponse ps:p.getList()){
            System.out.println(ps);
        }
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
