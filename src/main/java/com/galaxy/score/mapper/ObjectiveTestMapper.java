package com.galaxy.score.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author hfr
 * @Date 2021/3/2 10:30
 */
public interface ObjectiveTestMapper extends BaseMapper {

    List<Map<String, Object>> getAllModels();

    //降水结果获取
    List<Map<String, Object>> checkRainScore(@Param("tableName") String tableName, @Param("start") String start,
                                             @Param("end") String end, @Param("time") String time,
                                             @Param("hour") String hour, @Param("interval") String interval,
                                             @Param("type") String type);

    //降水综合
    List<Map<String,Object>> checkRainZh(@Param("tableName") String tableName, @Param("start") String start,
                                         @Param("end") String end, @Param("time") String time,
                                         @Param("hour") String hour, @Param("interval") String interval,
                                         @Param("type") String type);

    //温度评分结果获取
    List<Map<String,Object>> checkTemScore(@Param("tableName") String tableName, @Param("start") String start,
                                           @Param("end") String end, @Param("time") String time,
                                           @Param("hour") String hour, @Param("interval") String interval,
                                           @Param("facname") String facname, @Param("type") String type);

    //温度综合
    List<Map<String,Object>> checkTemZh(@Param("tableName") String tableName, @Param("start") String start,
                                        @Param("end") String end, @Param("time") String time,
                                        @Param("hour") String hour, @Param("interval") String interval,
                                        @Param("facname") String facname, @Param("type") String type);

    //短时强降水和雷暴大风评分结果获取
    List<Map<String,Object>> checkHeavyScore(@Param("tableName") String tableName, @Param("start") String start,
                                             @Param("end") String end, @Param("time") String time,
                                             @Param("hour") String hour, @Param("interval") String interval,
                                             @Param("facname") String facname, @Param("type") String type);

    //短时强降水和雷暴大风评分结果获取
    List<Map<String,Object>> checkHeavyZh(@Param("tableName") String tableName, @Param("start") String start,
                                          @Param("end") String end, @Param("time") String time,
                                          @Param("hour") String hour, @Param("interval") String interval,
                                          @Param("facname") String facname, @Param("type") String type);

    //逐日检验
    List<Map<String, Object>> checkRainDayByDay(@Param("tableName") String tableName, @Param("start") String start,
                                                @Param("end") String end, @Param("time") String time,
                                                @Param("interval") String interval, @Param("type") String type);

    List<Map<String,Object>> checkTemDayByDay(@Param("tableName") String tableName, @Param("start") String start,
                                              @Param("end") String end, @Param("time") String time,
                                              @Param("interval") String interval, @Param("facname") String facname,
                                              @Param("type") String type);

    List<Map<String,Object>> checkHeavyDayByDay(@Param("tableName") String tableName, @Param("start") String start,
                                                @Param("end") String end, @Param("time") String time,
                                                @Param("interval") String interval, @Param("facname") String facname,
                                                @Param("type") String type);
}
