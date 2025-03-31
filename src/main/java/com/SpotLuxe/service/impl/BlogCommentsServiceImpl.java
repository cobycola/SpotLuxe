package com.SpotLuxe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.SpotLuxe.entity.BlogComments;
import com.SpotLuxe.mapper.BlogCommentsMapper;
import com.SpotLuxe.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
