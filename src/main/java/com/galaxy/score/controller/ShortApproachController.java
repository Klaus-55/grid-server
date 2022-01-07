package com.galaxy.score.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galaxy.score.common.Result;
import com.galaxy.score.model.Attendance;
import com.galaxy.score.service.ShortApproachService;
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
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 短临预报质量评分接口
 */
@Api(tags = "短临预报质量评分接口")
@RestController
@RequestMapping("/duanlin")
public class ShortApproachController {

    @Autowired
    private ShortApproachService shortApproachService;

    @ApiOperation("登陆验证")
    @GetMapping("/login")
    public void login(HttpServletRequest request, String username, String password) {
        HttpSession session = request.getSession();

        session.setAttribute("username", "55555");
    }

    @ApiOperation("降水检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210701"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20210731"),
            @ApiImplicitParam(name = "ftime", value = "预报时次", example = "08", allowableValues = "zh,08,14,20"),
            @ApiImplicitParam(name = "wfinterval", value = "检验时效", example = "3", allowableValues = "1,3"),
            @ApiImplicitParam(name = "product", value = "检验产品（BBBUSI:业务评估;TECH:技术评估）", example = "BBBUSI", allowableValues = "BBBUSI,TECH"),
//            @ApiImplicitParam(name = "facname", value = "检验要素", example = "ME", allowableValues = "ME,MAE,PC,k1,k2,k3,zh"),
//            @ApiImplicitParam(name = "fhour", value = "检验时段", example = "3,6,9,12")
    })
    @GetMapping("/rainScore/{start}/{end}/{ftime}/{wfinterval}/{product}")
    public Result rainScore(@PathVariable String start, @PathVariable String end, @PathVariable String ftime,
                            @PathVariable String wfinterval, @PathVariable String product) {
        List<Map<String, Object>> list = shortApproachService.rainScore(start, end, ftime, wfinterval, product);
        return Result.success(list);
    }

    @ApiOperation("温度检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "20210101"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "20210430"),
            @ApiImplicitParam(name = "ftime", value = "预报时次", example = "08", allowableValues = "zh,08,20"),
            @ApiImplicitParam(name = "wfinterval", value = "检验时效", example = "3", allowableValues = "1,3"),
            @ApiImplicitParam(name = "product", value = "检验产品（BBBUSI:业务评估;TECH:技术评估）", example = "BBBUSI", allowableValues = "BBBUSI,TECH")
    })
    @GetMapping("/temScore/{start}/{end}/{ftime}/{wfinterval}/{product}")
    public Result temScore(@PathVariable String start, @PathVariable String end, @PathVariable String ftime,
                           @PathVariable String wfinterval, @PathVariable String product) {
        List<Map<String, Object>> list = shortApproachService.temScore(start, end, ftime, wfinterval, product);
        return Result.success(list);
    }

    @ApiOperation("分页查询值班信息")
    @GetMapping("/attendanceList")
    public Result getAttendanceList(Page<Attendance> page) {
        page = shortApproachService.findList(page);
        return Result.success(page);
    }

    @ApiOperation("强降水检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-07-31"),
            @ApiImplicitParam(name = "ftime", value = "预报时次", example = "08", allowableValues = "zh,08,20")
    })
    @GetMapping("/heavyRainScore/{start}/{end}/{ftime}")
    public Result heavyRainScore(@PathVariable String start, @PathVariable String end, @PathVariable String ftime) {
        List<Map<String, Object>> list = shortApproachService.heavyRainScore(start, end, ftime);
        return Result.success(list);
    }

    @ApiOperation("雷暴检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-07-31"),
            @ApiImplicitParam(name = "ftime", value = "预报时次", example = "08", allowableValues = "zh,08,20")
    })
    @GetMapping("/thunderScore/{start}/{end}/{ftime}")
    public Result thunderScore(@PathVariable String start, @PathVariable String end, @PathVariable String ftime) {
        List<Map<String, Object>> list = shortApproachService.thunderScore(start, end, ftime);
        return Result.success(list);
    }

    @ApiOperation("雷暴大风检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-07-31"),
            @ApiImplicitParam(name = "ftime", value = "预报时次", example = "08", allowableValues = "zh,08,20")
    })
    @GetMapping("/thunderstormScore/{start}/{end}/{ftime}")
    public Result thunderstormScore(@PathVariable String start, @PathVariable String end, @PathVariable String ftime) {
        List<Map<String, Object>> list = shortApproachService.thunderstormScore(start, end, ftime);
        return Result.success(list);
    }

    @ApiOperation("冰雹检验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-07-31"),
            @ApiImplicitParam(name = "ftime", value = "预报时次", example = "08", allowableValues = "zh,08,20")
    })
    @GetMapping("/hailScore/{start}/{end}/{ftime}")
    public Result hailScore(@PathVariable String start, @PathVariable String end, @PathVariable String ftime) {
        List<Map<String, Object>> list = shortApproachService.hailScore(start, end, ftime);
        return Result.success(list);
    }

    @ApiOperation("省级预警预报质量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-11-18"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹,综合"),
            @ApiImplicitParam(name = "method", value = "检验方法", example = "all", allowableValues = "all,fj,bfj")
    })
    @GetMapping("/provincialWarning/{start}/{end}/{warningType}/{method}")
    public Result provincialWarning(@PathVariable String start, @PathVariable String end,
                                    @PathVariable String warningType, @PathVariable String method) {
        Map<String, Object> map = shortApproachService.provincialWarning(start, end, warningType, method);
        return Result.success(map);
    }

    @ApiOperation("省级预警评定详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-06-30"),
