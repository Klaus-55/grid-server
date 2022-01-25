package com.galaxy.score.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 中短期预报质评分dao层
 */
public interface MediumShortMapper extends BaseMapper {

    //预警消息评分
    List<Map<String, Object>> warningMessageScore(@Param("start") String start, @Param("end") String end);

    //预警消息评定详情
    List<Map<String, Object>> getWarningDetail(@Param("start") String start, @Param("end") String end,
                                              @Param("type") String type, @Param("level") String level, @Param("fdate") String fdate);

    //降水过程评分
    List<Map<String, Object>> getRainProgress(@Param("start") String start, @Param("end") String end);

    //降水过程评定详情
    List<Map<String, Object>> getRainDetail(@Param("start") String start, @Param("end") String end);

    //暴雨公众预报
    List<Map<String, Object>> getRainstormPublic(@Param("year") String year, @Param("department") String model);

    //暴雨公众预报详情
    List<Map<String, Object>> getRainstormDetail(@Param("start") String start, @Param("end") String end);

    //每日预报评分
    List<Map<String, Object>> dailyForecastScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour,
                                                 @Param("rainType") String rainType, @Param("tempType") String tempType);

    //天气公报
    List<Map<String, Object>> getWeatherPublic(@Param("start") String start, @Param("end") String end);

    //城镇预报评分
    List<Map<String, Object>> townForecastScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour, @Param("obtType") String obtType);

    //城镇预报评分（省台结果）
    List<Map<String, Object>> townForecastScoreSt(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour, @Param("obtType") String obtType);

    //城镇预报员评分
    List<Map<String, Object>> townForecasterScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour,
                                                  @Param("obtType") String obtType, @Param("wfsrc") String wfsrc, @Param("department") String cityName);

    //城镇预报员评分（省台结果）
    List<Map<String, Object>> townForecasterScoreSt(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour,
                                                   @Param("obtType") String obtType, @Param("wfsrc") String wfsrc, @Param("department") String cityName);

    List<Map<String, Object>> getModelScore(@Param("year") String year, @Param("month") String month,
                                           @Param("feHour") String feHour, @Param("type") String type,
                                           @Param("rtc") String rtc);

    List<Map<String, Object>> rainScore(@Param("start") String start, @Param("end") String end,
                                        @Param("fTime") String fTime, @Param("type") String type);

    List<Map<String, Object>> rainScore2(@Param("start") String start, @Param("end") String end, @Param("fTime") String fTime, @Param("type") String type);

    List<Map<String, Object>> tempScore(@Param("start") String start, @Param("end") String end, @Param("fTime") String fTime,
                                       @Param("type") String type, @Param("wfhours") String wfhours, @Param("facname") String facname);

    List<Map<String, Object>> getLiveObtData(@Param("ddatetime") String ddatetime, @Param("obtFacname") String obtFacname, @Param("obtType") String obtType);

    List<Map<String,Object>> getLiveObtData_cal(@Param("begin_ddatetime") String begin_ddatetime, @Param("cal_facname") String cal_facname, @Param("cal_fun") String cal_fun,
                                                @Param("ddatetime") String ddatetime, @Param("obtFacname") String obtFacname, @Param("obtType") String obtType);


    List<Map<String, Object>> getWFGribData(@Param("wfinterval") String wfinterval, @Param("wfhour") String wfhour, @Param("facname") String facname,
                                            @Param("wfsrc") String wfsrc, @Param("wfdatetime") String wfdatetime, @Param("datatype") String datatype);

    List<Map<String, Object>> getChartLiveHourObt(@Param("obtFacname") String obtFacname, @Param("obtid") String obtid,
                                                  @Param("startDatetime") String startDatetime, @Param("endDatetime") String endDatetime);

    List<Map<String, Object>> getChartLiveHourObtCal(@Param("cal_fun") String cal_fun, @Param("obtFacname") String obtFacname, @Param("obtid") String obtid,
                                                     @Param("startDatetime") String startDatetime,@Param("endDatetime")  String endDatetime);

    List<Map<String,Object>> getChartLiveHourGrib(@Param("index") Integer index, @Param("table") String table, @Param("facname") String facname,
                                                  @Param("startDatetime") String startDatetime, @Param("endDatetime") String endDatetime);

    List<Map<String,Object>> getChartWfGrib(@Param("index") Integer index, @Param("facname") String facname,
                                            @Param("srcCode") String srcCode, @Param("wfdatetime") String wfdatetime);

    List<Map<String,Object>> getLiveGribData(@Param("ddatetime") String ddatetime, @Param("facname") String facname, @Param("table") String table);

}
