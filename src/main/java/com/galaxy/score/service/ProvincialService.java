package com.galaxy.score.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ga.common.kit.DateKit;
import com.galaxy.score.mapper.ProvincialMapper;
import com.galaxy.score.utils.Arith;
import com.galaxy.score.utils.ExamineKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

/**
 * @Author hfr
 * @Date 2021/12/6 15:09
 */
@Service
@DS("zhongduan")
public class ProvincialService {

    @Autowired
    private ProvincialMapper provincialMapper;

    public List<Map<String, Object>> getChiefScore(String start, String end) {
        List<Map<String, Object>> rsList = new ArrayList<>();
        List<Map<String, Object>> forecasters = provincialMapper.getChiefForecasters(start, end);
        Map<String, String> dayMap = new HashMap<>();
        for (Map<String, Object> map : forecasters) {
            String forecaster = map.get("forecaster").toString();
            String wfdatetime = map.get("wfdatetime").toString();
            String dayTime = dayMap.getOrDefault(forecaster, "");
            String dayStr = dayTime + "," + wfdatetime;
            if (Objects.equals("", dayTime)) dayStr = wfdatetime;
            dayMap.put(forecaster, dayStr);
        }
        for (String forecaster : dayMap.keySet()) {
            Map<String, Object> rsMap = new HashMap<>();
            rsMap.put("forecaster", forecaster);
            Double wmZh = provincialMapper.getWarningMessage(start, end, forecaster);
            rsMap.put("warning", wmZh);
            Map<String, Object> publicMap = provincialMapper.getWeatherPublic(dayMap.get(forecaster));
            rsMap.put("public", Arith.round(getTownZhValue(publicMap), 2));
            Double rainZh = provincialMapper.getRainProgress(start, end, forecaster);
            rsMap.put("rain", rainZh);
            rsList.add(rsMap);
        }
        resolveRsList(rsList);
        for (Map<String, Object> map : rsList) {
            map.put("zh", getChiefZh(map));
        }
        return rsList;
    }

