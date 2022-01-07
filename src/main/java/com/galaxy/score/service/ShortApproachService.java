package com.galaxy.score.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galaxy.score.common.SeriesItem;
import com.galaxy.score.mapper.AttendanceMapper;
import com.galaxy.score.mapper.ShortApproachMapper;
import com.galaxy.score.model.Attendance;
import com.galaxy.score.utils.Arith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 短临预报质量评分service层
 */
@Service
@DS("duanlin")
public class ShortApproachService {

    @Autowired
    private ShortApproachMapper shortApproachMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    //降水检验
    public List<Map<String, Object>> rainScore(String start, String end, String ftime, String wfinterval, String product) {
        List<Map<String, Object>> list = shortApproachMapper.rainScore(start, end, ftime, wfinterval, product);
        Map<String, String> modelMap = getModelName();
        for (Map<String, Object> map : list) {
            String modelName = (String) map.get("wfsrc");
            map.put("zwname", modelMap.get(modelName) == null ? modelName : modelMap.get(modelName));
        }
        return list;
    }

    //温度检验
    public List<Map<String, Object>> temScore(String start, String end, String ftime, String wfinterval, String product) {
        List<Map<String, Object>> list = shortApproachMapper.temScore(start, end, ftime, wfinterval, product);
        Map<String, String> modelMap = getModelName();
        for (Map<String, Object> map : list) {
            String modelName = (String) map.get("wfsrc");
            map.put("zwname", modelMap.get(modelName) == null ? modelName : modelMap.get(modelName));
        }
        return list;
    }

    public Map<String, String> getModelName() {
        Map<String, String> modelMap = new HashMap<>();
        List<Map<String, Object>> list = shortApproachMapper.getModelName();
        for (Map<String, Object> map : list) {
            String modelname = (String) map.get("modelname");
            String zwname = (String) map.get("zwname");
            modelMap.put(modelname, zwname);
        }
        return modelMap;
    }

    public List<Map<String, Object>> heavyRainScore(String start, String end, String ftime) {
        start += " 00:00:00";
        end += " 23:59:59";
        return shortApproachMapper.getHeavyRainScore(start, end, ftime);
    }

    public List<Map<String,Object>> thunderScore(String start, String end, String ftime) {
        return shortApproachMapper.getThunderScore(start, end, ftime);
    }

    public List<Map<String, Object>> thunderstormScore(String start, String end, String ftime) {
        start += " 00:00:00";
        end += " 23:59:59";
        return shortApproachMapper.getThunderstormScore(start, end, ftime);
    }

    public List<Map<String,Object>> hailScore(String start, String end, String ftime) {
        start += " 00:00:00";
        end += " 23:59:59";
        return shortApproachMapper.getHailScore(start, end, ftime);
    }

    //省级预警预报质量
    public Map<String, Object> provincialWarning(String start, String end, String warningType, String method) {
        Map<String, Object> rsMap = new HashMap<>();
        if (!Objects.equals("fj", method)) {
            getProvincialWarningAll(start, end, warningType, method, rsMap);
        } else {
            getProvincialWarningFj(start, end, warningType, method, rsMap);
        }
       return rsMap;
    }

    private void getProvincialWarningAll(String start, String end, String warningType, String method, Map<String, Object> rsMap) {
        List<Map<String, Object>> list;
        List<Map<String, Object>> zhList;
        if (!Objects.equals("综合", warningType)) {
            list = shortApproachMapper.getProvincialAll(start, end, warningType, method);
            zhList = shortApproachMapper.getProvincialAllZh(start, end, warningType, method);
        } else {
            list = shortApproachMapper.getProvincialAllByType(start, end, method);
            zhList = shortApproachMapper.getProvincialAllZhByType(start, end, method);
        }
        rsMap.put("rs", list);
        rsMap.put("zh", zhList);
    }

    private void getProvincialWarningFj(String start, String end, String warningType, String method, Map<String,Object> rsMap) {
        List<Map<String, Object>> list;
        List<Map<String, Object>> zhList;
        if (!Objects.equals("综合", warningType)) {
            list = shortApproachMapper.getProvincialFj(start, end, warningType);
            zhList = shortApproachMapper.getProvincialFjZhByLevel(start, end, warningType);
        } else {
            list = shortApproachMapper.getProvincialFjByType(start, end);
            zhList = shortApproachMapper.getProvincialFjZhByTypeLevel(start, end);
        }
        rsMap.put("rs", list);
        rsMap.put("zh", zhList);
    }

