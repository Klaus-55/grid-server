package com.galaxy.score.controller;

import com.galaxy.score.common.Result;
import com.galaxy.score.service.ObjectiveTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * @Author hfr
 * @Date 2021/3/2 1:01
 */
@RestController
@RequestMapping("/keguan/test")
@Api(tags = "客观预报竞赛测试版接口")
public class ObjectiveTestController {
    @Autowired
    private ObjectiveTestService objectiveTestService;

    @ApiOperation("获取所有模式信息")
    @GetMapping("/getAllModels")
    public Result getAllModels() {
        List<Map<String, Object>> list = objectiveTestService.getAllModels();
        return Result.success(list);
    }

    @ApiOperation("降水评分校验")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "start", value = "开始时间", example = "202103050000"),
        @ApiImplicitParam(name = "end", value = "结束时间", example = "202103112300"),
        @ApiImplicitParam(name = "time", value = "起报时次", example = "08", allowableValues = "zh,08,20"),
        @ApiImplicitParam(name = "hour", value = "预报时次", example = "1"),
        @ApiImplicitParam(name = "interval", value = "预报间隔", example = "1"),
        @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH,57687")
    })
    @GetMapping("/checkRainScore/{start}/{end}/{time}/{hour}/{interval}/{type}")
    public Result checkRainScore(@PathVariable String start, @PathVariable String end,
                                 @PathVariable String time, @PathVariable String hour,
                                 @PathVariable String interval, @PathVariable String type) {
        List<Map<String, Object>> list = objectiveTestService.checkRainScore(start, end, time, hour, interval, type);
        return Result.success(list);
    }

    @ApiOperation("温度评分校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "202103050000"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "202103112300"),
            @ApiImplicitParam(name = "time", value = "起报时次", example = "08", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "hour", value = "预报时次", example = "1"),
            @ApiImplicitParam(name = "interval", value = "预报间隔", example = "1"),
            @ApiImplicitParam(name = "facname", value = "检验要素", example = "TMIN", allowableValues = "TMP,TMIN,TMAX"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH,57687")
    })
    @GetMapping("/checkTemScore/{start}/{end}/{time}/{hour}/{interval}/{facname}/{type}")
    public Result checkTemScore(@PathVariable String start, @PathVariable String end,
                                @PathVariable String time, @PathVariable String hour,
                                @PathVariable String interval, @PathVariable String facname, @PathVariable String type) {
        List<Map<String, Object>> list = objectiveTestService.checkTemScore(start, end, time, hour, interval, facname, type);
        return Result.success(list);
    }

    @ApiOperation("短时强降水和雷暴大风评分校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "202103050000"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "202103112300"),
            @ApiImplicitParam(name = "time", value = "起报时次", example = "08", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "hour", value = "预报时次", example = "1"),
            @ApiImplicitParam(name = "interval", value = "预报间隔", example = "1"),
            @ApiImplicitParam(name = "facname", value = "检验要素", example = "RAT", allowableValues = "RAT,SMG"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH,57687")
    })
    @GetMapping("/checkHeavyScore/{start}/{end}/{time}/{hour}/{interval}/{facname}/{type}")
    public Result checkHeavyScore(@PathVariable String start, @PathVariable String end,
                                @PathVariable String time, @PathVariable String hour,
                                @PathVariable String interval, @PathVariable String facname, @PathVariable String type) {
        List<Map<String, Object>> list = objectiveTestService.checkHeavyScore(start, end, time, hour, interval, facname, type);
        return Result.success(list);
    }

    @ApiOperation("降水逐日评分校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "202103050000"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "202103112300"),
            @ApiImplicitParam(name = "time", value = "起报时次", example = "08", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "interval", value = "预报间隔", example = "1"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH,57687")
    })
    @GetMapping("/checkRainScore/{start}/{end}/{time}/{interval}/{type}")
    public Result checkRainDayByDay(@PathVariable String start, @PathVariable String end,
                                    @PathVariable String time, @PathVariable String interval,
                                    @PathVariable String type) {
        List<Map<String, Object>> list = objectiveTestService.checkRainDayByDay(start, end, time, interval, type);
        return Result.success(list);
    }

    @ApiOperation("温度逐日评分校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "202103050000"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "202103112300"),
            @ApiImplicitParam(name = "time", value = "起报时次", example = "08", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "interval", value = "预报间隔", example = "1"),
            @ApiImplicitParam(name = "facname", value = "检验要素", example = "TMIN", allowableValues = "TMP,TMIN,TMAX"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH,57687")
    })
    @GetMapping("/checkTemScore/{start}/{end}/{time}/{interval}/{facname}/{type}")
    public Result checkTemDayByDay(@PathVariable String start, @PathVariable String end,
                                @PathVariable String time, @PathVariable String interval,
                                @PathVariable String facname, @PathVariable String type) {
        List<Map<String, Object>> list = objectiveTestService.checkTemDayByDay(start, end, time, interval, facname, type);
        return Result.success(list);
    }

    @ApiOperation("短时强降水和雷暴大风逐日评分校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "202103050000"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "202103112300"),
            @ApiImplicitParam(name = "time", value = "起报时次", example = "08", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "interval", value = "预报间隔", example = "1"),
            @ApiImplicitParam(name = "facname", value = "检验要素", example = "RAT", allowableValues = "RAT,SMG"),
            @ApiImplicitParam(name = "type", value = "检验产品", example = "BBBUSI", allowableValues = "BBBUSI,TECH,57687")
    })
    @GetMapping("/checkHeavyScore/{start}/{end}/{time}/{interval}/{facname}/{type}")
    public Result checkHeavyDayByDay(@PathVariable String start, @PathVariable String end,
                                     @PathVariable String time, @PathVariable String interval,
                                     @PathVariable String facname, @PathVariable String type) {
        List<Map<String, Object>> list = objectiveTestService.checkHeavyDayByDay(start, end, time, interval, facname, type);
        return Result.success(list);
    }
}