//            @ApiImplicitParam(name = "department", value = "发布单位", example = "所有气象台",
//                    allowableValues = "所有气象台,湖南省气象台,长沙市气象台,株洲市气象台,湘潭市气象台,衡阳市气象台,邵阳市气象台,岳阳市气象台,常德市气象台,张家界市气象台,益阳市气象台,郴州市气象台,永州市气象台,怀化市气象台,娄底市气象台,湘西州气象台"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹"),
            @ApiImplicitParam(name = "level", value = "预警等级", example = "所有等级", allowableValues = "所有等级,蓝色,黄色,橙色,红色"),
            @ApiImplicitParam(name = "rs", value = "检验结果", example = "所有结果", allowableValues = "所有结果,正确,空报,漏报"),
            @ApiImplicitParam(name = "type", value = "检验类型", example = "1", allowableValues = "1,0")
    })
    @GetMapping("/provincialDetail/{start}/{end}/{warningType}/{level}/{rs}/{type}")
    public Result provincialDetail(@PathVariable String start, @PathVariable String end, @PathVariable String warningType,
                                   @PathVariable String level, @PathVariable String rs, @PathVariable String type) {
        List<Map<String, Object>> list = shortApproachService.provincialDetail(start, end, warningType, level, rs, type);
        return Result.success(list);
    }

    @ApiOperation("市级预警预报质量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-06-30"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹,暴雪,大风,大雾,霾,综合"),
            @ApiImplicitParam(name = "method", value = "检验方法", example = "fj", allowableValues = "all,fj,bfj")
    })
    @GetMapping("/cityWarning/{start}/{end}/{warningType}/{method}")
    public Result cityWarning(@PathVariable String start, @PathVariable String end,
                              @PathVariable String warningType, @PathVariable String method) {
        Map<String, Object> map = shortApproachService.cityWarning(start, end, warningType, method);
        return Result.success(map);
    }

    @ApiOperation("市级预警评定详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2020-04-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2020-06-30"),
            @ApiImplicitParam(name = "department", value = "发布单位", example = "所有气象台",
                    allowableValues = "所有气象台,湖南省气象台,长沙市气象台,株洲市气象台,湘潭市气象台,衡阳市气象台,邵阳市气象台,岳阳市气象台,常德市气象台,张家界市气象台,益阳市气象台,郴州市气象台,永州市气象台,怀化市气象台,娄底市气象台,湘西州气象台"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹,暴雪,大风,大雾,霾"),
            @ApiImplicitParam(name = "level", value = "预警等级", example = "所有等级", allowableValues = "所有等级,蓝色,黄色,橙色,红色"),
            @ApiImplicitParam(name = "rs", value = "检验结果", example = "所有结果", allowableValues = "所有结果,正确,空报,漏报"),
            @ApiImplicitParam(name = "type", value = "检验类型", example = "1", allowableValues = "1,0")
    })
    @GetMapping("/cityDetail/{start}/{end}/{department}/{warningType}/{level}/{rs}/{type}")
    public Result cityDetail(@PathVariable String start, @PathVariable String end,
                             @PathVariable String department, @PathVariable String warningType,
                             @PathVariable String level, @PathVariable String rs, @PathVariable String type) {
        List<Map<String, Object>> list = shortApproachService.cityDetail(start, end, department, warningType, level, rs, type);
        return Result.success(list);
    }

    @ApiOperation("预警有效性评定")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-05-31"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹,暴雪,大风,大雾,霾")
    })
    @GetMapping("/cityWarningEff/{start}/{end}/{warningType}")
    public Result cityWarningEff(@PathVariable String start, @PathVariable String end, @PathVariable String warningType) {
        Map<String, Object> map = shortApproachService.cityWarningEff(start, end, warningType);
        return Result.success(map);
    }

    @ApiOperation("地级市各区县的有效性评定")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-05-31"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹,暴雪,大风,大雾,霾,综合"),
            @ApiImplicitParam(name = "area", value = "地级市", example = "长沙市")
    })
    @GetMapping("/districtWarningEff/{start}/{end}/{warningType}/{area}")
    public Result districtWarningEff(@PathVariable String start, @PathVariable String end,
                                     @PathVariable String warningType, @PathVariable String area) {
        Map<String, Object> map = shortApproachService.districtWarningEff(start, end, warningType, area);
        return Result.success(map);
    }

    @ApiOperation("预警有效性评定详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-05-31"),
            @ApiImplicitParam(name = "department", value = "发布单位", example = "所有气象台",
                    allowableValues = "所有气象台,湖南省气象台,长沙市气象台,株洲市气象台,湘潭市气象台,衡阳市气象台,邵阳市气象台,岳阳市气象台,常德市气象台,张家界市气象台,益阳市气象台,郴州市气象台,永州市气象台,怀化市气象台,娄底市气象台,湘西州气象台"),
            @ApiImplicitParam(name = "warningType", value = "预警类型", example = "暴雨", allowableValues = "暴雨,雷雨大风,雷电,冰雹,暴雪,大风,大雾,霾"),
            @ApiImplicitParam(name = "level", value = "预警等级", example = "所有等级", allowableValues = "所有等级,蓝色,黄色,橙色,红色")
    })
    @GetMapping("/cityDetailEff/{start}/{end}/{department}/{warningType}/{level}")
    public Result cityDetailEff(@PathVariable String start, @PathVariable String end,
                                @PathVariable String department, @PathVariable String warningType,
                                @PathVariable String level) {
        List<Map<String, Object>> list = shortApproachService.cityDetailEff(start, end, department, warningType, level);
        return Result.success(list);
    }

    @ApiOperation("强降水监测警报")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-05-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-08-01"),
            @ApiImplicitParam(name = "area", value = "地区", example = "长沙市", allowableValues = "长沙市,株洲市,湘潭市,衡阳市,岳阳市,张家界市,娄底市,郴州市,常德市,益阳市,邵阳市,永州市,怀化市,湘西州"),
            @ApiImplicitParam(name = "regLevel", value = "地区级别（市级、区县、乡镇）", example = "area", allowableValues = "area,district,country")
    })
    @GetMapping("/heavyRainMonitor/{start}/{end}/{area}/{regLevel}")
    public Result heavyRainMonitor(@PathVariable String start, @PathVariable String end, @PathVariable String area, @PathVariable String regLevel) {
        Map<String, Object> map = shortApproachService.heavyRainMonitor(start, end, area, regLevel);
        return Result.success(map);
    }

    @ApiOperation("强降水监测警报详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "start", value = "开始时间", example = "2021-01-01"),
            @ApiImplicitParam(name = "end", value = "结束时间", example = "2021-12-31"),
            @ApiImplicitParam(name = "area", value = "地市", example = "长沙市"),
            @ApiImplicitParam(name = "district", value = "区县", example = "岳麓区"),
            @ApiImplicitParam(name = "level", value = "预警等级", example = "all", allowableValues = "all,蓝色,黄色,橙色,红色")
    })
    @GetMapping("/heavyDetail/{start}/{end}/{area}/{district}/{level}")
    public Result heavyDetail(@PathVariable String start, @PathVariable String end, @PathVariable String area, @PathVariable String district, @PathVariable String level) {
        List<Map<String, Object>> list = shortApproachService.heavyDetail(start, end, area, district, level);
        return Result.success(list);
    }

    @ApiOperation("强降水监测警报市县")
    @GetMapping("/getHeavyDistrict")
    public Result getHeavyDistrict() {
        List<Map<String, Object>> list = shortApproachService.getHeavyDistrict();
        return Result.success(list);
    }

}
