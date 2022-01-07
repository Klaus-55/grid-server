package com.galaxy.score.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 短临预报质量评分dao层
 */
public interface ShortApproachMapper extends BaseMapper {

    List<Map<String, Object>> getProvincialAll(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType, @Param("method") String method);

    List<Map<String, Object>> getProvincialAllZh(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType, @Param("method") String method);

    List<Map<String, Object>> getProvincialAllByType(@Param("start") String start, @Param("end") String end, @Param("method") String method);

    List<Map<String, Object>> getProvincialAllZhByType(@Param("start") String start, @Param("end") String end, @Param("method") String method);

    List<Map<String,Object>> getProvincialFj(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType);

    List<Map<String, Object>> getProvincialFjZhByLevel(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType);

    List<Map<String, Object>> getProvincialFjByType(@Param("start") String start, @Param("end") String end);

    List<Map<String, Object>> getProvincialFjZhByTypeLevel(@Param("start") String start, @Param("end") String end);

    //省级预警评定详情
    List<Map<String, Object>> provincialDetail(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType,
                                               @Param("level") String level, @Param("rs") String rs, @Param("type") String type);

    List<Map<String, Object>> getCityWarningAll(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType,
                                                @Param("method") String method,@Param("filed") String filed);

    List<Map<String, Object>> getCityWarningAllZh(@Param("start") String start, @Param("end") String end, @Param("method") String method, @Param("filed") String filed);

    List<Map<String, Object>> getCityWarningFj(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType, @Param("filed") String filed);

    List<Map<String, Object>> getZhByLevel(@Param("start") String start, @Param("end") String end, @Param("warningType") String warningType, @Param("filed") String filed);

    List<Map<String, Object>> getCityWarningFjZh(@Param("start") String start, @Param("end") String end, @Param("filed") String filed);

    List<Map<String, Object>> getZhByTypeLevel(@Param("start") String start, @Param("end") String end, @Param("filed") String filed);

    //预警有效性评定
    List<Map<String,Object>> getCityWarningEff(@Param("start") String start, @Param("end") String end,
                                               @Param("warningType") String warningType, @Param("field") String department);

    List<Map<String,Object>> getDistrictsWarningEff(@Param("start") String start, @Param("end") String end,
                                                    @Param("warningType") String warningType, @Param("area") String area);

    List<Map<String, Object>> cityDetail(@Param("start") String start, @Param("end") String end,
                                         @Param("department") String department, @Param("warningType") String warningType,
                                         @Param("level") String level, @Param("rs") String rs, @Param("type") String type);

    List<Map<String, Object>> cityDetailEff(@Param("start") String start, @Param("end") String end, @Param("department") String department,
                                            @Param("warningType") String warningType, @Param("level") String level);

    List<Map<String, Object>> rainScore(@Param("start") String start, @Param("end") String end, @Param("ftime") String ftime,
                                        @Param("wfinterval") String wfinterval, @Param("type") String product);

    List<Map<String, Object>> temScore(@Param("start") String start, @Param("end") String end, @Param("ftime") String ftime,
                                       @Param("wfinterval") String wfinterval, @Param("type") String product);

    @DS("master")
    List<Map<String, Object>> getModelName();

    List<Map<String, Object>> getHeavyMonitorAll(@Param("start") String start, @Param("end") String end, @Param("filed") String filed);

    List<Map<String, Object>> getHeavyMonitorByArea(@Param("start") String start, @Param("end") String end, @Param("filed") String filed);

    List<Map<String, Object>> getHeavyMonitorByDistrict(@Param("start") String start, @Param("end") String end, @Param("area") String area, @Param("filed") String filed);

    List<Map<String,Object>> getHeavyMonitorByCountry(@Param("start") String start, @Param("end") String end, @Param("area") String area);

    List<Map<String, Object>> heavyDetail(@Param("start") String start, @Param("end") String end, @Param("area") String area, @Param("district") String district, @Param("level") String level);

    List<Map<String,Object>> getHeavyDistrict();

    List<Map<String, Object>> getHeavyRainScore(@Param("start") String start, @Param("end") String end, @Param("ftime") String ftime);

    List<Map<String, Object>> getThunderScore(@Param("start") String start, @Param("end") String end, @Param("ftime") String ftime);

    List<Map<String, Object>> getThunderstormScore(@Param("start") String start, @Param("end") String end, @Param("ftime") String ftime);

    List<Map<String,Object>> getHailScore(@Param("start") String start, @Param("end") String end, @Param("ftime") String ftime);

}
