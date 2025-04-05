package com.SpotLuxe.service;

import com.SpotLuxe.dto.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import com.SpotLuxe.entity.Blog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result queryMyBlog(Integer current);

    Result likeBlog(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogLikes(Long id);

    Result queryBlogByUserId(Long id, Integer current);

    Result queryBlogOfFollow(Long id,Integer offset);
}
