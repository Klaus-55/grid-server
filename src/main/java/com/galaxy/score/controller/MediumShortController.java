package com.galaxy.score.controller;

import com.galaxy.score.common.Result;
import com.galaxy.score.service.MediumShortService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 中短期预报质量接口
 */
@Api(tags = "中短期预报质量评分接口")
@RestController
@RequestMapping("/zhongduan")
public class MediumShortController {

    @Autowired
    private MediumShortService mediumShortService;

    @ApiOperation("预警消息评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-03-31")
    })
    @GetMapping("/warningMessage/{start}/{end}")
    public Result warningMessage(@PathVariable String start, @PathVariable String end) {
        Map<String, Object> map = mediumShortService.warningMessage(start, end);
        return Result.success(map);
    }

    @ApiOperation("预警消息评定详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-03-31"),
            @ApiImplicitParam(name = "type", value = "预警类型", allowableValues = "all,暴雨,暴雪,寒潮,低温雨雪冰冻,高温,强对流,大雾,霾"),
            @ApiImplicitParam(name = "level", value = "预警等级", allowableValues = "all,蓝色,黄色,橙色,红色"),
            @ApiImplicitParam(name = "fdate", value = "起报时次", allowableValues = "08,20")
    })
    @GetMapping("/warningDetail/{start}/{end}/{type}/{level}/{fdate}")
    public Result warningDetail(@PathVariable String start, @PathVariable String end,
                                @PathVariable String type, @PathVariable String level, @PathVariable String fdate) {
        List<Map<String, Object>> list = mediumShortService.warningDetail(start, end, type, level, fdate);
        return Result.success(list);
    }

    @ApiOperation("降水过程评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-06-30")
    })
    @GetMapping("/rainProgress/{start}/{end}")
    public Result rainProgress(@PathVariable String start, @PathVariable String end) {
        Map<String, Object> map = mediumShortService.rainProgress(start, end);
        return Result.success(map);
    }

    @ApiOperation("降水过程评定详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-06-30")
    })
    @GetMapping("/rainDetail/{start}/{end}")
    public Result rainDetail(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = mediumShortService.rainDetail(start, end);
        return Result.success(list);
    }

    @ApiOperation("暴雨公众预报")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "检验年份", example = "2020"),
            @ApiImplicitParam(name = "product", value = "检验产品", allowableValues = "技巧评分,分项质量"),
            @ApiImplicitParam(name = "model", value = "检验模式", allowableValues = "中央台,湖南省气象台,地市"),
    })
    @GetMapping("/rainstormPublic/{year}/{product}/{model}")
    public Result rainstormPublic(@PathVariable String year, @PathVariable String product,
                                  @PathVariable String model) {
        Map<String, Object> map = mediumShortService.rainstormPublic(year, product, model);
        return Result.success(map);
    }

    @ApiOperation("暴雨公众预报详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-05-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-07-31")
    })
    @GetMapping("/rainstormDetail/{start}/{end}")
    public Result rainstormDetail(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = mediumShortService.rainstormDetail(start, end);
        return Result.success(list);
    }

    @ApiOperation("每日预报评分")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "start", value = "开始时间", example = "20210501"),
        @ApiImplicitParam(name = "end", value = "结束时间", example = "20210510"),
        @ApiImplicitParam(name = "fTime", value = "检验时段", example = "24"),
        @ApiImplicitParam(name = "rainType", value = "降水站点类型", example = "S99", allowableValues = "S99,S322,S421,S1912"),
        @ApiImplicitParam(name = "tempType", value = "温度站点类型", example = "S421", allowableValues = "S99,S322,S421,S1912"),
    })
    @GetMapping("/dailyForecast/{start}/{end}/{fTime}/{rainType}/{tempType}")
    public Result dailyForecast(@PathVariable String start, @PathVariable String end, @PathVariable String fTime,
                                @PathVariable String rainType, @PathVariable String tempType) {
        List<Map<String, Object>> list = mediumShortService.dailyForecast(start, end, fTime, rainType, tempType);
        return Result.success(list);
    }

    @ApiOperation("每日预报年度评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20201201"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211130"),
            @ApiImplicitParam(name = "fTime", value = "检验时段", example = "24")
    })
    @GetMapping("/getScoreByYear/{start}/{end}/{fTime}")
    public Result getScoreByYear(@PathVariable String start, @PathVariable String end, @PathVariable String fTime) {
        List<Map<String, Object>> list = mediumShortService.getScoreByYear(start, end, fTime);
        return Result.success(list);
    }

    @ApiOperation("天气公报评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210501"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20210510")
    })
    @GetMapping("/weatherPublic/{start}/{end}")
    public Result weatherPublic(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = mediumShortService.weatherPublic(start, end);
        return Result.success(list);
    }

    @ApiOperation("网格预报评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210701"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211030"),
            @ApiImplicitParam(name = "period", value = "检验时段", example = "24", allowableValues = "24,48,72,96,120,h72,h120"),
            @ApiImplicitParam(name = "obtType", value = "站点类型", example = "S99", allowableValues = "S99,S322,S421,S1912")
    })
    @GetMapping("/townForecastScore/{start}/{end}/{period}/{obtType}")
    public Result townForecastScore(@PathVariable String start, @PathVariable String end,
                                    @PathVariable String period, @PathVariable String obtType) {
        List<Map<String, Object>> list = mediumShortService.townForecastScore(start, end, period, obtType);
        return Result.success(list);
    }

    @ApiOperation("网格预报员评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-11-01"),
            @ApiImplicitParam(name = "period", value = "检验时段", example = "24", allowableValues = "24,48,72,96,120,h72,h120"),
            @ApiImplicitParam(name = "obtType", value = "站点类型", example = "S99", allowableValues = "S99,S322,S421,S1912"),
            @ApiImplicitParam(name = "wfsrc", value = "BFXK(湘潭市),BFJO(湘西自治州),BFDA(张家界市),BFYE(永州市),BFZU(株洲市),BFYY(益阳市),BFHW(怀化市),BFUY(岳阳市),BFSB(邵阳市),BFHA(衡阳市),BFCA(常德市),BFCE(郴州市),BFCS(长沙市),BFLD(娄底市)",
                    example = "BFXK", allowableValues = "BFXK,BFJO,BFDA,BFYE,BFZU,BFYY,BFHW,BFUY,BFSB,BFHA,BFCA,BFCE,BFCS,BFLD")
    })
    @GetMapping("/townForecasterScore/{start}/{end}/{period}/{obtType}/{wfsrc}")
    public Result townForecasterScore(@PathVariable String start, @PathVariable String end, @PathVariable String period,
                                      @PathVariable String obtType, @PathVariable String wfsrc) {
        List<Map<String, Object>> list = mediumShortService.townForecasterScore(start, end, period, obtType, wfsrc);
        return Result.success(list);
    }

    @ApiOperation("模式检验评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "年", example = "2020"),
            @ApiImplicitParam(name = "month", value = "检验月份，如果周期不为月份，则输入该周期内的最后一个月作为该字段的值", example = "7"),
            @ApiImplicitParam(name = "feHour", value = "检验时段", example = "0-72", allowableValues = "0-72,24,48,72"),
            @ApiImplicitParam(name = "type", value = "1:国家站,2:骨干站,3:自动站,4:格点", example = "1", allowableValues = "1,2,3,4"),
            @ApiImplicitParam(name = "rtc", value = "统计周期：1:月;2:季度;3:半年;4:全年", example = "1", allowableValues = "1,2,3,4"),
            @ApiImplicitParam(name = "item", value = "检验项", example = "分项质量", allowableValues = "分项质量,技巧评分")
    })
    @GetMapping("/modelScore/{year}/{month}/{feHour}/{type}/{rtc}/{item}")
    public Result modelScore(@PathVariable String year, @PathVariable String month,
                             @PathVariable String feHour, @PathVariable String type,
                             @PathVariable String rtc, @PathVariable String item) {
        Map<String, Object> map = mediumShortService.modelScore(year, month, feHour, type, rtc, item);
        return Result.success(map);
    }

    @ApiOperation("降水检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20200103"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20210103"),
            @ApiImplicitParam(name = "fTime", value = "预报时次", example = "zh", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH"),
//            @ApiImplicitParam(name = "wfhours", value = "检验时段", example = "72", allowableValues = "72,120")
    })
    @GetMapping("/rainScore/{start}/{end}/{fTime}/{type}")
    public Result rainScore(@PathVariable String start, @PathVariable String end,
                            @PathVariable String fTime, @PathVariable String type) {
        List<Map<String, Object>> list = mediumShortService.rainScore(start, end, fTime, type);
        return Result.success(list);
    }

    @ApiOperation("降水检验2")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20200103"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20210103"),
            @ApiImplicitParam(name = "fTime", value = "预报时次", example = "zh", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH")
    })
    @GetMapping("/rainScore2/{start}/{end}/{fTime}/{type}")
    public Result rainScore2(@PathVariable String start, @PathVariable String end,
                            @PathVariable String fTime, @PathVariable String type) {
        Map<String, Object> map = mediumShortService.rainScore2(start, end, fTime, type);
        return Result.success(map);
    }

    @ApiOperation("温度检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20200103"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20210103"),
            @ApiImplicitParam(name = "fTime", value = "预报时次", example = "zh", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH"),
            @ApiImplicitParam(name = "wfhours", value = "检验时段", example = "72", allowableValues = "72,120"),
            @ApiImplicitParam(name = "facname", value = "检验要素", example = "TMAX", allowableValues = "TMAX,TMIN"),
    })
    @GetMapping("/tempScore/{start}/{end}/{fTime}/{type}/{wfhours}/{facname}")
    public Result tempScore(@PathVariable String start, @PathVariable String end, @PathVariable String fTime,
                            @PathVariable String type, @PathVariable String wfhours, @PathVariable String facname) {
        List<Map<String, Object>> list = mediumShortService.tempScore(start, end, fTime, type, wfhours, facname);
        return Result.success(list);
    }

    @ApiOperation("站点实况数据获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ddatetime", value = "日期", example = "2021082608"),
            @ApiImplicitParam(name = "obtFacname", value = "要素", example = "tem_max_24h"),
            @ApiImplicitParam(name = "obtType", value = "站点类型", example = "and b.level = '1'")
    })
    @GetMapping("/getLiveObtData")
    public Result getLiveObtData(String ddatetime, String obtFacname, String obtType) {
        List<Map<String, Object>> list = mediumShortService.getLiveObtData(ddatetime, obtFacname, obtType);
        return Result.success(list);
    }

    @ApiOperation("站点实况数据获取（自行计算）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "begin_ddatetime", value = "开始日期", example = "2021082508"),
            @ApiImplicitParam(name = "cal_facname", value = "要素", example = "pre_1h"),
            @ApiImplicitParam(name = "cal_fun", value = "计算函数", example = "sum(pre_1h)"),
            @ApiImplicitParam(name = "ddatetime", value = "日期", example = "2021082608"),
            @ApiImplicitParam(name = "obtFacname", value = "要素", example = "pre_24h"),
            @ApiImplicitParam(name = "obtType", value = "站点类型", example = "and b.level = '1'")
    })
    @GetMapping("/getLiveObtData_cal")
    public Result getLiveObtData_cal(String begin_ddatetime, String cal_facname, String cal_fun, String ddatetime, String obtFacname, String obtType) {
        List<Map<String, Object>> list = mediumShortService.getLiveObtData_cal(begin_ddatetime, cal_facname, cal_fun, ddatetime, obtFacname, obtType);
        return Result.success(list);
    }

    @ApiOperation("请求格点预报数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "wfinterval", value = "预报频次", example = "24"),
            @ApiImplicitParam(name = "wfhour", value = "预报时次", example = "24"),
            @ApiImplicitParam(name = "facname", value = "要素", example = "TMAX"),
            @ApiImplicitParam(name = "wfsrc", value = "预报模式", example = "BECS"),
            @ApiImplicitParam(name = "wfdatetime", value = "预报时间", example = "202108230800"),
            @ApiImplicitParam(name = "datatype", value = "是否首席")
    })
    @GetMapping("/getWFGribData")
    public Result getWFGribData(String wfinterval, String wfhour, String facname, String wfsrc, String wfdatetime, String datatype) {
        List<Map<String, Object>> list = mediumShortService.getWFGribData(wfinterval, wfhour, facname, wfsrc, wfdatetime, datatype);
        return Result.success(list);
    }

    @ApiOperation("请求图表实况站点数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "obtFacname", value = "要素名称", example = "tem"),
            @ApiImplicitParam(name = "obtid", value = "站号", example = "57673"),
            @ApiImplicitParam(name = "startDatetime", value = "开始日期", example = "2021082308"),
            @ApiImplicitParam(name = "endDatetime", value = "结束日期", example = "2021082608")
    })
    @GetMapping("/chart_live_hour_obt")
    public Result getChartLiveHourObt(String obtFacname, String obtid, String startDatetime, String endDatetime) {
        List<Map<String, Object>> list = mediumShortService.getChartLiveHourObt(obtFacname, obtid, startDatetime, endDatetime);
        return Result.success(list);
    }

    @ApiOperation("请求图表实况站点数据（计算）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cal_fun", value = "计算方法", example = "max(tem_max)"),
            @ApiImplicitParam(name = "obtFacname", value = "要素名称", example = "tem_max_24h"),
            @ApiImplicitParam(name = "obtid", value = "站号", example = "57673"),
            @ApiImplicitParam(name = "startDatetime", value = "开始日期", example = "2021082308"),
            @ApiImplicitParam(name = "endDatetime", value = "结束日期", example = "2021082608")
    })
    @GetMapping("/chart_live_hour_obt_cal")
    public Result getChartLiveHourObtCal(String cal_fun, String obtFacname, String obtid, String startDatetime, String endDatetime) {
        List<Map<String, Object>> list = mediumShortService.getChartLiveHourObtCal(cal_fun, obtFacname, obtid, startDatetime, endDatetime);
        return Result.success(list);
    }

    @ApiOperation("请求图表格点实况数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "index", value = "站点对应的格点下标", example = "432"),
            @ApiImplicitParam(name = "table", value = "表名", example = "live.tb_grib_hourd", allowableValues = "live.tb_grib_hourd,live.tb_grib_dayd,"),
            @ApiImplicitParam(name = "facname", value = "要素名", example = "TEM"),
            @ApiImplicitParam(name = "startDatetime", value = "开始日期", example = "2021082308"),
            @ApiImplicitParam(name = "endDatetime", value = "结束日期", example = "2021082608")
    })
    @GetMapping("/chart_live_hour_grib")
    public Result getChartLiveHourObtCal(Integer index, String table, String facname, String startDatetime, String endDatetime) {
        List<Map<String, Object>> list = mediumShortService.getChartLiveHourGrib(index, table, facname, startDatetime, endDatetime);
        return Result.success(list);
    }

    @ApiOperation("请求图表格点预报数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "index", value = "站点对应的格点下标", example = "432"),
            @ApiImplicitParam(name = "facname", value = "要素名", example = "TMAX"),
            @ApiImplicitParam(name = "srcCode", value = "模式代码", example = "BECS"),
            @ApiImplicitParam(name = "wfdatetime", value = "预报日期", example = "2021082308")
    })
    @GetMapping("/chart_wf_grib")
    public Result getChartWfGrib(Integer index, String facname, String srcCode, String wfdatetime) {
        List<Map<String, Object>> list = mediumShortService.getChartWfGrib(index, facname, srcCode, wfdatetime);
        return Result.success(list);
    }

    @ApiOperation("格点实况数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ddatetime", value = "预报日期", example = "2021091111"),
            @ApiImplicitParam(name = "facname", value = "要素名", example = "TEM"),
            @ApiImplicitParam(name = "table", value = "实况表名", example = "live.tb_grib_hourd")
    })
    @GetMapping("/getLiveGribData")
    public Result getLiveGribData(String ddatetime, String facname, String table) {
        List<Map<String, Object>> list = mediumShortService.getLiveGribData(ddatetime, facname, table);
        return Result.success(list);
    }

    @ApiOperation("评分文件下载")
    @GetMapping("/downloadWord")
    public void downloadWord(String fileName, HttpServletResponse response) {
        mediumShortService.downloadWord(fileName, response);
    }
}
