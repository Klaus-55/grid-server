package com.galaxy.score.controller;

import com.galaxy.score.common.Result;
import com.galaxy.score.service.ProvincialService;
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
 中短期预报质量接口
 */
@Api(tags = "省级预报竞赛")
@RestController
@RequestMapping("/provincial")
public class ProvincialController {

    @Autowired
    private ProvincialService provincialService;

    @ApiOperation("首席岗预报员评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210101"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211118"),
    })
    @GetMapping("/getChiefScore/{start}/{end}")
    public Result getChiefScore(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = provincialService.getChiefScore(start, end);
        return Result.success(list);
    }

    @ApiOperation("中短期领班预报员评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210101"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211118"),
    })
    @GetMapping("/getForemanScore/{start}/{end}")
    public Result getForemanScore(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = provincialService.getForemanScore(start, end);
        return Result.success(list);
    }

    @ApiOperation("中短期夜班预报员评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210101"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211118"),
    })
    @GetMapping("/getNightShiftScore/{start}/{end}")
    public Result getNightShiftScore(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = provincialService.getNightShiftScore(start, end);
        return Result.success(list);
    }

    @ApiOperation("短临岗预报员评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-12-01"),
    })
    @GetMapping("/getShortTermScore/{start}/{end}")
    public Result getShortTermScore(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = provincialService.getShortTermScore(start, end);
        return Result.success(list);
    }

    @ApiOperation("客观预报岗评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210101"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211201"),
    })
    @GetMapping("/getObjectiveScore/{start}/{end}")
    public Result getObjectiveScore(@PathVariable String start, @PathVariable String end) {
        Map<String, Object> map = provincialService.getObjectiveScore(start, end);
        return Result.success(map);
    }

    @ApiOperation("市级评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210101"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20211201"),
    })
    @GetMapping("/getCityScore/{start}/{end}")
    public Result getCityScore(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = provincialService.getCityScore(start, end);
        return Result.success(list);
    }

    @ApiOperation("市级评分2")
    @GetMapping("/getCityScore2")
    public Result getCityScore2() {
        List<Map<String, Object>> list = provincialService.getCityScore2();
        return Result.success(list);
    }

    @ApiOperation("市级预报员评分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-12-01"),
    })
    @GetMapping("/getForecasterScore/{start}/{end}")
    public Result getForecasterScore(@PathVariable String start, @PathVariable String end) {
        List<Map<String, Object>> list = provincialService.getForecasterScore(start, end);
        return Result.success(list);
    }

    @ApiOperation("市级预报员评分2")
    @GetMapping("/getForecasterScore2")
    public Result getForecasterScore2() {
        List<Map<String, Object>> list = provincialService.getForecasterScore2();
        return Result.success(list);
    }
}