    public List<Map<String, Object>> getForemanScore(String start, String end) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> rsList = getForemanScoreByFtime(start, end, false);
//        List<Map<String, Object>> rsList = provincialMapper.getForemanScore(start, end);
        //省台结果，以此来算各个预报员的技巧
//        List<Map<String, Object>> ytList = provincialMapper.getForemanScoreYt(start, end);
        List<Map<String, Object>> ytList = getForemanScoreByFtime(start, end, true);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> ytRs = ytList.stream().filter(item -> Objects.equals(item.get("forecaster"), map.get("forecaster"))).findAny().orElse(null);
            if (Objects.isNull(ytRs)) continue;
            ExamineKit.getTownJq(map, ytRs);
            map.put("zhzl", getZh(map));
        }
        resolveFnList(rsList);
        getRankedRs(rsList);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> rsMap = new HashMap<>();
            rsMap.put("forecaster", map.get("forecaster"));
            rsMap.put("zhjq", map.get("zhjq"));
            rsMap.put("zhjq_per", map.get("zhjq_per"));
            rsMap.put("zhzl", map.get("zhzl"));
            rsMap.put("zhzl_per", map.get("zhzl_per"));
            double zhjq = Double.parseDouble(map.get("zhjq_per").toString());
            double zhzl = Double.parseDouble(map.get("zhzl_per").toString());
            if (Double.isNaN(zhjq) && zhzl == -999.0) {
                rsMap.put("zh", null);
            } else {
                if (Double.isNaN(zhjq)) zhjq = 0.0;
                if (zhzl == -999.0) zhzl = 0.0;
                rsMap.put("zh", Arith.round((zhjq + zhzl) / 2, 2));
            }
            list.add(rsMap);
        }
        list.sort((a, b) -> {
            double zh1 = Double.parseDouble(a.get("zh").toString());
            double zh2 = Double.parseDouble(b.get("zh").toString());
            return Double.compare(zh2, zh1);
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put("pm", i + 1);
        }
        return list;
    }

    private List<Map<String,Object>> getForemanScoreByFtime(String start, String end, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = provincialMapper.getForemanScore(start, end, fhour);
            } else {
                rsList = provincialMapper.getForemanScoreYt(start, end, fhour);
            }
            for (Map<String, Object> map : rsList) {
                double zhValue = getTownZhValue(map);
                map.put("zhjs", Arith.round(zhValue, 1));
                String forecaster = map.get("forecaster").toString();
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(forecaster, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String forecaster = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("forecaster", forecaster);
            list.add(rsMap);
        }
        return list;
    }

    public List<Map<String, Object>> getNightShiftScore(String start, String end) {
        List<Map<String, Object>> list = new ArrayList<>();
//        List<Map<String, Object>> rsList = provincialMapper.getNightShiftScore(start, end);
        List<Map<String, Object>> rsList = getNightShiftScoreByFtime(start, end, false);
        //省台结果，以此来算各个预报员的技巧
//        List<Map<String, Object>> ytList = provincialMapper.getNightShiftScoreYt(start, end);
        List<Map<String, Object>> ytList = getNightShiftScoreByFtime(start, end, true);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> ytRs = ytList.stream().filter(item -> Objects.equals(item.get("forecaster"), map.get("forecaster"))).findAny().orElse(null);
            if (Objects.isNull(ytRs)) continue;
            ExamineKit.getTownJq(map, ytRs);
            map.put("zhzl", getZh(map));
        }
        resolveFnList(rsList);
        getRankedRs(rsList);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> rsMap = new HashMap<>();
            rsMap.put("forecaster", map.get("forecaster"));
            rsMap.put("zhjq", map.get("zhjq"));
            rsMap.put("zhjq_per", map.get("zhjq_per"));
            rsMap.put("zhzl", map.get("zhzl"));
            rsMap.put("zhzl_per", map.get("zhzl_per"));
            double zhjq = Double.parseDouble(map.get("zhjq_per").toString());
            double zhzl = Double.parseDouble(map.get("zhzl_per").toString());
            if (Double.isNaN(zhjq) && zhzl == -999.0) {
                rsMap.put("zh", null);
            } else {
                if (Double.isNaN(zhjq)) zhjq = 0.0;
                if (zhzl == -999.0) zhzl = 0.0;
                rsMap.put("zh", Arith.round((zhjq + zhzl) / 2, 2));
            }
            list.add(rsMap);
        }
        list.sort((a, b) -> {
            double zh1 = Double.parseDouble(a.get("zh").toString());
            double zh2 = Double.parseDouble(b.get("zh").toString());
            return Double.compare(zh2, zh1);
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put("pm", i + 1);
        }
        return list;
    }

    private List<Map<String,Object>> getNightShiftScoreByFtime(String start, String end, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = provincialMapper.getNightShiftScore(start, end, fhour);
            } else {
                rsList = provincialMapper.getNightShiftScoreYt(start, end, fhour);
            }
            for (Map<String, Object> map : rsList) {
                double zhValue = getTownZhValue(map);
                map.put("zhjs", Arith.round(zhValue, 1));
                String forecaster = map.get("forecaster").toString();
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(forecaster, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String forecaster = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("forecaster", forecaster);
            list.add(rsMap);
        }
        return list;
    }

    public List<Map<String, Object>> getShortTermScore(String start, String end) {
        List<Map<String, Object>> list = provincialMapper.getShortTermScore(start, end);
        resolveShortTermList(list);
        for (Map<String, Object> map : list) {
            double sg = Double.parseDouble(map.get("sg").toString());
            double sh = Double.parseDouble(map.get("sh").toString());
            double sr = Double.parseDouble(map.get("sr").toString());
            map.put("zh", Arith.round(sg * 0.4 + sh * 0.3 + sr * 0.3, 2));
        }
        return list;
    }

    public Map<String, Object> getObjectiveScore(String start, String end) {
        Map<String, Object> rsMap = new HashMap<>();
        Map<String, Object> map = provincialMapper.getObjectiveScore(start, end);
        if (Objects.isNull(map)) return rsMap;
        double zhjs = getTownZhValue(map);
        double qyzql = Objects.isNull(map.get("qyzql")) ? -999.0 : Double.parseDouble(map.get("qyzql").toString());
        double maxt = Objects.isNull(map.get("maxt")) ? -999.0 : Double.parseDouble(map.get("maxt").toString());
        double mint = Objects.isNull(map.get("mint")) ? -999.0 : Double.parseDouble(map.get("mint").toString());
        rsMap.put("zhjs", zhjs);
        rsMap.put("qy", qyzql);
        rsMap.put("maxt", maxt);
        rsMap.put("mint", mint);
        if (zhjs == -999.0 && qyzql == -999.0 && maxt == -999.0 && mint == -999.0) {
            rsMap.put("zh", -999.0);
        } else {
            if (zhjs == -999.0) zhjs = 0.0;
            if (qyzql == -999.0) qyzql = 0.0;
            if (maxt == -999.0) maxt = 0.0;
            if (mint == -999.0) mint = 0.0;
            double zh = zhjs * 0.25 + qyzql * 0.25 + maxt * 0.25 + mint * 0.25;
            rsMap.put("zh", zh);
        }
        return rsMap;
    }

    public List<Map<String, Object>> getCityScore(String start, String end) {
        List<Map<String, Object>> list = new ArrayList<>();
//        List<Map<String, Object>> rsList = getCityScoreByFtime(start, end, false);
        //省台结果，以此来算各个地市的技巧
//        List<Map<String, Object>> stList = getCityScoreByFtime(start, end, true);
        List<Map<String, Object>> rsList = getCityScoreByFtime2(start, end);
        //地市预警信号结果
        start = DateKit.format(DateKit.parse(start), "yyyy-MM-dd");
        end = DateKit.format(DateKit.parse(end), "yyyy-MM-dd");
        List<Map<String, Object>> warnList = provincialMapper.getCityWarningScore(start, end);
        List<Map<String, Object>> heavyList = provincialMapper.getCityHeavyScore(start, end);
        for (Map<String, Object> map : rsList) {
            if (Objects.equals(map.get("model"), "BECS")) continue;
            Map<String, Object> rsMap = new HashMap<>();
            //省台结果
//            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            //计算技巧
//            ExamineKit.getTownJq(map, proRs);
            //地市预警信号综合成绩
            Map<String, Object> warnRs = warnList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            //地市强降水监测
            Map<String, Object> heavyRs = heavyList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            rsMap.put("model", map.get("model"));
            rsMap.put("warning", Objects.isNull(warnRs) ? -999.0 : warnRs.get("zh"));
            rsMap.put("heavy", Objects.isNull(heavyRs) ? -999.0 : heavyRs.get("ts"));
//            rsMap.put("zhzl", getZh(map));
            rsMap.put("zhzl", map.get("zhzl"));
            rsMap.put("zhjq", map.get("zhjq"));
            list.add(rsMap);
        }
        resolveCityList(list);
        return list;
    }

    private void resolveCityList(List<Map<String, Object>> list) {
        if (list.size() == 0) return;
        //成绩排名
        rankRsList(list);
        //地市综合成绩排名
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put("pm", i + 1);
        }
    }

    public List<Map<String, Object>> getCityScore2() {
        List<Map<String, Object>> list = new ArrayList<>();
        String warnStart = "2021-01-01";
        String warnEnd = "2021-11-30";
        String gridStart = "20210301";
        String gridEnd = "20211130";
//        List<Map<String, Object>> rsList = getCityScoreByFtime(gridStart, gridEnd, false);
        //省台结果，以此来算各个地市的技巧
//        List<Map<String, Object>> stList = getCityScoreByFtime(gridStart, gridEnd, true);
        List<Map<String, Object>> rsList = getCityScoreByFtime2(gridStart, gridEnd);
        //地市预警信号结果
        List<Map<String, Object>> warnList = provincialMapper.getCityWarningScore(warnStart, warnEnd);
        //强降水监测结果
        List<Map<String, Object>> heavyList = provincialMapper.getCityHeavyScore(warnStart, warnEnd);
        for (Map<String, Object> map : rsList) {
            if (Objects.equals(map.get("model"), "BECS")) continue;
            Map<String, Object> rsMap = new HashMap<>();
            //省台结果
//            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            //计算技巧
//            ExamineKit.getTownJq(map, proRs);
            //地市预警信号综合成绩
            Map<String, Object> warnRs = warnList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            //地市强降水监测
            Map<String, Object> heavyRs = heavyList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            rsMap.put("model", map.get("model"));
            rsMap.put("warning", Objects.isNull(warnRs) ? -999.0 : warnRs.get("zh"));
            rsMap.put("heavy", Objects.isNull(heavyRs) ? -999.0 : heavyRs.get("ts"));
//            rsMap.put("zhzl", getZh(map));
            rsMap.put("zhzl", map.get("zhzl"));
            rsMap.put("zhjq", map.get("zhjq"));
            list.add(rsMap);
        }
        resolveCityList(list);
        return list;
    }

    private List<Map<String, Object>> getCityScoreByFtime(String start, String end, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = provincialMapper.getCityScore(start, end, fhour);
            } else {
                rsList = provincialMapper.getCityScoreSt(start, end, fhour);
            }
            for (Map<String, Object> map : rsList) {
                double zhValue = getTownZhValue(map);
                map.put("zhjs", Arith.round(zhValue, 1));
                String model = map.get("model").toString();
                Map<String, Object> insideMap = sMap.get(model);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(model, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String model = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("model", model);
            list.add(rsMap);
        }
        return list;
    }

    private List<Map<String, Object>> getCityScoreByFtime2(String start, String end) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"zhjq", "qyjq", "genjq", "baoyujq", "zhjsjq", "maxtjq", "mintjq", "zhzl", "qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList = provincialMapper.getCityScore(start, end, fhour);
            List<Map<String, Object>> stList = provincialMapper.getCityScoreSt(start, end, fhour);
            //计算综合降水和综合质量
            rsList.forEach(item -> {
                item.put("zhjs", Arith.round(getTownZhValue(item), 1));
                item.put("zhzl", getZh(item));
            });
            stList.forEach(item -> {
                item.put("zhjs", Arith.round(getTownZhValue(item), 1));
                item.put("zhzl", getZh(item));
            });
            for (Map<String, Object> map : rsList) {
                //省台结果
                Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
                //计算技巧
                ExamineKit.getTownJq(map, proRs);
                String model = map.get("model").toString();
                Map<String, Object> insideMap = sMap.get(model);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(model, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String model = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Double.isNaN(rs) ? -999.0 : Arith.round(rs, 3));
            }
            rsMap.put("model", model);
            list.add(rsMap);
        }
        return list;
    }

    public List<Map<String, Object>> getForecasterScore(String start, String end) {
//        List<Map<String, Object>> rsList = getForecasterScoreByFtime(start, end, false);
//        List<Map<String, Object>> stList = getForecasterScoreByFtime(start, end, true);
        List<Map<String, Object>> rsList = getForecasterScoreByFtime2(start, end);
        List<Map<String, Object>> warnList = provincialMapper.getForecasterWarningZh(start, end);
        List<Map<String, Object>> heavyList = provincialMapper.getForecasterHeavy(start, end);
        //预报员集合 格式：{预报员}_{地市}
        Set<String> forecasters = new HashSet<>();
        rsList.forEach(item -> forecasters.add(item.get("forecaster").toString()));
//        warnList.forEach(item -> forecasters.add(String.format("%s_%s", item.get("forecaster"), item.get("area"))));
        List<Map<String, Object>> list = new ArrayList<>();
        for (String forecaster : forecasters) {
            Map<String, Object> rsMap = new HashMap<>();
            String[] foreDep = forecaster.split("_");
            //预报员结果
            Map<String, Object> rs = rsList.stream().filter(item -> Objects.equals(item.get("forecaster"), forecaster)).findAny().orElse(null);
            //预报员结果（省台）
//            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("forecaster"), forecaster)).findAny().orElse(null);
            //计算技巧
//            ExamineKit.getTownJq(rs, proRs);
            //预报员预警信号综合成绩
            Map<String, Object> warnRs = warnList.stream().filter(item -> Objects.equals(item.get("forecaster"), foreDep[0]) && Objects.equals(item.get("area"), foreDep[1])).findAny().orElse(null);
            //强降水监测成绩
            Map<String, Object> heavyRs = heavyList.stream().filter(item -> Objects.equals(item.get("forecaster"), foreDep[0]) && Objects.equals(item.get("area"), foreDep[1])).findAny().orElse(null);
            if (Objects.isNull(heavyRs)) heavyRs = heavyList.stream().filter(item -> Objects.equals(item.get("forecaster"), foreDep[1]) && Objects.equals(item.get("area"), foreDep[1])).findAny().orElse(null);
            rsMap.put("forecaster", foreDep[0]);
            rsMap.put("area", foreDep[1]);
            rsMap.put("warning", Objects.isNull(warnRs) ? -999.0 : warnRs.get("zh"));
//            rsMap.put("zhzl", getZh(rs));
            rsMap.put("zhzl", Objects.isNull(rs) ? -999.0 : rs.get("zhzl"));
            rsMap.put("zhjq", Objects.isNull(rs) ? Double.NaN : rs.get("zhjq"));
            rsMap.put("heavy", Objects.isNull(heavyRs) ? -999.0 : heavyRs.get("ts"));
            rsMap.put("bc", Objects.isNull(rs) ? 0 : rs.get("bc"));
            rsMap.put("avgbc", Objects.isNull(rs) ? 0 : rs.get("avgbc"));
            list.add(rsMap);
        }
        resolveForeList(list);
        return list;
    }

    public List<Map<String, Object>> getForecasterScore2() {
        String warnStart = "2021-01-01";
        String warnEnd = "2021-11-30";
        String gridStart = "2021-03-01";
        String gridEnd = "2021-11-30";
        String bcStart = "2021-01-01";
        String bcEnd = "2021-11-30";
//        List<Map<String, Object>> rsList = getForecasterScoreByFtime2(gridStart, gridEnd, bcStart, bcEnd, false);
//        List<Map<String, Object>> stList = getForecasterScoreByFtime2(gridStart, gridEnd, bcStart, bcEnd, true);
        List<Map<String, Object>> rsList = getForecasterScoreByFtime2(gridStart, gridEnd, bcStart, bcEnd);
        List<Map<String, Object>> warnList = provincialMapper.getForecasterWarningZh(warnStart, warnEnd);
        List<Map<String, Object>> heavyList = provincialMapper.getForecasterHeavy(warnStart, warnEnd);
        //预报员集合 格式：{预报员}_{地市}
        Set<String> forecasters = new HashSet<>();
        rsList.forEach(item -> forecasters.add(item.get("forecaster").toString()));
//        warnList.forEach(item -> forecasters.add(String.format("%s_%s", item.get("forecaster"), item.get("area"))));
        List<Map<String, Object>> list = new ArrayList<>();
        for (String forecaster : forecasters) {
            Map<String, Object> rsMap = new HashMap<>();
            String[] foreDep = forecaster.split("_");
            //预报员结果
            Map<String, Object> rs = rsList.stream().filter(item -> Objects.equals(item.get("forecaster"), forecaster)).findAny().orElse(null);
            //预报员结果（省台）
//            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("forecaster"), forecaster)).findAny().orElse(null);
            //计算技巧
//            ExamineKit.getTownJq(rs, proRs);
            //预报员预警信号综合成绩
            Map<String, Object> warnRs = warnList.stream().filter(item -> Objects.equals(item.get("forecaster"), foreDep[0]) && Objects.equals(item.get("area"), foreDep[1])).findAny().orElse(null);
            //强降水监测成绩
            Map<String, Object> heavyRs = heavyList.stream().filter(item -> Objects.equals(item.get("forecaster"), foreDep[0]) && Objects.equals(item.get("area"), foreDep[1])).findAny().orElse(null);
            if (Objects.isNull(heavyRs)) heavyRs = heavyList.stream().filter(item -> Objects.equals(item.get("forecaster"), foreDep[1]) && Objects.equals(item.get("area"), foreDep[1])).findAny().orElse(null);
            rsMap.put("forecaster", foreDep[0]);
            rsMap.put("area", foreDep[1]);
            rsMap.put("warning", Objects.isNull(warnRs) ? -999.0 : warnRs.get("zh"));
//            rsMap.put("zhzl", getZh(rs));
            rsMap.put("zhzl", Objects.isNull(rs) ? Double.NaN : rs.get("zhzl"));
            rsMap.put("zhjq", Objects.isNull(rs) ? Double.NaN : rs.get("zhjq"));
            rsMap.put("heavy", Objects.isNull(heavyRs) ? -999.0 : heavyRs.get("ts"));
            rsMap.put("bc", Objects.isNull(rs) ? 0 : rs.get("bc"));
            rsMap.put("avgbc", Objects.isNull(rs) ? 0 : rs.get("avgbc"));
            list.add(rsMap);
        }
        resolveForeList(list);
        return list;
    }

    private List<Map<String,Object>> getForecasterScoreByFtime2(String gridStart, String gridEnd, String bcStart, String bcEnd, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = provincialMapper.getForecasterScore2(gridStart, gridEnd, bcStart, bcEnd, fhour);
            } else {
                rsList = provincialMapper.getForecasterScoreSt2(gridStart, gridEnd, bcStart, bcEnd, fhour);
            }
            for (Map<String, Object> map : rsList) {
                double zhValue = getTownZhValue(map);
                map.put("zhjs", Arith.round(zhValue, 1));
                String forecaster = String.format("%s_%s", map.get("forecaster"), map.get("department"));
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                insideMap.put("bc", map.get("bc"));
                insideMap.put("avgbc", map.get("avgbc"));
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(forecaster, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String forecaster = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("forecaster", forecaster);
            rsMap.put("bc", map.get("bc"));
            rsMap.put("avgbc", map.get("avgbc"));
            list.add(rsMap);
        }
        return list;
    }

    private List<Map<String,Object>> getForecasterScoreByFtime2(String gridStart, String gridEnd, String bcStart, String bcEnd) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"zhjq", "qyjq", "genjq", "baoyujq", "zhjsjq", "maxtjq", "mintjq", "zhzl", "qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList = provincialMapper.getForecasterScore2(gridStart, gridEnd, bcStart, bcEnd, fhour);
            List<Map<String, Object>> stList = provincialMapper.getForecasterScoreSt2(gridStart, gridEnd, bcStart, bcEnd, fhour);
            //计算综合降水和综合质量
            rsList.forEach(item -> {
                item.put("zhjs", Arith.round(getTownZhValue(item), 1));
                item.put("zhzl", getZh(item));
            });
            stList.forEach(item -> {
                item.put("zhjs", Arith.round(getTownZhValue(item), 1));
                item.put("zhzl", getZh(item));
            });
            for (Map<String, Object> map : rsList) {
                String forecaster = String.format("%s_%s", map.get("forecaster"), map.get("department"));
                //省台结果
                Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(String.format("%s_%s", item.get("forecaster"), item.get("department")), forecaster)).findAny().orElse(null);
                //计算技巧
                ExamineKit.getTownJq(map, proRs);
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                insideMap.put("bc", map.get("bc"));
                insideMap.put("avgbc", map.get("avgbc"));
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(forecaster, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String forecaster = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Double.isNaN(rs) ? -999.0 : Arith.round(rs, 3));
            }
            rsMap.put("forecaster", forecaster);
            rsMap.put("bc", map.get("bc"));
            rsMap.put("avgbc", map.get("avgbc"));
            list.add(rsMap);
        }
        return list;
    }

    private void resolveForeList(List<Map<String, Object>> list) {
        if (list.size() == 0) return;
        //去除班次不达平均班次2/3的预报员
        List<Map<String, Object>> notRankList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int bc = Integer.parseInt(list.get(i).get("bc").toString());
            int avgbc = Integer.parseInt(list.get(i).get("avgbc").toString());
            if (Double.isNaN(Double.parseDouble(list.get(i).get("zhjq").toString()))) list.get(i).put("zhjq", -999.0);
            if (bc < avgbc) {
                Map<String, Object> map = list.get(i);
                map.put("pm", "-");
                notRankList.add(map);
            }
        }
        list.removeAll(notRankList);
        //成绩排名
        rankRsList(list);
        //预报员综合成绩排名
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put("pm", i + 1);
        }
        list.addAll(notRankList);
    }

    private void rankRsList(List<Map<String, Object>> list) {
        //预警信号排名
        String key = "warning";
        String key_per = "warning_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key).toString());
            double value2 = Double.parseDouble(b.get(key).toString());
            return Double.compare(value2, value1);
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put(key + "_pm", i + 1);
        }
        //预警信号根据评分办法，计算出的预报员成绩可能大于100，对此将成绩进行正常化处理（提出了这个问题，暂时没给出解决方案，先这样进行处理）
        double maxWarning = Double.parseDouble(list.get(0).get(key).toString());
        if (maxWarning > 100.0) {
            double scale = maxWarning / 100;
            for (Map<String, Object> map : list) {
                double value = Double.parseDouble(map.get(key).toString());
                if (value == -999.0) {
                    map.put(key_per, -999.0);
                    continue;
                }
                map.put(key_per, Arith.round(value / scale, 1));
            }
            getPerScore(list, key_per, key_per);
        } else {
            getPerScore(list, key, key_per);
        }
        //网格预报TS
        String key2 = "zhzl";
        String key2_per = "zhzl_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key2).toString());
            double value2 = Double.parseDouble(b.get(key2).toString());
            return Double.compare(value2, value1);
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put(key2 + "_pm", i + 1);
        }
        getPerScore(list, key2, key2_per);
        //网格预报技巧
        String key3 = "zhjq";
        String key3_per = "zhjq_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key3).toString());
            double value2 = Double.parseDouble(b.get(key3).toString());
            if (Double.isNaN(value1)) value1 = -999.0;
            if (Double.isNaN(value2)) value2 = -999.0;
            return Double.compare(value2, value1);
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put(key3 + "_pm", i + 1);
        }
        getPerScore(list, key3, key3_per);
        //强降水监测
        String key4 = "heavy";
        String key4_per = "heavy_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key4).toString());
            double value2 = Double.parseDouble(b.get(key4).toString());
            return Double.compare(value2, value1);
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put(key4 + "_pm", i + 1);
        }
        getPerScore(list, key4, key4_per);
        //计算综合成绩（预警信号 * 0.4 + 综合质量 * 0.2 + 综合技巧 * 0.2 + 强降水监测 * 0.1 + 10.0）
        for (Map<String, Object> map : list) {
            double warning = Double.parseDouble(map.get(key_per).toString());
            double zhzl = Double.parseDouble(map.get(key2_per).toString());
            double zhjq = Double.parseDouble(map.get(key3_per).toString());
            double heavy = Double.parseDouble(map.get(key4_per).toString());
            if (warning == -999.0 && zhzl == -999.0 && zhjq == -999.0 && heavy == -999.0) {
                map.put("zh", -999.0);
                continue;
            }
            if (warning == -999) warning = 0.0;
            if (zhzl == -999) zhzl = 0.0;
            if (zhjq == -999) zhjq = 0.0;
            if (heavy == -999) heavy = 0.0;
            double value = warning * 0.4 + zhzl * 0.2 + zhjq * 0.2 + heavy * 0.1 + 10.0;
            map.put("zh", Arith.round(value, 3));
        }
        //根据综合成绩排名
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get("zh").toString());
            double value2 = Double.parseDouble(b.get("zh").toString());
            return Double.compare(value2, value1);
        });
    }

    private void rankRsList_v1(List<Map<String, Object>> list) {
        //预警信号排名
        String key = "warning";
        String key_per = "warning_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key).toString());
            double value2 = Double.parseDouble(b.get(key).toString());
            return Double.compare(value2, value1);
        });
        //预警信号根据评分办法，计算出的预报员成绩可能大于100，对此将成绩进行减法处理（提出了这个问题，暂时没给出解决方案，先这样进行处理）
        double maxWarning = Double.parseDouble(list.get(0).get(key).toString());
        if (maxWarning > 100.0) {
            double scale = maxWarning / 100;
            for (Map<String, Object> map : list) {
                double value = Double.parseDouble(map.get(key).toString());
                if (value == -999.0) {
                    map.put(key_per, -999.0);
                    continue;
                }
                map.put(key_per, Arith.round(value / scale, 1));
            }
        } else {
            double scale = 100.0 / maxWarning;
            for (int i = 0; i < list.size(); i++) {
                double value = Double.parseDouble(list.get(i).get(key).toString());
                if (value == -999.0) {
                    list.get(i).put(key_per, -999.0);
                    continue;
                }
                if (i == 0) {
                    list.get(i).put(key_per, 100.0);
                    continue;
                }
                list.get(i).put(key_per, Arith.round(value * scale, 1));
            }
        }
        //网格预报TS
        String key2 = "zhzl";
        String key2_per = "zhzl_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key2).toString());
            double value2 = Double.parseDouble(b.get(key2).toString());
            return Double.compare(value2, value1);
        });
        double maxZhzl = Double.parseDouble(list.get(0).get(key2).toString());
        double zhzlScale = 100.0 / maxZhzl;
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                list.get(i).put(key2_per, 100.0);
                continue;
            }
            double value = Double.parseDouble(list.get(i).get(key2).toString());
            if (value == -999.0) {
                list.get(i).put(key2_per, -999.0);
                continue;
            }
            list.get(i).put(key2_per, Arith.round(value * zhzlScale, 1));
        }
        //网格预报技巧
        String key3 = "zhjq";
        String key3_per = "zhjq_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key3).toString());
            double value2 = Double.parseDouble(b.get(key3).toString());
            if (Double.isNaN(value1)) value1 = -999.0;
            if (Double.isNaN(value2)) value2 = -999.0;
            return Double.compare(value2, value1);
        });
        double maxZhjq = Double.parseDouble(list.get(0).get(key3).toString());
        double zhjqScale = 100.0 / maxZhjq;
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                list.get(i).put(key3_per, 100.0);
                continue;
            }
            double value = Double.parseDouble(list.get(i).get(key3).toString());
            if (Double.isNaN(value)) {
                list.get(i).put(key3, -999);
                list.get(i).put(key3_per, -999);
                continue;
            }
            list.get(i).put(key3_per, value * zhjqScale < -100.0 ? -100.0 : Arith.round(value * zhjqScale, 1));
        }
        //计算综合成绩（预警信号 * 0.5 + 综合质量 * 0.25 + 综合技巧 * 0.25）
        for (Map<String, Object> map : list) {
            double warning = Double.parseDouble(map.get(key_per).toString());
            double zhzl = Double.parseDouble(map.get(key2_per).toString());
            double zhjq = Double.parseDouble(map.get(key3_per).toString());
            if (warning == -999.0 && zhzl == -999.0 && zhjq == -999.0) {
                map.put("zh", -999.0);
                continue;
            }
            if (warning == -999) warning = 0.0;
            if (zhzl == -999) zhzl = 0.0;
            if (zhjq == -999) zhjq = 0.0;
            double value = warning * 0.5 + zhzl * 0.25 + zhjq * 0.25;
            map.put("zh", Arith.round(value, 1));
        }
        //根据综合成绩排名
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get("zh").toString());
            double value2 = Double.parseDouble(b.get("zh").toString());
            return Double.compare(value2, value1);
        });
    }

    private void rankRsList_v2(List<Map<String, Object>> list) {
        if (list.size() == 0) return;
        //预警信号排名
        String key = "warning";
        String key_per = "warning_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key).toString());
            double value2 = Double.parseDouble(b.get(key).toString());
            return Double.compare(value2, value1);
        });
        //预警信号根据评分办法，计算出的预报员成绩可能大于100，对此将成绩进行减法处理（提出了这个问题，暂时没给出解决方案，先这样进行处理）
        double maxWarning = Double.parseDouble(list.get(0).get(key).toString());
        if (maxWarning > 100.0) {
            double scale = maxWarning / 100;
            for (Map<String, Object> map : list) {
                double value = Double.parseDouble(map.get(key).toString());
                if (value == -999.0) {
                    map.put(key_per, -999.0);
                    continue;
                }
                map.put(key_per, Arith.round(value / scale, 1));
            }
            getPerScore_v1(list, key_per, key_per);
        } else {
            getPerScore_v1(list, key, key_per);
        }
        //网格预报TS
        String key2 = "zhzl";
        String key2_per = "zhzl_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key2).toString());
            double value2 = Double.parseDouble(b.get(key2).toString());
            return Double.compare(value2, value1);
        });
        getPerScore_v1(list, key2, key2_per);
        //网格预报技巧
        String key3 = "zhjq";
        String key3_per = "zhjq_per";
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get(key3).toString());
            double value2 = Double.parseDouble(b.get(key3).toString());
            if (Double.isNaN(value1)) value1 = -999.0;
            if (Double.isNaN(value2)) value2 = -999.0;
            return Double.compare(value2, value1);
        });
        getPerScore_v1(list, key3, key3_per);
        //计算综合成绩（预警信号 * 0.5 + 综合质量 * 0.25 + 综合技巧 * 0.25）
        for (Map<String, Object> map : list) {
            double warning = Double.parseDouble(map.get(key_per).toString());
            double zhzl = Double.parseDouble(map.get(key2_per).toString());
            double zhjq = Double.parseDouble(map.get(key3_per).toString());
            if (warning == -999.0 && zhzl == -999.0 && zhjq == -999.0) {
                map.put("zh", -999.0);
                continue;
            }
            if (warning == -999) warning = 0.0;
            if (zhzl == -999) zhzl = 0.0;
            if (zhjq == -999) zhjq = 0.0;
            double value = warning * 0.5 + zhzl * 0.25 + zhjq * 0.25;
            map.put("zh", Arith.round(value, 1));
        }
        //根据综合成绩排名
        list.sort((a, b) -> {
            double value1 = Double.parseDouble(a.get("zh").toString());
            double value2 = Double.parseDouble(b.get("zh").toString());
            return Double.compare(value2, value1);
        });
    }

    private void getPerScore(List<Map<String, Object>> list, String key, String key_per) {
        //90-100 15%
        double maxPerValue = 100.0;
        double minPerValue = 70.0;
        List<Map<String, Object>> filterList = new ArrayList<>();
        //去掉没有分数的个例
        for (Map<String, Object> map : list) {
            double value = Double.parseDouble(map.get(key).toString());
            if (value == -999.0 || Double.isNaN(value)) {
                map.put(key_per, -999.0);
                continue;
            }
            filterList.add(map);
        }
        if (filterList.size() != 0) {
            if (filterList.size() == 1) {
                filterList.get(0).put(key_per, 100.0);
            } else {
                double maxActValue = Double.parseDouble(filterList.get(0).get(key).toString());
                double minActValue = Double.parseDouble(filterList.get(filterList.size() - 1).get(key).toString());
                filterList.get(0).put(key_per, maxPerValue);
                filterList.get(filterList.size() - 1).put(key_per, minPerValue);
                if (filterList.size() > 2) {
                    double scale = (maxPerValue - minPerValue) / (maxActValue - minActValue);
                    for (int i = 0; i < filterList.size(); i++) {
                        if (i == 0 || i == filterList.size() - 1) continue;
                        if (maxActValue == 0.0 && minActValue == 0.0) {
                            filterList.get(i).put(key_per, 0.0);
                            continue;
                        }
                        double value1 = Double.parseDouble(filterList.get(i).get(key).toString());
                        double value2 = Double.parseDouble(filterList.get(i - 1).get(key).toString());
                        double perValue = Double.parseDouble(filterList.get(i - 1).get(key_per).toString());
                        double value = Arith.round(perValue - (value2 - value1) * scale, 3);
                        filterList.get(i).put(key_per, value);
                    }
                }
            }
        }
    }

    private void getPerScore_v1(List<Map<String, Object>> list, String key, String key_per) {
        //90-100 15%
        double maxPerValue = 100.0;
        double minPerValue = 90.0;
        int num1 = (int) Arith.round(list.size() * 0.15, 0);
        List<Map<String, Object>> newList = list.subList(0, num1);
        List<Map<String, Object>> filterList = new ArrayList<>();
        //去掉没有分数的个例
        for (Map<String, Object> map : newList) {
            double value = Double.parseDouble(map.get(key).toString());
            if (value == -999.0 || Double.isNaN(value)) {
                map.put(key_per, -999.0);
                continue;
            }
            filterList.add(map);
        }
        if (filterList.size() != 0) {
            if (filterList.size() == 1) {
                filterList.get(0).put(key_per, 100.0);
            } else {
                double maxActValue = Double.parseDouble(filterList.get(0).get(key).toString());
                double minActValue = Double.parseDouble(filterList.get(filterList.size() - 1).get(key).toString());
                filterList.get(0).put(key_per, maxPerValue);
                filterList.get(filterList.size() - 1).put(key_per, minPerValue);
                if (filterList.size() > 2) {
                    double scale = (maxPerValue - minPerValue) / (maxActValue - minActValue);
                    for (int i = 0; i < filterList.size(); i++) {
                        if (i == 0 || i == filterList.size() - 1) continue;
                        if (maxActValue == 0.0 && minActValue == 0.0) {
                            filterList.get(i).put(key_per, 0.0);
                            continue;
                        }
                        double value1 = Double.parseDouble(filterList.get(i).get(key).toString());
                        double value2 = Double.parseDouble(filterList.get(i - 1).get(key).toString());
                        double perValue = Double.parseDouble(filterList.get(i - 1).get(key_per).toString());
                        double value = Arith.round(perValue - (value2 - value1) * scale, 1);
                        filterList.get(i).put(key_per, value);
                    }
                }
            }
        }
        //80-90 35%
        maxPerValue = 90.0;
        minPerValue = 80.0;
        int num2 = (int) Arith.round(list.size() * 0.35, 0);
        newList = list.subList(num1, num1 + num2);
        filterList = new ArrayList<>();
        //去掉没有分数的个例
        for (Map<String, Object> map : newList) {
            double value = Double.parseDouble(map.get(key).toString());
            if (value == -999.0 || Double.isNaN(value)) {
                map.put(key_per, -999.0);
                continue;
            }
            filterList.add(map);
        }
        if (filterList.size() != 0) {
            if (filterList.size() == 1) {
                filterList.get(0).put(key_per, 100.0);
            } else {
                double maxActValue = Double.parseDouble(filterList.get(0).get(key).toString());
                double minActValue = Double.parseDouble(filterList.get(filterList.size() - 1).get(key).toString());
                filterList.get(0).put(key_per, maxPerValue);
                filterList.get(filterList.size() - 1).put(key_per, minPerValue);
                if (filterList.size() > 2) {
                    double scale = (maxPerValue - minPerValue) / (maxActValue - minActValue);
                    for (int i = 0; i < filterList.size(); i++) {
                        if (i == 0 || i == filterList.size() - 1) continue;
                        if (maxActValue == 0.0 && minActValue == 0.0) {
                            filterList.get(i).put(key_per, 0.0);
                            continue;
                        }
                        double value1 = Double.parseDouble(filterList.get(i).get(key).toString());
                        double value2 = Double.parseDouble(filterList.get(i - 1).get(key).toString());
                        double perValue = Double.parseDouble(filterList.get(i - 1).get(key_per).toString());
                        double value = Arith.round(perValue - (value2 - value1) * scale, 1);
                        filterList.get(i).put(key_per, value);
                    }
                }
            }
        }
        //70-80 35%
        maxPerValue = 80.0;
        minPerValue = 70.0;
        int num3 = (int) Arith.round(list.size() * 0.35, 0);
        newList = list.subList(num1 + num2, num1 + num2 + num3);
        filterList = new ArrayList<>();
        //去掉没有分数的个例
        for (Map<String, Object> map : newList) {
            double value = Double.parseDouble(map.get(key).toString());
            if (value == -999.0 || Double.isNaN(value)) {
                map.put(key_per, -999.0);
                continue;
            }
            filterList.add(map);
        }
        if (filterList.size() != 0) {
            if (filterList.size() == 1) {
                filterList.get(0).put(key_per, 100.0);
            } else {
                double maxActValue = Double.parseDouble(filterList.get(0).get(key).toString());
                double minActValue = Double.parseDouble(filterList.get(filterList.size() - 1).get(key).toString());
                filterList.get(0).put(key_per, maxPerValue);
                filterList.get(filterList.size() - 1).put(key_per, minPerValue);
                if (filterList.size() > 2) {
                    double scale = (maxPerValue - minPerValue) / (maxActValue - minActValue);
                    for (int i = 0; i < filterList.size(); i++) {
                        if (i == 0 || i == filterList.size() - 1) continue;
                        if (maxActValue == 0.0 && minActValue == 0.0) {
                            filterList.get(i).put(key_per, 0.0);
                            continue;
                        }
                        double value1 = Double.parseDouble(filterList.get(i).get(key).toString());
                        double value2 = Double.parseDouble(filterList.get(i - 1).get(key).toString());
                        double perValue = Double.parseDouble(filterList.get(i - 1).get(key_per).toString());
                        double value = Arith.round(perValue - (value2 - value1) * scale, 1);
                        filterList.get(i).put(key_per, value);
                    }
                }
            }
        }
        //60-70 35%
        maxPerValue = 70.0;
        minPerValue = 60.0;
//        int num4 = (int) Arith.round(list.size() * 0.15, 0);
        newList = list.subList(num1 + num2 + num3, list.size());
        filterList = new ArrayList<>();
        //去掉没有分数的个例
        for (Map<String, Object> map : newList) {
            double value = Double.parseDouble(map.get(key).toString());
            if (value == -999.0 || Double.isNaN(value)) {
                map.put(key_per, -999.0);
                continue;
            }
            filterList.add(map);
        }
        if (filterList.size() != 0) {
            if (filterList.size() == 1) {
                filterList.get(0).put(key_per, 100.0);
            } else {
                double maxActValue = Double.parseDouble(filterList.get(0).get(key).toString());
                double minActValue = Double.parseDouble(filterList.get(filterList.size() - 1).get(key).toString());
                filterList.get(0).put(key_per, maxPerValue);
                filterList.get(filterList.size() - 1).put(key_per, minPerValue);
                if (filterList.size() > 2) {
                    double scale = (maxPerValue - minPerValue) / (maxActValue - minActValue);
                    for (int i = 0; i < filterList.size(); i++) {
                        if (i == 0 || i == filterList.size() - 1) continue;
                        if (maxActValue == 0.0 && minActValue == 0.0) {
                            filterList.get(i).put(key_per, 0.0);
                            continue;
                        }
                        double value1 = Double.parseDouble(filterList.get(i).get(key).toString());
                        double value2 = Double.parseDouble(filterList.get(i - 1).get(key).toString());
                        double perValue = Double.parseDouble(filterList.get(i - 1).get(key_per).toString());
                        double value = Arith.round(perValue - (value2 - value1) * scale, 1);
                        filterList.get(i).put(key_per, value);
                    }
                }
            }
        }
    }


    private List<Map<String, Object>> getForecasterScoreByFtime(String start, String end, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = provincialMapper.getForecasterScore(start, end, fhour);
            } else {
                rsList = provincialMapper.getForecasterScoreSt(start, end, fhour);
            }
            for (Map<String, Object> map : rsList) {
                double zhValue = getTownZhValue(map);
                map.put("zhjs", Arith.round(zhValue, 1));
                String forecaster = String.format("%s_%s", map.get("forecaster"), map.get("department"));
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                insideMap.put("bc", map.get("bc"));
                insideMap.put("avgbc", map.get("avgbc"));
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(forecaster, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String forecaster = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("forecaster", forecaster);
            rsMap.put("bc", map.get("bc"));
            rsMap.put("avgbc", map.get("avgbc"));
            list.add(rsMap);
        }
        return list;
    }

    private List<Map<String, Object>> getForecasterScoreByFtime2(String start, String end) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"zhjq", "qyjq", "genjq", "baoyujq", "zhjsjq", "maxtjq", "mintjq", "zhzl", "qyzql", "qyts", "genzql", "stormzql", "maxt",
                "mint", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int[] fTimes = {24, 48, 72};
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList = provincialMapper.getForecasterScore(start, end, fhour);
            List<Map<String, Object>> stList = provincialMapper.getForecasterScoreSt(start, end, fhour);
            //计算综合降水和综合质量
            rsList.forEach(item -> {
                item.put("zhjs", Arith.round(getTownZhValue(item), 1));
                item.put("zhzl", getZh(item));
            });
            stList.forEach(item -> {
                item.put("zhjs", Arith.round(getTownZhValue(item), 1));
                item.put("zhzl", getZh(item));
            });
            for (Map<String, Object> map : rsList) {
                String forecaster = String.format("%s_%s", map.get("forecaster"), map.get("department"));
                //省台结果
                Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(String.format("%s_%s", item.get("forecaster"), item.get("department")), forecaster)).findAny().orElse(null);
                //计算技巧
                ExamineKit.getTownJq(map, proRs);
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                insideMap.put("bc", map.get("bc"));
                insideMap.put("avgbc", map.get("avgbc"));
                for (String factor : factors) {
                    insideMap.put(factor + "_" + wfhour, map.get(factor));
                }
                sMap.put(forecaster, insideMap);
            }
        }
        Set<Map.Entry<String, Map<String, Object>>> entries = sMap.entrySet();
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rsMap = new HashMap<>();
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            String forecaster = entry.getKey();
            Map<String, Object> map = entry.getValue();
            for (String factor : factors) {
                String key_24 = factor + "_" + 24;
                String key_48 = factor + "_" + 48;
                String key_72 = factor + "_" + 72;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                Double rs = get0_72(d24, d48, d72);
                rsMap.put(factor, Double.isNaN(rs) ? -999.0 : Arith.round(rs, 3));
            }
            rsMap.put("forecaster", forecaster);
            rsMap.put("bc", map.get("bc"));
            rsMap.put("avgbc", map.get("avgbc"));
            list.add(rsMap);
        }
        return list;
    }

    private Double get0_72(Double d24, Double d48, Double d72) {
        double rs = -999;

        if (d24 != -999) {
            rs = 0;
            rs += d24 * 6 / 10;
        }
        if (d48 != -999) {
            if (rs == -999) rs = 0;
            rs += d48 * 3 / 10;
        }
        if (d72 != -999) {
            if (rs == -999) rs = 0;
            rs += d72 * 1 / 10;
        }
        return rs;
    }

    private void resolveFnList(List<Map<String, Object>> rsList) {
        List<Map<String, Object>> list = new ArrayList<>(rsList);
        Double zhzlAvg = 0.0;
        Double zhjqAvg = 0.0;
        for (int i = 0; i < list.size(); i++) {
            double value = Double.parseDouble(list.get(i).get("zhzl").toString());
            if (value == -999.0) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double zhzl = Double.parseDouble(map.get("zhzl").toString());
            double value = zhzl == -999.0 ? 0.0 : zhzl;
            zhzlAvg += value;
        }
        int zhzlCount = list.size();
        list = new ArrayList<>(rsList);
        for (int i = 0; i < list.size(); i++) {
            double value = Double.parseDouble(list.get(i).get("zhjq").toString());
            if (Double.isNaN(value)) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double zhjq = Double.parseDouble(map.get("zhjq").toString());
            double value = Double.isNaN(zhjq) ? 0.0 : zhjq;
            zhjqAvg += value;
        }
        int zhqjCount = list.size();
        for (Map<String, Object> map : rsList) {
            double zhzl = Double.parseDouble(map.get("zhzl").toString());
            double zhjq = Double.parseDouble(map.get("zhjq").toString());
            if (zhzl == -999.0) map.put("zhzl", Arith.round(zhzlAvg / zhzlCount, 2));
            if (Double.isNaN(zhjq)) map.put("zhjq", Arith.round(zhjqAvg / zhqjCount, 2));
        }
    }

    private void getRankedRs(List<Map<String, Object>> rsList) {
        if (rsList.size() == 0) return;
        String key = "zhzl";
        String key_per = "zhzl_per";
        rsList.sort((a, b) -> {
            double zh1 = Double.parseDouble(a.get(key).toString());
            double zh2 = Double.parseDouble(b.get(key).toString());
            return Double.compare(zh2, zh1);
        });
        double zhzl = Double.parseDouble(rsList.get(0).get(key).toString());
        double scale = 100 / zhzl;
        for (int i = 0; i < rsList.size(); i++) {
            if (zhzl == 0) {
                rsList.get(i).put(key_per, -999.0);
                continue;
            }
            if (i == 0) {
                rsList.get(i).put(key_per, 100);
            } else {
                double value = Double.parseDouble(rsList.get(i).get(key).toString());
                rsList.get(i).put(key_per, Arith.round(value * scale, 1));
            }
        }
        String key2 = "zhjq";
        String key2_per = "zhjq_per";
        rsList.sort((a, b) -> {
            double zh1 = Double.parseDouble(a.get(key2).toString());
            double zh2 = Double.parseDouble(b.get(key2).toString());
            return Double.compare(zh2, zh1);
        });
        double zhjq = Double.parseDouble(rsList.get(0).get(key2).toString());
        scale = 100 / zhjq;
        for (int i = 0; i < rsList.size(); i++) {
            if (zhjq == 0) {
                rsList.get(i).put(key2_per, Double.NaN);
                continue;
            }
            if (i == 0) {
                rsList.get(i).put(key2_per, 100);
            } else {
                double value = Double.parseDouble(rsList.get(i).get(key2).toString());
                double v1 = value * scale < -100.0 ? -100.0 : Arith.round(value * scale, 1);
                rsList.get(i).put(key2_per, v1);
            }
        }
    }

    private void resolveShortTermList(List<Map<String, Object>> rsList) {
        List<Map<String, Object>> list = new ArrayList<>(rsList);
        if (list.size() == 1) {
            if (Objects.isNull(rsList.get(0).get("sh"))) rsList.get(0).put("sh", 0);
            if (Objects.isNull(rsList.get(0).get("sg"))) rsList.get(0).put("sg", 0);
            if (Objects.isNull(rsList.get(0).get("sr"))) rsList.get(0).put("sr", 0);
        }
        Double shAvg = 0.0;
        Double sgAvg = 0.0;
        Double srAvg = 0.0;
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i).get("sh"))) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double value = Objects.isNull(map.get("sh")) ? 0.0 : Double.parseDouble(map.get("sh").toString());
            shAvg += value;
        }
        int shCount = list.size();
        list = new ArrayList<>(rsList);
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i).get("sg"))) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double value = Objects.isNull(map.get("sg")) ? 0.0 : Double.parseDouble(map.get("sg").toString());
            sgAvg += value;
        }
        int sgCount = list.size();
        list = new ArrayList<>(rsList);
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i).get("sr"))) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double value = Objects.isNull(map.get("sr")) ? 0.0 : Double.parseDouble(map.get("sr").toString());
            srAvg += value;
        }
        int srCount = list.size();
        for (Map<String, Object> map : rsList) {
            if (Objects.isNull(map.get("sh"))) map.put("sh", Arith.round(shAvg / shCount, 2));
            if (Objects.isNull(map.get("sg"))) map.put("sg", Arith.round(sgAvg / sgCount, 2));
            if (Objects.isNull(map.get("sr"))) map.put("sr", Arith.round(srAvg / srCount, 2));
        }
    }

    private void resolveRsList(List<Map<String, Object>> rsList) {
        List<Map<String, Object>> list = new ArrayList<>(rsList);
        Double warningAvg = 0.0;
        Double publicAvg = 0.0;
        Double rainAvg = 0.0;
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i).get("warning"))) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double value = Objects.isNull(map.get("warning")) ? 0.0 : Double.parseDouble(map.get("warning").toString());
            warningAvg += value;
        }
        int warningCount = list.size();
        list = new ArrayList<>(rsList);
        for (int i = 0; i < list.size(); i++) {
            double value = Double.parseDouble(list.get(i).get("public").toString());
            if (value == -999.0) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double v = Double.parseDouble(map.get("public").toString());
            double value = v == -999.0 ? 0.0 : v;
            publicAvg += value;
        }
        int publicCount = list.size();
        list = new ArrayList<>(rsList);
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i).get("rain"))) {
                list.remove(i);
                break;
            }
        }
        for (Map<String, Object> map : list) {
            double value = Objects.isNull(map.get("rain")) ? 0.0 : Double.parseDouble(map.get("rain").toString());
            rainAvg += value;
        }
        int rainCount = list.size();
        for (Map<String, Object> map : rsList) {
            if (Objects.isNull(map.get("warning")) && warningCount != 0) map.put("warning", Arith.round(warningAvg / warningCount, 2));
            if (Double.parseDouble(map.get("public").toString()) == -999.0 && publicCount != 0) map.put("public", Arith.round(publicAvg / publicCount, 2));
            if (Objects.isNull(map.get("rain")) && rainCount != 0) map.put("rain", Arith.round(rainAvg / rainCount, 2));
        }
    }

    private Double getChiefZh(Map<String, Object> rsMap) {
        Double warning = Objects.isNull(rsMap.get("warning")) ? null : Double.parseDouble(rsMap.get("warning").toString());
        Double weatherPublic = Double.parseDouble(rsMap.get("public").toString());
        Double rain = Objects.isNull(rsMap.get("rain")) ? null : Double.parseDouble(rsMap.get("rain").toString());
        if (Objects.isNull(warning) && weatherPublic == -999.0 && Objects.isNull(rain)) return null;
        if (Objects.isNull(warning)) warning = 0.0;
        if (weatherPublic == -999.0) weatherPublic = 0.0;
        if (Objects.isNull(rain)) rain = 0.0;
        double value = warning * 0.3 + weatherPublic * 0.4 + rain * 0.3;
        return Arith.round(value, 2);
    }

    private double getTownZhValue(Map<String, Object> map) {
        if (Objects.isNull(map)) return -999.0;
        int fjCount = 0;
        int ljCount = 0;
        int ybxCount = 0;
        int zhCount = 0;
        double fjValue = 0.0;
        double ljValue = 0.0;
        double ybxValue = 0.0;
        double zhValue = 0.0;
        for (int i = 1; i <= 6; i++) {
            if (map.get("fj" + i + "zql") != null) {
                fjCount++;
                fjValue += Double.parseDouble(map.get("fj" + i + "zql").toString());
            }
            if (map.get("ljo" + i + "zql") != null) {
                ljCount++;
                ljValue += Double.parseDouble(map.get("ljo" + i + "zql").toString());
            }
        }
        if (map.get("genzql") != null) {
            ybxCount++;
            ybxValue += Double.parseDouble(map.get("genzql").toString());
        }
        if (map.get("stormzql") != null) {
            ybxCount++;
            ybxValue += Double.parseDouble(map.get("stormzql").toString());
        }
        if (fjCount == 0) {
            fjValue = -999.0;
        } else {
            fjValue = fjValue / fjCount;
        }
        if (ljCount == 0) {
            ljValue = -999.0;
        } else {
            ljValue = ljValue / ljCount;
        }
        if (ybxCount == 0) {
            ybxValue = -999.0;
        } else {
            ybxValue = ybxValue / ybxCount;
        }
        if (fjValue != -999.0) {
            zhCount++;
            zhValue += fjValue;
        }
        if (ljValue != -999.0) {
            zhCount++;
            zhValue += ljValue;
        }
        if (ybxValue != -999.0) {
            zhCount++;
            zhValue += ybxValue;
        }
        if (zhCount == 0) {
            zhValue = -999.0;
        } else {
            zhValue = zhValue / zhCount;
        }
        return zhValue;
    }

    private double getZh(Map<String, Object> map) {
        if (Objects.isNull(map)) return -999.0;
        double qy_pc = 0.0;
        double zhjs = 0.0;
        double maxt = 0.0;
        double mint = 0.0;
        if ((Objects.isNull(map.get("qyzql")) || Double.parseDouble(map.get("qyzql").toString()) == -999.0) &&
            (Objects.isNull(map.get("zhjs")) || Double.parseDouble(map.get("zhjs").toString()) == -999.0) &&
            (Objects.isNull(map.get("maxt")) || Double.parseDouble(map.get("maxt").toString()) == -999.0) &&
            (Objects.isNull(map.get("mint"))  || Double.parseDouble(map.get("maxt").toString()) == -999.0)) return -999.0;
        if (!Objects.isNull(map.get("qyzql"))) qy_pc = Double.parseDouble(map.get("qyzql").toString());
        if (!Objects.isNull(map.get("zhjs"))) zhjs = Double.parseDouble(map.get("zhjs").toString());
        if (!Objects.isNull(map.get("maxt"))) maxt = Double.parseDouble(map.get("maxt").toString());
        if (!Objects.isNull(map.get("mint"))) mint = Double.parseDouble(map.get("mint").toString());
        if (qy_pc == -999.0) qy_pc = 0;
        if (zhjs == -999.0) zhjs = 0;
        if (maxt == -999.0) maxt = 0;
        if (mint == -999.0) mint = 0;
        double zhzl = qy_pc * 0.3 + zhjs * 0.45 + maxt * 0.15 + mint * 0.1;
        return Arith.round(zhzl, 3);
    }
}
