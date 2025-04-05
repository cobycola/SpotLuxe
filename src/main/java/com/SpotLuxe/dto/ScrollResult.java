package com.SpotLuxe.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScrollResult {
    // 数据
    private List<?> list;
    // 最小时间戳
    private long minTime;
    // 偏移量
    private Integer offset;
}
