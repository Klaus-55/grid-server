package com.galaxy.score.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.ga.common.kit.DateKit;
import com.galaxy.score.mapper.ObjectiveMapper;
import com.galaxy.score.utils.NumberUtil;
import io.github.classgraph.json.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.sql.Timestamp;
import java.util.*;

/**
 * @Author hfr
 * 客观预报竞赛评分
 */
@Service
public class ObjectiveService {
    @Autowired
    private ObjectiveMapper objectiveMapper;

    public List<Map<String, Object>> getAllModels() {
        List<Map<String, Object>> rsList = new ArrayList<>();
        List<Map<String, Object>> list = objectiveMapper.getAllModels();
        for (Map<String, Object> map : list) {
            Map<String, Object> rsMap = new HashMap<>();
            rsMap.put("unit", map.get("modelname"));
            rsMap.put("unitName", map.get("zwname"));
            rsMap.put("color", map.get("color"));
            rsList.add(rsMap);
        }
        return rsList;
    }

    //降水评分检验
    public List<Map<String, Object>> checkRainScore(String start, String end, String time, String hour, String interval, String type) {
        String tableName = "public.tb_score_day_pre_ybjs_er" + NumberUtil.makeup0(Integer.parseInt(interval), 2);
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
//        String sql = " AND substr(wfdatetime || '', 9, 2) = " + time;
//        if (Objects.equals(time, "zh")) {
//            sql = " AND (substr(wfdatetime || '', 9, 2) = '20' OR substr(wfdatetime || '', 9, 2) = '08')";
//        }
        List<Map<String, Object>> list = objectiveMapper.checkRainScore(tableName, start, end, time, hour, interval, type);
        if (!"24".equals(interval)) {
            List<Map<String, Object>> zhList = objectiveMapper.checkRainZh(tableName, start, end, time, hour, interval, type);
            for (Map<String, Object> map : zhList) {
                map.put("wfhour", 0);
            }
            list.addAll(zhList);
        }
        //中央台结果
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("BABJ".equals(map.get("wfsrc"))) {
                babjMap.put("pc_" + map.get("wfhour"), map.get("pc"));
                babjMap.put("ts_" + map.get("wfhour"), map.get("ts"));
                babjMap.put("bi_" + map.get("wfhour"), map.get("bi"));
                babjMap.put("me_" + map.get("wfhour"), map.get("me"));
            }
        }

