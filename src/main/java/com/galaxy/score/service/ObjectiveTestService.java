package com.galaxy.score.service;

import com.galaxy.score.mapper.ObjectiveMapper;
import com.galaxy.score.mapper.ObjectiveTestMapper;
import com.galaxy.score.utils.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author hfr
 * 客观预报竞赛评分
 */
@Service
public class ObjectiveTestService {
    @Autowired
    private ObjectiveTestMapper objectiveTestMapper;

    public List<Map<String, Object>> getAllModels() {
        List<Map<String, Object>> rsList = new ArrayList<>();
        List<Map<String, Object>> list = objectiveTestMapper.getAllModels();
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
        String tableName = "public_test.tb_score_day_pre_ybjs_er" + NumberUtil.makeup0(Integer.parseInt(interval), 2);
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveTestMapper.checkRainScore(tableName, start, end, time, hour, interval, type);
        if (!"24".equals(interval)) {
            List<Map<String, Object>> zhList = objectiveTestMapper.checkRainZh(tableName, start, end, time, hour, interval, type);
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
        String tableName = "public_test.tb_score_day_tem_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveTestMapper.checkTemScore(tableName, start, end, time, hour, interval, facname, type);
        if (!"24".equals(interval)) {
            List<Map<String, Object>> zhList = objectiveTestMapper.checkTemZh(tableName, start, end, time, hour, interval, facname, type);
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
        String tableName = "public_test.tb_score_day_rat_smg_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveTestMapper.checkHeavyScore(tableName, start, end, time, hour, interval, facname, type);
        List<Map<String, Object>> zhList = objectiveTestMapper.checkHeavyZh(tableName, start, end, time, hour, interval, facname, type);
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
        String tableName = "public_test.tb_score_day_pre_ybjs_er" + NumberUtil.makeup0(Integer.parseInt(interval), 2);
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveTestMapper.checkRainDayByDay(tableName, start, end, time, interval, type);
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
        String tableName = "public_test.tb_score_day_tem_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveTestMapper.checkTemDayByDay(tableName, start, end, time, interval, facname, type);
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
        String tableName = "public_test.tb_score_day_rat_smg_ybjs";
        if ("57687".equals(type)) {
            tableName += "_57679";
        }
        List<Map<String, Object>> list = objectiveTestMapper.checkHeavyDayByDay(tableName, start, end, time, interval, facname, type);
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
}
