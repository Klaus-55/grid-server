package com.galaxy.score.common;

import lombok.Data;

import java.util.List;

/**
 图表数据列内容
 */
@Data
public class SeriesItem {
    private String name;
    private List<Double> data;
}