    //省级预警评定详情
    public List<Map<String, Object>> provincialDetail(String start, String end, String warningType, String level, String rs, String type) {
        start += " 00:00:00";
        end += " 23:59:59";
//        if ("所有气象台".equals(department)) department = "all";
        if ("所有等级".equals(level)) level = "all";
        if ("所有结果".equals(rs)) {
            rs = "all";
        } else if ("正确".equals(rs)) {
            rs = "NA";
        } else if ("空报".equals(rs)) {
            rs = "NB";
        } else if ("漏报".equals(rs)) {
            rs = "NC";
        }
        return shortApproachMapper.provincialDetail(start, end, warningType, level, rs, type);
    }

    public Map<String, Object> cityWarning(String start, String end, String warningType, String method) {
        Map<String, Object> rsMap = new HashMap<>();
        if (!Objects.equals("fj", method)) {
            getCityWarningAll(start, end, warningType, method, rsMap);
        } else {
            getCityWarningFj(start, end, warningType, method, rsMap);
        }
        return rsMap;
    }

    private void getCityWarningAll(String start, String end, String warningType, String method, Map<String, Object> rsMap) {
        List<Map<String, Object>> areaList;
        List<Map<String, Object>> forecasterList;
        List<Map<String, Object>> districtList;
        if (!Objects.equals("综合", warningType)) {
            areaList = shortApproachMapper.getCityWarningAll(start, end, warningType, method, "area");
            forecasterList = shortApproachMapper.getCityWarningAll(start, end, warningType, method, "forecaster");
            districtList = shortApproachMapper.getCityWarningAll(start, end, warningType, method, "district");
        } else {
            areaList = shortApproachMapper.getCityWarningAllZh(start, end, method, "area");
            forecasterList = shortApproachMapper.getCityWarningAllZh(start, end, method, "forecaster");
            districtList = shortApproachMapper.getCityWarningAllZh(start, end,  method, "district");
        }
        rsMap.put("area", areaList);
        rsMap.put("forecaster", forecasterList);
        rsMap.put("district", districtList);
    }

    private void getCityWarningFj(String start, String end, String warningType, String method, Map<String, Object> rsMap) {
        List<Map<String, Object>> areaList;
        List<Map<String, Object>> districtList;
        List<Map<String, Object>> areaZhList;
        List<Map<String, Object>> forecasterZhList;
        List<Map<String, Object>> districtZhList;
        if (!Objects.equals("综合", warningType)) {
            areaList = shortApproachMapper.getCityWarningFj(start, end, warningType, "area");
            districtList = shortApproachMapper.getCityWarningFj(start, end, warningType, "district");
            areaZhList = shortApproachMapper.getZhByLevel(start, end, warningType, "area");
            forecasterZhList = shortApproachMapper.getZhByLevel(start, end, warningType, "forecaster");
            districtZhList = shortApproachMapper.getZhByLevel(start, end, warningType, "district");
        } else {
            areaList = shortApproachMapper.getCityWarningFjZh(start, end, "area");
            districtList = shortApproachMapper.getCityWarningFjZh(start, end, "district");
            areaZhList = shortApproachMapper.getZhByTypeLevel(start, end, "area");
            forecasterZhList = shortApproachMapper.getZhByTypeLevel(start, end, "forecaster");
            districtZhList = shortApproachMapper.getZhByTypeLevel(start, end, "district");
        }
        Map<String, Object> zhMap = new HashMap<>();
        zhMap.put("area", areaZhList);
        zhMap.put("forecaster", forecasterZhList);
        zhMap.put("district", districtZhList);
        rsMap.put("area", areaList);
        rsMap.put("district", districtList);
        rsMap.put("zh", zhMap);
    }

    public List<Map<String, Object>> cityDetail(String start, String end, String department, String warningType, String level, String rs, String type) {
        start += " 00:00:00";
        end += " 23:59:59";
        if ("所有气象台".equals(department)) department = "all";
        if ("所有等级".equals(level)) level = "all";
        if ("所有结果".equals(rs)) {
            rs = "all";
        } else if ("正确".equals(rs)) {
            rs = "NA";
        } else if ("空报".equals(rs)) {
            rs = "NB";
        } else if ("漏报".equals(rs)) {
            rs = "NC";
        }
        return shortApproachMapper.cityDetail(start, end, department, warningType, level, rs, type);
    }

