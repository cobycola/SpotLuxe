package com.SpotLuxe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.SpotLuxe.entity.Blog;
import com.SpotLuxe.mapper.BlogMapper;
import com.SpotLuxe.service.IBlogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
