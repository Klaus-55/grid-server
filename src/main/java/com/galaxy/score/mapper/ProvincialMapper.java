package com.galaxy.score.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * @Author hfr
 * @Date 2021/12/6 15:11
 */
public interface ProvincialMapper extends BaseMapper {

    List<Map<String, Object>> getChiefForecasters(@Param("start") String start, @Param("end") String end);

    @DS("fquality")
    Double getWarningMessage(@Param("start") String start, @Param("end") String end, @Param("forecaster") String forecaster);

    Map<String, Object> getWeatherPublic(@Param("time") String time);

    @DS("fquality")
    Double getRainProgress(@Param("start") String start, @Param("end") String end, @Param("forecaster") String forecaster);

    List<Map<String, Object>> getForemanScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    List<Map<String, Object>> getForemanScoreYt(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    List<Map<String, Object>> getNightShiftScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    List<Map<String, Object>> getNightShiftScoreYt(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    @DS("duanlin")
    List<Map<String, Object>> getShortTermScore(@Param("start") String start, @Param("end") String end);

    Map<String, Object> getObjectiveScore(@Param("start") String start, @Param("end") String end);

    List<Map<String, Object>> getCityScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    List<Map<String, Object>> getCityScoreSt(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    @DS("duanlin")
    List<Map<String, Object>> getCityWarningScore(@Param("start") String start, @Param("end") String end);

    @DS("duanlin")
    List<Map<String,Object>> getCityHeavyScore(@Param("start") String start, @Param("end") String end);

    List<Map<String, Object>> getForecasterScore(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    List<Map<String,Object>> getForecasterScore2(@Param("gridStart") String gridStart, @Param("gridEnd") String gridEnd, @Param("bcStart") String bcStart,
                                                 @Param("bcEnd") String bcEnd, @Param("wfhour") String wfhour);

    List<Map<String, Object>> getForecasterScoreSt(@Param("start") String start, @Param("end") String end, @Param("wfhour") String wfhour);

    List<Map<String,Object>> getForecasterScoreSt2(@Param("gridStart") String gridStart, @Param("gridEnd") String gridEnd, @Param("bcStart") String bcStart,
                                                    @Param("bcEnd") String bcEnd, @Param("wfhour") String wfhour);

    @DS("duanlin")
    List<Map<String, Object>> getForecasterWarningZh(@Param("start") String start, @Param("end") String end);

}