    //预警有效性评定
    //{data: [{name: '湖南省', y: 75.2, drilldown: '湖南省'}], series: [{id: '湖南省', data: [{name: '唐佳', y: 2.1}]}]}
    public Map<String,Object> cityWarningEff(String start, String end, String warningType) {
        Map<String, Object> rsMap = new HashMap<>();
        start += " 00:00:00";
        end += " 23:59:59";
        String[] areas = new String[]{"湖南省", "长沙","株洲","湘潭","衡阳","邵阳","岳阳",
                "常德","张家界","益阳","郴州","永州","怀化","娄底","湘西州"};
        //得到各气象台结果
        List<Map<String, Object>> depList = shortApproachMapper.getCityWarningEff(start, end, warningType, "department");
        //各地级市预报员结果
        List<Map<String, Object>> foreList = shortApproachMapper.getCityWarningEff(start, end, warningType, "forecaster");
        List<Map<String, Object>> data = new ArrayList<>();
        List<Map<String, Object>> series = new ArrayList<>();
        for (String area : areas) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("name", area);
            dataMap.put("drilldown", area);
            for (Map<String, Object> map : depList) {
                String department = map.get("area").toString();
                if (department.contains(area)) {
//                    Double value = Objects.isNull(map.get("score")) ? null : Double.parseDouble(map.get("score").toString()) + 60;
                    dataMap.put("y", map.get("score"));
                    break;
                }
            }
            Map<String, Object> seriesMap = new HashMap<>();
            seriesMap.put("id", area);
            List<Map<String, Object>> seriesData = new ArrayList<>();
            for (Map<String, Object> map : foreList) {
                String department = map.get("area").toString();
                if (department.contains(area)) {
                    Map<String, Object> sDataMap = new HashMap<>();
                    sDataMap.put("name", map.get("forecaster"));
                    sDataMap.put("y", map.get("score"));
                    seriesData.add(sDataMap);
                }
            }
            seriesMap.put("data", seriesData);
            data.add(dataMap);
            series.add(seriesMap);
        }
        rsMap.put("data", data);
        rsMap.put("series", series);
        if (depList.size() == 0) rsMap.put("data", depList);
        if (foreList.size() == 0) rsMap.put("series", foreList);
        return rsMap;
    }

    //地级市各区县的有效性评定
    public Map<String, Object> districtWarningEff(String start, String end, String warningType, String area) {
        start += " 00:00:00";
        end += " 23:59:59";
        Map<String, Object> rsMap = new HashMap<>();
        List<Map<String, Object>> list = shortApproachMapper.getDistrictsWarningEff(start, end, warningType, area);
        Map<String, Object> mapData = list.remove(0);
        List<Map<String, Object>> tableData = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("district", map.get("area"));
            tMap.put("score", map.get("score"));
            tableData.add(tMap);
        }
        rsMap.put("tableData", tableData);
        rsMap.put("mapData", mapData);
        return rsMap;
    }

    public List<Map<String, Object>> cityDetailEff(String start, String end, String department, String warningType, String level) {
        start += " 00:00:00";
        end += " 23:59:59";
        if ("所有气象台".equals(department)) department = "all";
        if ("所有等级".equals(level)) level = "all";
        return shortApproachMapper.cityDetailEff(start, end, department, warningType, level);
    }

    public Map<String, Object> heavyRainMonitor(String start, String end, String area, String regLevel) {
        Map<String, Object> rsMap = new HashMap<>();
        if (Objects.equals(regLevel, "area_district")) {
            List<Map<String, Object>> areaList = shortApproachMapper.getHeavyMonitorAll(start, end, "area");
            List<Map<String, Object>> foreList = shortApproachMapper.getHeavyMonitorAll(start, end, "forecaster");
            rsMap.put(regLevel, areaList);
            rsMap.put("forecaster", foreList);
        } else if (Objects.equals(regLevel, "area")) {
            List<Map<String, Object>> areaList = shortApproachMapper.getHeavyMonitorByArea(start, end, "area");
            List<Map<String, Object>> foreList = shortApproachMapper.getHeavyMonitorByArea(start, end, "forecaster");
            rsMap.put(regLevel, areaList);
            rsMap.put("forecaster", foreList);
        } else if (Objects.equals(regLevel, "district")) {
            List<Map<String, Object>> disList = shortApproachMapper.getHeavyMonitorByDistrict(start, end, area, "district");
            List<Map<String, Object>> foreList = shortApproachMapper.getHeavyMonitorByDistrict(start, end, area, "forecaster");
            rsMap.put(regLevel, disList);
            rsMap.put("forecaster", foreList);
        } else {
            List<Map<String, Object>> couList = shortApproachMapper.getHeavyMonitorByCountry(start, end, area);
            rsMap.put(regLevel, couList);
            rsMap.put("forecaster", new ArrayList<>());
        }

        return rsMap;
    }

    public List<Map<String, Object>> heavyDetail(String start, String end, String area, String district, String level) {
        return shortApproachMapper.heavyDetail(start, end, area, district, level);
    }

    public List<Map<String, Object>> getHeavyDistrict() {
        return shortApproachMapper.getHeavyDistrict();
    }

    public Page<Attendance> findList(Page<Attendance> page) {
//        QueryWrapper<Attendance> wrapper = new QueryWrapper<>();
        return (Page<Attendance>) attendanceMapper.selectPage(page, null);
    }


}