        //晴雨预报技巧(SPC)、强降水预报TS技巧(STS)、强降水预报BIAS技巧(SBI)、降水量预报技巧（SME）
        for (Map<String, Object> map : list) {
            //各参赛队结果
            double pcp = map.get("pc") == null ? Double.NaN : Double.valueOf(map.get("pc").toString());
            double tsp = map.get("ts") == null ? Double.NaN : Double.valueOf(map.get("ts").toString());
            double bp = map.get("bi") == null ? Double.NaN : Double.valueOf(map.get("bi").toString());
            double mep = map.get("me") == null ? Double.NaN : Double.valueOf(map.get("me").toString());
            //国家气象中心结果
            int wfhour = (int) map.get("wfhour");
            double pcn = babjMap.get("pc_" + wfhour) == null ? Double.NaN : Double.valueOf(babjMap.get("pc_" + wfhour).toString());
            double tsn = babjMap.get("ts_" + wfhour) == null ? Double.NaN : Double.valueOf(babjMap.get("ts_" + wfhour).toString());
            double bn = babjMap.get("bi_" + wfhour) == null ? Double.NaN : Double.valueOf(babjMap.get("bi_" + wfhour).toString());
            double men = babjMap.get("me_" + wfhour) == null ? Double.NaN :  Double.valueOf(babjMap.get("me_" + wfhour).toString());
            //晴雨预报技巧(SPC) (PCP - PCN) *100% / (1 - PCN)  其中PCP代表各参赛队，PCN代表国家气象中心
            double spc = Double.NaN;
            if (!Double.isNaN(pcp) && !Double.isNaN(pcn)) spc = (pcp - pcn) * 100 / (100 - pcn);
            //强降水预报TS技巧(STS) (TSP - TSN) / (1 - TSN)
            double sts = Double.NaN;
            if (!Double.isNaN(tsp) && !Double.isNaN(tsn)) sts = (tsp - tsn) / (1 - tsn);
            //强降水预报BIAS技巧(SBI) (BN - BP) / BN
            double sbi = Double.NaN;
            if (!Double.isNaN(bn) && !Double.isNaN(bp)) sbi = (bn - bp) / bn;
            //降水量预报技巧(SME) (MEN - MEP) / MEN
            double sme = Double.NaN;
            if (!Double.isNaN(men) && !Double.isNaN(mep)) sme = (men - mep) / men;
            if (!Double.isInfinite(spc) && spc < -100) {
                spc = -100.0;
            }
            map.put("spc", spc);
            map.put("sts", sts);
            map.put("sbi", sbi);
            map.put("sme", sme);
        }
        return list;
    }

    //温度评分检验
    public List<Map<String, Object>> checkTemScore(String start, String end, String time, String hour,
                                                    String interval, String facname, String type) {
        String tableName = "public.tb_score_day_tem_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveMapper.checkTemScore(tableName, start, end, time, hour, interval, facname, type);
        if (!"24".equals(interval)) {
            List<Map<String, Object>> zhList = objectiveMapper.checkTemZh(tableName, start, end, time, hour, interval, facname, type);
            for (Map<String, Object> map : zhList) {
                map.put("wfhour", 0);
            }
            list.addAll(zhList);
        }
        //中央台的查询结果过滤出来放到map集合中
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("BABJ".equals(map.get("wfsrc"))) {
                babjMap.put("mae_" + map.get("wfhour"), map.get("mae"));
                babjMap.put("rmse_" + map.get("wfhour"), map.get("rmse"));
                babjMap.put("ok1_" + map.get("wfhour"), map.get("ok1"));
                babjMap.put("ok2_" + map.get("wfhour"), map.get("ok2"));
            }
        }
        //计算各个模式的温度预报技巧 (maen - maep)/maen
        for (Map<String, Object> map : list) {
            double maep = map.get("mae") == null ? Double.NaN : Double.valueOf(map.get("mae").toString());
            double maen = babjMap.get("mae_" + map.get("wfhour")) == null ? Double.NaN : Double.parseDouble(babjMap.get("mae_" + map.get("wfhour")).toString());
            double sst = Double.NaN;
            if (!Double.isNaN(maep) && !Double.isNaN(maen)) sst = (maen - maep) / maen;
            map.put("sst", sst);
            if ("TMAX".equals(facname)) {
                map.put("maxtsst", map.remove("sst"));
                map.put("maxtok2", map.remove("ok2"));
                map.put("maxtok1", map.remove("ok1"));
                map.put("maxtmae", map.remove("mae"));
                map.put("maxtrmse", map.remove("rmse"));
            } else if ("TMIN".equals(facname)) {
                map.put("mintsst", map.remove("sst"));
                map.put("mintok2", map.remove("ok2"));
                map.put("mintok1", map.remove("ok1"));
                map.put("mintmae", map.remove("mae"));
                map.put("mintrmse", map.remove("rmse"));
            }
        }
        return list;
    }

    //强对流评分检验
    public List<Map<String,Object>> checkHeavyScore(String start, String end, String time, String hour,
                                                    String interval, String facname, String type) {
        String tableName = "public.tb_score_day_rat_smg_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveMapper.checkHeavyScore(tableName, start, end, time, hour, interval, facname, type);
        List<Map<String, Object>> zhList = objectiveMapper.checkHeavyZh(tableName, start, end, time, hour, interval, facname, type);
        for (Map<String, Object> map : zhList) {
            map.put("wfhour", 0);
        }
        list.addAll(zhList);
        //中央台的查询结果过滤出来放到map集合中
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("BABJ".equals(map.get("wfsrc"))) {
                babjMap.put("pod_" + map.get("wfhour"), map.get("pod"));
                babjMap.put("far_" + map.get("wfhour"), map.get("far"));
            }
        }
        //计算各个模式的预报技巧评分
        for (Map<String, Object> map : list) {
            //各模式结果
            double podp = map.get("pod") == null ? Double.NaN : Double.valueOf(map.get("pod").toString());
            double farp = map.get("far") == null ? Double.NaN : Double.valueOf(map.get("far").toString());
            //中央台结果
            double podn = babjMap.get("pod_" + map.get("wfhour")) == null ? Double.NaN : Double.parseDouble(babjMap.get("pod_" + map.get("wfhour")).toString());
            double farn = babjMap.get("far_" + map.get("wfhour")) == null ? Double.NaN : Double.parseDouble(babjMap.get("far_" + map.get("wfhour")).toString());
            //命中率预报技巧 (podp - podn) / (1 - podn)
            double spo = Double.NaN;
            if (!Double.isNaN(podp) && !Double.isNaN(podn)) spo = (podp - podn) / (1 - podn);
            //空报率预报技巧 (farn - farp) / farp
            double sfa = Double.NaN;
            if (!Double.isNaN(farp) && !Double.isNaN(farn)) sfa = (farn - farp) / farp;
            map.put("spo", spo);
            map.put("sfa", sfa);
            if (!Double.isNaN(podp)) map.put("pod", podp * 100);
            if (!Double.isNaN(farp)) map.put("far", farp * 100);
        }
        return list;
    }

    //逐日检验
    public List<Map<String, Object>> checkRainDayByDay(String start, String end, String time, String interval, String type) {
        String tableName = "public.tb_score_day_pre_ybjs_er" + NumberUtil.makeup0(Integer.parseInt(interval), 2);
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveMapper.checkRainDayByDay(tableName, start, end, time, interval, type);
        //中央台结果
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("BABJ".equals(map.get("wfsrc"))) {
                babjMap.put("pc_" + map.get("wfdatetime"), map.get("pc"));
                babjMap.put("ts_" + map.get("wfdatetime"), map.get("ts"));
                babjMap.put("bi_" + map.get("wfdatetime"), map.get("bi"));
                babjMap.put("me_" + map.get("wfdatetime"), map.get("me"));
            }
        }

        //晴雨预报技巧(SPC)、强降水预报TS技巧(STS)、强降水预报BIAS技巧(SBI)、降水量预报技巧（SME）
        for (Map<String, Object> map : list) {
            //各参赛队结果
            double pcp = map.get("pc") == null ? Double.NaN : Double.valueOf(map.get("pc").toString());
            double tsp = map.get("ts") == null ? Double.NaN : Double.valueOf(map.get("ts").toString());
            double bp = map.get("bi") == null ? Double.NaN : Double.valueOf(map.get("bi").toString());
            double mep = map.get("me") == null ? Double.NaN : Double.valueOf(map.get("me").toString());
            //国家气象中心结果
            String wfdatetime = (String) map.get("wfdatetime");
            double pcn = babjMap.get("pc_" + wfdatetime) == null ? Double.NaN : Double.valueOf(babjMap.get("pc_" + wfdatetime).toString());
            double tsn = babjMap.get("ts_" + wfdatetime) == null ? Double.NaN : Double.valueOf(babjMap.get("ts_" + wfdatetime).toString());
            double bn = babjMap.get("bi_" + wfdatetime) == null ? Double.NaN : Double.valueOf(babjMap.get("bi_" + wfdatetime).toString());
            double men = babjMap.get("me_" + wfdatetime) == null ? Double.NaN :  Double.valueOf(babjMap.get("me_" + wfdatetime).toString());
            //晴雨预报技巧(SPC) (PCP - PCN) *100% / (1 - PCN)  其中PCP代表各参赛队，PCN代表国家气象中心
            double spc = Double.NaN;
            if (!Double.isNaN(pcp) && !Double.isNaN(pcn)) spc = (pcp - pcn) * 100 / (100 - pcn);
            //强降水预报TS技巧(STS) (TSP - TSN) / (1 - TSN)
            double sts = Double.NaN;
            if (!Double.isNaN(tsp) && !Double.isNaN(tsn)) sts = (tsp - tsn) / (1 - tsn);
            //强降水预报BIAS技巧(SBI) (BN - BP) / BN
            double sbi = Double.NaN;
            if (!Double.isNaN(bn) && !Double.isNaN(bp)) sbi = (bn - bp) / bn;
            //降水量预报技巧(SME) (MEN - MEP) / MEN
            double sme = Double.NaN;
            if (!Double.isNaN(men) && !Double.isNaN(mep)) sme = (men - mep) / men;
            if (!Double.isInfinite(spc) && spc < -100) {
                spc = -100.0;
            }
            map.put("spc", spc);
            map.put("sts", sts);
            map.put("sbi", sbi);
            map.put("sme", sme);
        }
        return list;
    }

    public List<Map<String, Object>> checkTemDayByDay(String start, String end, String time,
                                                      String interval, String facname, String type) {
        String tableName = "public.tb_score_day_tem_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveMapper.checkTemDayByDay(tableName, start, end, time, interval, facname, type);
        //中央台的查询结果过滤出来放到map集合中
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("BABJ".equals(map.get("wfsrc"))) {
                babjMap.put("mae_" + map.get("wfdatetime"), map.get("mae"));
                babjMap.put("rmse_" + map.get("wfdatetime"), map.get("rmse"));
                babjMap.put("ok1_" + map.get("wfdatetime"), map.get("ok1"));
                babjMap.put("ok2_" + map.get("wfdatetime"), map.get("ok2"));
            }
        }
        //计算各个模式的温度预报技巧 (maen - maep)/maen
        for (Map<String, Object> map : list) {
            double maep = map.get("mae") == null ? Double.NaN : Double.valueOf(map.get("mae").toString());
            double maen = babjMap.get("mae_" + map.get("wfdatetime")) == null ? Double.NaN : Double.parseDouble(babjMap.get("mae_" + map.get("wfdatetime")).toString());
            double sst = Double.NaN;
            if (!Double.isNaN(maep) && !Double.isNaN(maen)) sst = (maen - maep) / maen;
            map.put("sst", sst);
            if ("TMAX".equals(facname)) {
                map.put("maxtsst", map.remove("sst"));
                map.put("maxtok2", map.remove("ok2"));
                map.put("maxtok1", map.remove("ok1"));
                map.put("maxtmae", map.remove("mae"));
                map.put("maxtrmse", map.remove("rmse"));
            } else if ("TMIN".equals(facname)) {
                map.put("mintsst", map.remove("sst"));
                map.put("mintok2", map.remove("ok2"));
                map.put("mintok1", map.remove("ok1"));
                map.put("mintmae", map.remove("mae"));
                map.put("mintrmse", map.remove("rmse"));
            }
        }
        return list;
    }

    public List<Map<String,Object>> checkHeavyDayByDay(String start, String end, String time,
                                                       String interval, String facname, String type) {
        String tableName = "public.tb_score_day_rat_smg_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveMapper.checkHeavyDayByDay(tableName, start, end, time, interval, facname, type);
        //中央台的查询结果过滤出来放到map集合中
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("BABJ".equals(map.get("wfsrc"))) {
                babjMap.put("pod_" + map.get("wfdatetime"), map.get("pod"));
                babjMap.put("far_" + map.get("wfdatetime"), map.get("far"));
            }
        }
        //计算各个模式的预报技巧评分
        for (Map<String, Object> map : list) {
            //各模式结果
            double podp = map.get("pod") == null ? Double.NaN : Double.valueOf(map.get("pod").toString());
            double farp = map.get("far") == null ? Double.NaN : Double.valueOf(map.get("far").toString());
            //中央台结果
            double podn = babjMap.get("pod_" + map.get("wfdatetime")) == null ? Double.NaN : Double.parseDouble(babjMap.get("pod_" + map.get("wfdatetime")).toString());
            double farn = babjMap.get("far_" + map.get("wfdatetime")) == null ? Double.NaN : Double.parseDouble(babjMap.get("far_" + map.get("wfdatetime")).toString());
            //命中率预报技巧 (podp - podn) / (1 - podn)
            double spo = Double.NaN;
            if (!Double.isNaN(podp) && !Double.isNaN(podn)) spo = (podp - podn) / (1 - podn);
            //空报率预报技巧 (farn - farp) / farp
            double sfa = Double.NaN;
            if (!Double.isNaN(farp) && !Double.isNaN(farn)) sfa = (farn - farp) / farp;
            map.put("spo", spo);
            map.put("sfa", sfa);
            if (!Double.isNaN(podp)) map.put("pod", podp * 100);
            if (!Double.isNaN(farp)) map.put("far", farp * 100);
        }
        return list;
    }

    public Map<String, Object> forecastMonitor(String start, String end, String facname) {
        Map<String, Object> rsMap = new HashMap<>();
        List<String> dateTimes = new ArrayList<>();
        Date startDate = DateKit.parse(start);
        Date endDate= DateKit.parse(end);
        while (!startDate.after(endDate)) {
            String dateStr = DateKit.format(startDate, "yyyyMMdd");
            dateTimes.add(dateStr + "08");
            dateTimes.add(dateStr + "20");
            startDate = DateKit.addDay(startDate, 1);
        }
        start += "0000";
        end += "2359";
        String tableName = "tb_gribwf_zd";
        if (Objects.equals("rain", facname)) {
            facname = "ER01,ER03,ER24";
        } else if (Objects.equals("tem", facname)) {
            facname = "TMAX,TMIN,TMP";
        } else {
            facname = "SMG,RAT";
            tableName = "tb_gribwf_dl";
        }
        List<Map<String, String>> tableHeader = new ArrayList<>();
        Map<String, String> columnMap = new HashMap<>();
        columnMap.put("prop", "unit");
        columnMap.put("label", "模式名");
        tableHeader.add(columnMap);
        columnMap = new HashMap<>();
        columnMap.put("prop", "facname");
        columnMap.put("label", "要素");
        tableHeader.add(columnMap);
        for (String dateTime : dateTimes) {
            columnMap = new HashMap<>();
            columnMap.put("prop", dateTime);
            columnMap.put("label", dateTime);
            tableHeader.add(columnMap);
        }
        String[] facnames = facname.split(",");
        List<Map<String, Object>> list = objectiveMapper.foreAndLiveMonitor(start, end, facname, tableName);
        Set<String> unitMap = new LinkedHashSet<>();
        for (Map<String, Object> map : list) {
            unitMap.add((String) map.get("unit"));
        }
        List<Map<String, Object>> tableData = new ArrayList<>();
        for (String unit : unitMap) {
            for (String fac : facnames) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("unit", unit);
                rowMap.put("facname", fac);
                for (String dateTime : dateTimes) {
                    rowMap.put(dateTime, 0);
                    for (Map<String, Object> map : list) {
                        String unit2 = map.get("unit").toString();
                        String wfdatetime = map.get("wfdatetime").toString();
                        String fac2 = map.get("facname").toString();
                        if (Objects.equals(unit, unit2) && Objects.equals(dateTime, wfdatetime) && Objects.equals(fac, fac2)) {
                            rowMap.put(dateTime, map.get("filecount"));
                        }
                    }
                }
                tableData.add(rowMap);
            }
        }
        rsMap.put("tableHeader", tableHeader);
        rsMap.put("tableData", tableData);
        return rsMap;
    }

    @DS("live")
    public Map<String, Object> getObtCount(String start, String end) {
        Map<String, Object> rsMap = new HashMap<>();
        start += "000000";
        end += "235959";
        Date startDate = DateKit.parse(start);
        Date endDate = DateKit.parse(end);
        List<String> dateTimes = new ArrayList<>();
        while (!startDate.after(endDate)) {
            String dateTime = DateKit.format(startDate, "yyyy-MM-dd HH:mm");
            dateTimes.add(dateTime);
            startDate = DateKit.addHour(startDate, 1);
        }
        String headStr = "[" +
                "{prop: 'datetime', label: '日期'}," +
                "{prop: 'obtHour', label: '站点实况'}," +
                "{prop: 'gridHour', label: '格点小时实况'}," +
                "{prop: 'gridDay', label: '格点日实况'}," +
                "]";
        List tableHeader = JSON.parseObject(headStr, List.class);
        rsMap.put("tableHeader", tableHeader);
        start = DateKit.format(DateKit.parse(start), "yyyy-MM-dd HH:mm:ss");
        end = DateKit.format(DateKit.parse(end), "yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> list = objectiveMapper.getObtHourCount(Timestamp.valueOf(start), Timestamp.valueOf(end));
        List<Map<String, Object>> tableData = new ArrayList<>();
        Map<String, Object> obtHour = new HashMap<>();
        for (Map<String, Object> map : list) {
            obtHour.put(map.get("ddatetime").toString(), map.get("obthour"));
        }
        for (String dateTime : dateTimes) {
            Map<String, Object> map = new HashMap<>();
            map.put("datetime", dateTime);
            map.put("obtHour", obtHour.get(dateTime) == null ? 0 : obtHour.get(dateTime));
            tableData.add(map);
        }
        rsMap.put("tableData", tableData);
        return rsMap;
    }

    public void getGridCount(Map<String, Object> rsMap, String start, String end, String facname) {
        start += "000000";
        end += "235959";
        Map<String, Integer> gridHour = getGridHourCount(start, end, facname);
        Map<String, Integer> gridDay = getGridDayCount(start, end, facname);
        List<Map<String, Object>> tableData = (List<Map<String, Object>>) rsMap.get("tableData");
        for (Map<String, Object> map : tableData) {
            String dateTime = map.get("datetime").toString();
            map.put("gridHour", gridHour.get(dateTime) == null ? 0 : gridHour.get(dateTime));
            map.put("gridDay", gridDay.get(dateTime) == null ? 0 : gridDay.get(dateTime));
        }
    }

    private Map<String, Integer> getGridDayCount(String start, String end, String facname) {
        Map<String, Integer> rsMap = new HashMap<>();
        if (Objects.equals("rain", facname)) {
            facname = "PRE";
        } else if (Objects.equals("tem", facname)) {
            facname = "MNT,MXT";
        } else if (Objects.equals("qdl", facname)) {
            facname = "PRE";
        }
        start = DateKit.format(DateKit.parse(start), "yyyyMMddHH");
        end = DateKit.format(DateKit.parse(end), "yyyyMMddHH");
        List<Map<String, Object>> list = objectiveMapper.getGridDayCount(start, end, facname);
        for (Map<String, Object> map : list) {
            rsMap.put(map.get("ddatetime").toString(), Integer.parseInt(map.get("gridday").toString()));
        }
        return rsMap;
    }

    private Map<String, Integer> getGridHourCount(String start, String end, String facname) {
        Map<String, Integer> rsMap = new HashMap<>();
        if (Objects.equals("rain", facname)) {
            facname = "PRE01,PRE03";
        } else if (Objects.equals("tem", facname)) {
            facname = "TEM";
        } else if (Objects.equals("qdl", facname)) {
            facname = "PRE01,PRE03";
        }
        start = DateKit.format(DateKit.parse(start), "yyyyMMddHH");
        end = DateKit.format(DateKit.parse(end), "yyyyMMddHH");
        List<Map<String, Object>> list = objectiveMapper.getGridHourCount(start, end, facname);
        for (Map<String, Object> map : list) {
            rsMap.put(map.get("ddatetime").toString(), Integer.parseInt(map.get("gridhour").toString()));
        }
        return rsMap;
    }
}
