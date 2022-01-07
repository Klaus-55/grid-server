package com.galaxy.score.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ga.common.kit.DateKit;
import com.galaxy.score.common.SeriesItem;
import com.galaxy.score.mapper.MediumShortMapper;
import com.galaxy.score.utils.Arith;
import com.galaxy.score.utils.ExamineKit;
import com.galaxy.score.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 中短期预报质量评分service层
 */
@Service
@DS("zhongduan")
public class MediumShortService {

    @Autowired
    private MediumShortMapper mediumShortMapper;
    @Autowired
    private ShortApproachService shortApproachService;

    //预警消息评分
    @DS("fquality")
    public Map<String, Object> warningMessage(String start, String end) {
        Map<String, Object> rsMap = new HashMap<>();
        List<Map<String, Object>> list = mediumShortMapper.warningMessageScore(start, end);
        Set<String> forecasters = new LinkedHashSet<>();
        Set<String> warnTypes = new LinkedHashSet<>();
        warnTypes.add("综合");
        Map<String, Map<String, Double>> sMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            if ("所有级别".equals(map.get("level"))) continue;
            forecasters.add(map.get("forecaster").toString());
            warnTypes.add(map.get("warntype").toString());
        }
        rsMap.put("categories", forecasters);
        for (String forecaster : forecasters) {
            for (String type : warnTypes) {
                double ts = 0.0;
                double count = 0.0;
                for (Map<String, Object> map : list) {
                    if ("所有等级".equals(map.get("level"))) continue;
                    if (forecaster.equals(map.get("forecaster")) && type.equals(map.get("warntype"))) {
                        double weights = getWeightsByLevel(map.get("level").toString());
                        if (map.get("ts") != null) ts += Double.parseDouble(map.get("ts").toString()) * weights;
                        count += weights;
                    }
                }
                if (count != 0) {
                    Map<String, Double> insideMap = sMap.get(forecaster);
                    if (insideMap == null) insideMap = new HashMap<>();
                    insideMap.put(type, Arith.round(ts / count, 2));
                    sMap.put(forecaster, insideMap);
                }
            }
        }
        for (String forecaster : sMap.keySet()) {
            Map<String, Double> insideMap = sMap.get(forecaster);
            double value = 0.0;
            double count = 0.0;
            for (String type : insideMap.keySet()) {
                double weights = getWeightsByType(type);
                value += insideMap.get(type) * weights;
                count += weights;
            }
            insideMap.put("综合", count == 0 ? null : Arith.round(value / count, 2));
        }

        List<SeriesItem> seriesItemList = new ArrayList<>();
        for (String type : warnTypes) {
            SeriesItem seriesItem = new SeriesItem();
            seriesItem.setName(type);
            List<Double> data = new ArrayList<>();
            for (String forecaster : forecasters) {
                Map<String, Double> insideMap = sMap.get(forecaster);
                data.add(insideMap.get(type));
            }
            seriesItem.setData(data);
            seriesItemList.add(seriesItem);
        }
        rsMap.put("series", seriesItemList);
        return rsMap;
    }

    //预警消息评定详情
    @DS("fquality")
    public List<Map<String, Object>> warningDetail(String start, String end, String type, String level, String fdate) {
        return mediumShortMapper.getWarningDetail(start, end, type, level, fdate);
    }

    //降水过程评分
    @DS("fquality")
    public Map<String, Object> rainProgress(String start, String end) {
        start += " 00:00:00";
        end += " 23:59:59";
        Map<String, Object> rsMap = new HashMap<>();
        List<Map<String, Object>> list = mediumShortMapper.getRainProgress(start, end);
        Set<String> forecasters = new LinkedHashSet<>();
        List<Double> data = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String forecaster = map.get("forecaster").toString();
            if (forecaster.contains("---")) continue;
            forecasters.add(forecaster);
            data.add(map.get("ts") == null ? null : Double.parseDouble(map.get("ts").toString()));
        }
        SeriesItem seriesItem = new SeriesItem();
        seriesItem.setName("预报质量");
        seriesItem.setData(data);
        List<SeriesItem> seriesList = new ArrayList<>();
        seriesList.add(seriesItem);
        if (forecasters.size() == 1) seriesList.remove(0);
        rsMap.put("categories", forecasters);
        rsMap.put("series", seriesList);
        return rsMap;
    }

    //降水过程评定详情
    @DS("fquality")
    public List<Map<String, Object>> rainDetail(String start, String end) {
        start += " 00:00:00";
        end += " 23:59:59";
        return mediumShortMapper.getRainDetail(start, end);
    }

    //暴雨公众预报
    @DS("fquality")
    public Map<String, Object> rainstormPublic(String year, String product, String model) {
        Map<String, Object> rsMap = new HashMap<>();
        List<Map<String, Object>> list = mediumShortMapper.getRainstormPublic(year, model);
        String[] magnitudes = {"50", "100", "250"};
        if ("技巧评分".equals(product)) {
            String newModel = "湖南省气象台".equals(model) ? "中央台" : "湖南省气象台";
            List<Map<String, Object>> newList = mediumShortMapper.getRainstormPublic(year, newModel);
            for (Map<String, Object> map : list) {
                boolean flag = true;
                String m = map.get("m").toString();
                for (Map<String, Object> newMap : newList) {
                    if (m.equals(newMap.get("m").toString())) {
                        flag = false;
                        for (String magnitude : magnitudes) {
                            if (map.get("rain" + magnitude) != null && newMap.get("rain" + magnitude) != null) {
                                double value = Double.parseDouble(map.get("rain" + magnitude).toString());
                                double newValue = Double.parseDouble(newMap.get("rain" + magnitude).toString());
                                map.put("rain" + magnitude, Arith.round(value - newValue, 1));
                            } else {
                                map.put("rain" + magnitude, null);
                            }
                        }
                    }
                }
                if (flag) {
                    map.put("rain50", null);
                    map.put("rain100", null);
                    map.put("rain250", null);
                }
            }
        }
        Set<String> categories = new LinkedHashSet<>();
        List<SeriesItem> seriesList = new ArrayList<>();
        for (String magnitude : magnitudes) {
            SeriesItem seriesItem = new SeriesItem();
            seriesItem.setName(magnitude + "mm级" + product);
            List<Double> data = new ArrayList<>();
            for (Map<String, Object> map : list) {
                categories.add(map.get("m").toString() + "月");
                Double value = null;
                try {
                    value = Double.parseDouble(map.get("rain" + magnitude).toString());
                } catch (Exception e) {
                }
                data.add(value);
            }
            seriesItem.setData(data);
            seriesList.add(seriesItem);
        }
        rsMap.put("categories", categories);
        rsMap.put("series", seriesList);
        return rsMap;
    }

    //暴雨公众预报详情
    @DS("fquality")
    public List<Map<String, Object>> rainstormDetail(String start, String end) {
        List<Map<String, Object>> list = mediumShortMapper.getRainstormDetail(start, end);
        for (Map<String, Object> map : list) {
            double forecast = Double.parseDouble(map.get("rain").toString());
            double fact = Double.parseDouble(map.get("factrain").toString());
            String rs = ExamineKit.examineRainstorm(forecast, fact);
            String[] rss = rs.split(",");
            map.put("rain50", rss[0]);
            map.put("rain100", rss[1]);
            map.put("rain250", rss[2]);
        }
        return list;
    }

    private static double getWeightsByLevel(String level) {
        double weights;
        switch (level) {
            case "红色":
                weights = 1.5;
                break;
            case "橙色":
                weights = 0.9;
                break;
            case "黄色":
                weights = 0.5;
                break;
            case "蓝色":
                weights = 0.3;
                break;
            default:
                weights = 0;
                break;
        }
        return weights;
    }

    private static double getWeightsByType(String type) {
        double weights;
        switch (type) {
            case "暴雨":
                weights = 1.5;
                break;
            case "高温":
                weights = 0.8;
                break;
            case "霾":
                weights = 0.2;
                break;
            case "强对流":
                weights = 0.2;
                break;
            default:
                weights = 1;
                break;
        }
        return weights;
    }

    //每日预报评分
    public List<Map<String, Object>> dailyForecast(String start, String end, String fTime, String rainType, String tempType) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (!fTime.contains("h")) {
            int time = Integer.parseInt(fTime);
            String wfhour = (time - 12) + "," + time;
            List<Map<String, Object>> rsList = mediumShortMapper.dailyForecastScore(start, end, wfhour, rainType, tempType);
            for (Map<String, Object> map : rsList) {
                Map<String, Object> rsMap = new HashMap<>();
                rsMap.put("forecaster", map.get("forecaster"));
                rsMap.put("bc", map.get("bc"));
                double zhjs = getZhValue(map);
                map.put("zhjs", zhjs);
                double zh = getZh(map);
                rsMap.put("qy", map.get("qy_pc") == null ? -999.0 : map.get("qy_pc"));
                rsMap.put("ybx", map.get("ybx_pc") == null ? -999.0 : map.get("ybx_pc"));
                rsMap.put("by", map.get("by_pc") == null ? -999.0 : map.get("by_pc"));
                rsMap.put("byfj", ExamineKit.getByfjValue(map));
                rsMap.put("zhjs", Arith.round(zhjs, 1));
                rsMap.put("maxt", map.get("maxt") == null ? -999.0 : map.get("maxt"));
                rsMap.put("mint", map.get("mint") == null ? -999.0 : map.get("mint"));
                rsMap.put("act_zh", Arith.round(zh, 1));
                list.add(rsMap);
            }
        } else {
            dailyForecastByFtime(start, end, fTime, rainType, tempType, list);
        }
        //按综合成绩排序
        list.sort((a, b) -> {
            double zh1 = Double.parseDouble(a.get("act_zh").toString());
            double zh2 = Double.parseDouble(b.get("act_zh").toString());
            return Double.compare(zh2, zh1);
        });
        double scale = 0.0;
        for (int i = 0; i < list.size(); i++) {
            double zh = Double.parseDouble(list.get(i).get("act_zh").toString());
            if (zh == -999.0) {
                list.get(i).put("per_zh", zh);
                continue;
            }
            if (i == 0) {
                list.get(i).put("per_zh", 100.0);
                scale = 100 / zh;
                continue;
            }
            list.get(i).put("per_zh", Arith.round(zh * scale, 1));
        }
        return list;
    }

    //每日预报年度评分
    public List<Map<String, Object>> getScoreByYear(String start, String end, String fTime) {
        int year = DateKit.formatToInt(DateKit.parse(end), "yyyy");
        Date sumStartDate = DateKit.parse(year + "0301");
        String winEndTime = DateKit.format(DateKit.addDay(sumStartDate, -1), "yyyyMMdd");
        String sumStartTime = DateKit.format(sumStartDate, "yyyyMMdd");
        String rainType = "S99";
        String tempType = "S421";
        //冬季评分
        List<Map<String, Object>> winList = new ArrayList<>();
        //非冬季评分
        List<Map<String, Object>> notWinList = new ArrayList<>();
        if (!fTime.contains("h")) {
            int time = Integer.parseInt(fTime);
            String wfhour = (time - 12) + "," + time;
            List<Map<String, Object>> winList2 = mediumShortMapper.dailyForecastScore(start, winEndTime, wfhour, rainType, tempType);
            for (Map<String, Object> map : winList2) {
                Map<String, Object> rsMap = new HashMap<>();
                rsMap.put("forecaster", map.get("forecaster"));
                rsMap.put("bc", map.get("bc"));
                double zhjs = getZhValue(map);
                map.put("zhjs", zhjs);
                double zh = getZh(map);
                rsMap.put("qy", map.get("qy_pc") == null ? -999.0 : map.get("qy_pc"));
                rsMap.put("ybx", map.get("ybx_pc") == null ? -999.0 : map.get("ybx_pc"));
                rsMap.put("by", map.get("by_pc") == null ? -999.0 : map.get("by_pc"));
                rsMap.put("byfj", ExamineKit.getByfjValue(map));
                rsMap.put("zhjs", Arith.round(zhjs, 1));
                rsMap.put("maxt", map.get("maxt") == null ? -999.0 : map.get("maxt"));
                rsMap.put("mint", map.get("mint") == null ? -999.0 : map.get("mint"));
                rsMap.put("act_zh", Arith.round(zh, 1));
                winList.add(rsMap);
            }
            rainType = "S1912";
            List<Map<String, Object>> notWinList2 = mediumShortMapper.dailyForecastScore(sumStartTime, end, wfhour, rainType, tempType);
            for (Map<String, Object> map : notWinList2) {
                Map<String, Object> rsMap = new HashMap<>();
                rsMap.put("forecaster", map.get("forecaster"));
                rsMap.put("bc", map.get("bc"));
                double zhjs = getZhValue(map);
                map.put("zhjs", zhjs);
                double zh = getZh(map);
                rsMap.put("qy", map.get("qy_pc") == null ? -999.0 : map.get("qy_pc"));
                rsMap.put("ybx", map.get("ybx_pc") == null ? -999.0 : map.get("ybx_pc"));
                rsMap.put("by", map.get("by_pc") == null ? -999.0 : map.get("by_pc"));
                rsMap.put("byfj", ExamineKit.getByfjValue(map));
                rsMap.put("zhjs", Arith.round(zhjs, 1));
                rsMap.put("maxt", map.get("maxt") == null ? -999.0 : map.get("maxt"));
                rsMap.put("mint", map.get("mint") == null ? -999.0 : map.get("mint"));
                rsMap.put("act_zh", Arith.round(zh, 1));
                notWinList.add(rsMap);
            }
        } else {
            dailyForecastByFtime(start, winEndTime, fTime, rainType, tempType, winList);
            rainType = "S1912";
            dailyForecastByFtime(sumStartTime, end, fTime, rainType, tempType, notWinList);
        }
        return getZhScore(winList, notWinList);
    }

    private List<Map<String, Object>> getZhScore(List<Map<String, Object>> winList, List<Map<String, Object>> notWinList) {
        String[] factors = {"qy", "ybx", "by", "byfj", "zhjs", "maxt", "mint", "act_zh"};
        List<Map<String, Object>> list = new ArrayList<>();
        //预报员集合
        Set<String> forecasters = new HashSet<>();
        winList.forEach(map -> forecasters.add(map.get("forecaster").toString()));
        notWinList.forEach(map -> forecasters.add(map.get("forecaster").toString()));
        for (String forecaster : forecasters) {
            Map<String, Object> rsMap = new HashMap<>();
            rsMap.put("forecaster", forecaster);
            Map<String, Object> winMap = winList.stream().filter(map -> Objects.equals(forecaster, map.get("forecaster"))).findAny().orElse(null);
            Map<String, Object> notWinMap = notWinList.stream().filter(map -> Objects.equals(forecaster, map.get("forecaster"))).findAny().orElse(null);
            int winBc = Objects.isNull(winMap) ? 0 : Integer.parseInt(winMap.get("bc").toString());
            int notWinBc = Objects.isNull(notWinMap) ? 0 : Integer.parseInt(notWinMap.get("bc").toString());
            rsMap.put("bc", winBc + notWinBc);
            for (String factor : factors) {
                double value = getValueByBc(winMap, notWinMap, factor);
                rsMap.put(factor, value);
            }
            list.add(rsMap);
        }
        //按综合成绩排序
        list.sort((a, b) -> {
            double zh1 = Double.parseDouble(a.get("act_zh").toString());
            double zh2 = Double.parseDouble(b.get("act_zh").toString());
            return Double.compare(zh2, zh1);
        });
        double scale = 0.0;
        for (int i = 0; i < list.size(); i++) {
            double zh = Double.parseDouble(list.get(i).get("act_zh").toString());
            if (zh == -999.0) {
                list.get(i).put("per_zh", zh);
                continue;
            }
            if (i == 0) {
                list.get(i).put("per_zh", 100.0);
                scale = 100 / zh;
                continue;
            }
            list.get(i).put("per_zh", Arith.round(zh * scale, 1));
        }
        return list;
    }

    private double getValueByBc(Map<String, Object> winMap, Map<String, Object> notWinMap, String factor) {
        int winBc = 0;
        int notWinBc = 0;
        double winRs = 0;
        double notWinRs = 0;
        if (!Objects.isNull(winMap)) {
            winBc = Integer.parseInt(winMap.get("bc").toString());
            winRs = Objects.isNull(winMap.get(factor)) ? -999.0 : Double.parseDouble(winMap.get(factor).toString());
        }
        if (!Objects.isNull(notWinMap)) {
            notWinBc = Integer.parseInt(notWinMap.get("bc").toString());
            notWinRs = Objects.isNull(notWinMap.get(factor)) ? -999.0 : Double.parseDouble(notWinMap.get(factor).toString());
        }
        if (winRs == -999.0 && notWinRs == -999.0) return -999.0;
        if (winRs == -999.0) winRs = 0;
        if (notWinRs == -999.0) notWinRs = 0;
        double value = winRs * (winBc / (winBc * 1.0 + notWinBc)) + notWinRs * (notWinBc * 1.0 / (winBc + notWinBc));
        return Arith.round(value, 1);
    }

    //天气公报
    public List<Map<String, Object>> weatherPublic(String start, String end) {
        List<Map<String, Object>> list = mediumShortMapper.getWeatherPublic(start, end);
        for (Map<String, Object> map : list) {
            double zhjs = getTownZhValue(map);
            map.put("zhjs", zhjs);
        }
        return list;
    }

    //城镇预报评分
    public List<Map<String, Object>> townForecastScore(String start, String end, String period, String obtType) {
        if (period.contains("h")) {
            return townForecastScoreByFtime(start, end, period, obtType);
        }
        int time = Integer.parseInt(period);
        String wfhour = (time - 12) + "," + time;
        List<Map<String, Object>> rsList = mediumShortMapper.townForecastScore(start, end, wfhour, obtType);
        //省台结果，以此来算各个地市的技巧
        List<Map<String, Object>> stList = mediumShortMapper.townForecastScoreSt(start, end, wfhour, obtType);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            if (Objects.isNull(proRs)) continue;
            map.put("zhjs", getTownZhValue(map));
            proRs.put("zhjs", getTownZhValue(proRs));
            ExamineKit.getTownJq(map, proRs);
        }
        return rsList;
    }

    private List<Map<String, Object>> townForecastScoreByFtime(String start, String end, String period, String obtType) {
        List<Map<String, Object>> rsList = getTownRsByFtime(start, end, period, obtType, false);
        List<Map<String, Object>> stList = getTownRsByFtime(start, end, period, obtType, true);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("model"), map.get("model"))).findAny().orElse(null);
            if (Objects.isNull(proRs)) continue;
            ExamineKit.getTownJq(map, proRs);
        }
        return rsList;
    }

    private List<Map<String, Object>> getTownRsByFtime(String start, String end, String period, String obtType, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "tmaxozql",
                "tmaxtzql", "tminozql", "tmintzql", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int fHours = Integer.parseInt(period.substring(1));
        int[] fTimes = new int[fHours / 24];
        for (int i = 0; i < fTimes.length; i++) {
            fTimes[i] = (i + 1) * 24;
        }
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = mediumShortMapper.townForecastScore(start, end, fhour, obtType);
            } else {
                rsList = mediumShortMapper.townForecastScoreSt(start, end, fhour, obtType);
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
                String key_96 = factor + "_" + 96;
                String key_120 = factor + "_" + 120;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                double d96 = sMap.get(key_96) == null ? -999 : Double.parseDouble(sMap.get(key_96).toString());
                double d120 = sMap.get(key_120) == null ? -999 : Double.parseDouble(sMap.get(key_120).toString());
                Double rs;
                if (Objects.equals("h72", period)) {
                    rs = get0_72(d24, d48, d72);
                } else {
                    rs = get0_120(d24, d48, d72, d96, d120);
                }
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("model", model);
            list.add(rsMap);
        }
        return list;
    }

    //城镇预报员评分
    public List<Map<String, Object>> townForecasterScore(String start, String end, String period, String obtType, String wfsrc) {
        String cityName = getCityName(wfsrc);
        if (period.contains("h")) {
            List<Map<String, Object>> rsList = getForecasterRsByFtime(start, end, period, obtType, wfsrc, cityName, false);
            List<Map<String, Object>> stList = getForecasterRsByFtime(start, end, period, obtType, wfsrc, cityName, true);
            for (Map<String, Object> map : rsList) {
                Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("forecaster"), map.get("forecaster"))).findAny().orElse(null);
                if (Objects.isNull(proRs)) continue;
                ExamineKit.getTownJq(map, proRs);
            }
            return rsList;
        }
        int time = Integer.parseInt(period);
        String wfhour = (time - 12) + "," + time;
        List<Map<String, Object>> rsList = mediumShortMapper.townForecasterScore(start, end, wfhour, obtType, wfsrc, cityName);
        //省台结果，以此来算各个地市的技巧
        List<Map<String, Object>> stList = mediumShortMapper.townForecasterScoreSt(start, end, wfhour, obtType, wfsrc, cityName);
        for (Map<String, Object> map : rsList) {
            Map<String, Object> proRs = stList.stream().filter(item -> Objects.equals(item.get("forecaster"), map.get("forecaster"))).findAny().orElse(null);
            if (Objects.isNull(proRs)) continue;
            map.put("zhjs", getTownZhValue(map));
            proRs.put("zhjs", getTownZhValue(proRs));
            ExamineKit.getTownJq(map, proRs);
        }
        return rsList;
    }

    private List<Map<String, Object>> getForecasterRsByFtime(String start, String end, String period, String obtType, String wfsrc, String cityName, boolean isSt) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] factors = {"qyzql", "qyts", "genzql", "stormzql", "tmaxozql",
                "tmaxtzql", "tminozql", "tmintzql", "tminmae", "tmaxmae", "fj1zql", "fj2zql",
                "fj3zql", "fj4zql", "fj5zql", "fj6zql", "ljo1zql", "ljo2zql",
                "ljo3zql", "ljo4zql", "ljo5zql", "ljo6zql", "zhjs"};
        int fHours = Integer.parseInt(period.substring(1));
        int[] fTimes = new int[fHours / 24];
        for (int i = 0; i < fTimes.length; i++) {
            fTimes[i] = (i + 1) * 24;
        }
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList;
            if (!isSt) {
                rsList = mediumShortMapper.townForecasterScore(start, end, fhour, obtType, wfsrc, cityName);
            } else {
                rsList = mediumShortMapper.townForecasterScoreSt(start, end, fhour, obtType, wfsrc, cityName);
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
                String key_96 = factor + "_" + 96;
                String key_120 = factor + "_" + 120;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                double d96 = sMap.get(key_96) == null ? -999 : Double.parseDouble(sMap.get(key_96).toString());
                double d120 = sMap.get(key_120) == null ? -999 : Double.parseDouble(sMap.get(key_120).toString());
                Double rs;
                if (Objects.equals("h72", period)) {
                    rs = get0_72(d24, d48, d72);
                } else {
                    rs = get0_120(d24, d48, d72, d96, d120);
                }
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("forecaster", forecaster);
            list.add(rsMap);
        }
        return list;
    }

    private String getCityName(String wfsrc) {
        String name = "";
        switch (wfsrc) {
            case "BECS":
                name = "湖南省";break;
            case "BFXK":
                name = "湘潭"; break;
            case "BFJO":
                name = "吉首";break;
            case "BFDA":
                name = "张家界";break;
            case "BFYE":
                name = "永州";break;
            case "BFZU":
                name = "株洲";break;
            case "BFYY":
                name = "益阳";break;
            case "BFHW":
                name = "怀化";break;
            case "BFUY":
                name = "岳阳";break;
            case "BFSB":
                name = "邵阳";break;
            case "BFHA":
                name = "衡阳";break;
            case "BFCA":
                name = "常德";break;
            case "BFCE":
                name = "郴州";break;
            case "BFCS":
                name = "长沙";break;
            case "BFLD":
                name = "娄底";break;
        }
        return name;
    }

    //根据时段不同权重计算评分 0-72时段 24-6 48-3 72-1；0-120时段 24-10 48-8 72-6 96-2 120-1
    private void dailyForecastByFtime(String start, String end, String fTime, String rainType, String tempType, List<Map<String, Object>> list) {
        int fHours = Integer.parseInt(fTime.substring(1));
        int[] fTimes = new int[fHours / 24];
        for (int i = 0; i < fTimes.length; i++) {
            fTimes[i] = (i + 1) * 24;
        }
        Map<String, Map<String, Object>> sMap = new HashMap<>();
        for (int wfhour : fTimes) {
            String fhour = (wfhour - 12) + "," + wfhour;
            List<Map<String, Object>> rsList = mediumShortMapper.dailyForecastScore(start, end, fhour, rainType, tempType);
            for (Map<String, Object> map : rsList) {
                double zhValue = getZhValue(map);
                map.put("zhjs", Arith.round(zhValue, 1));
                String forecaster = map.get("forecaster").toString();
                Map<String, Object> insideMap = sMap.get(forecaster);
                if (Objects.isNull(insideMap)) insideMap = new HashMap<>();
                insideMap.put("bc", map.get("bc"));
                insideMap.put("qy_" + wfhour, map.get("qy_pc"));
                insideMap.put("ybx_" + wfhour, map.get("ybx_pc"));
                insideMap.put("by_" + wfhour, map.get("by_pc"));
                insideMap.put("byfj_" + wfhour, ExamineKit.getByfjValue(map));
                insideMap.put("zhjs_" + wfhour, Arith.round(zhValue, 1));
                insideMap.put("maxt_" + wfhour, map.get("maxt"));
                insideMap.put("mint_" + wfhour, map.get("mint"));
                insideMap.put("act_zh_" + wfhour, Arith.round(getZh(map), 1));
                sMap.put(forecaster, insideMap);
            }
        }
        getValueByFtime(list, sMap, fTime);
    }

    private void getValueByFtime(List<Map<String, Object>> list, Map<String, Map<String, Object>> sMap, String fTime) {
        String[] factors = {"qy", "ybx", "by", "byfj", "zhjs", "maxt", "mint", "act_zh"};
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
                String key_96 = factor + "_" + 96;
                String key_120 = factor + "_" + 120;
                double d24 = map.get(key_24) == null ? -999 : Double.parseDouble(map.get(key_24).toString());
                double d48 = map.get(key_48) == null ? -999 : Double.parseDouble(map.get(key_48).toString());
                double d72 = map.get(key_72) == null ? -999 : Double.parseDouble(map.get(key_72).toString());
                double d96 = sMap.get(key_96) == null ? -999 : Double.parseDouble(sMap.get(key_96).toString());
                double d120 = sMap.get(key_120) == null ? -999 : Double.parseDouble(sMap.get(key_120).toString());
                Double rs;
                if (Objects.equals("h72", fTime)) {
                    rs = get0_72(d24, d48, d72);
                } else {
                    rs = get0_120(d24, d48, d72, d96, d120);
                }
                rsMap.put(factor, Arith.round(rs, 1));
            }
            rsMap.put("forecaster", forecaster);
            rsMap.put("bc", map.get("bc"));
            list.add(rsMap);
        }
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

    private Double get0_120(Double d24, Double d48, Double d72, Double d96, Double d120) {
        double rs = -999;

        if (d24 != -999) {
            rs = 0;
            rs += d24 * 10 / 27;
        }
        if (d48 != -999) {
            if (rs == -999) rs = 0;
            rs += d48 * 8 / 27;
        }
        if (d72 != -999) {
            if (rs == -999) rs = 0;
            rs += d72 * 6 / 27;
        }
        if (d96 != -999) {
            if (rs == -999) rs = 0;
            rs += d96 * 2 / 27;
        }
        if (d120 != -999) {
            if (rs == -999) rs = 0;
            rs += d120 * 1 / 27;
        }
        return rs;
    }

    private double getZhValue(Map<String, Object> map) {
        int fjCount = 0;
        int ljCount = 0;
        int ybxCount = 0;
        int zhCount = 0;
        double fjValue = 0.0;
        double ljValue = 0.0;
        double ybxValue = 0.0;
        double zhValue = 0.0;
        for (int i = 1; i <= 6; i++) {
            if (map.get("fj" + i + "_pc") != null) {
                fjCount++;
                fjValue += Double.parseDouble(map.get("fj" + i + "_pc").toString());
            }
            if (map.get("lj" + i + "_pc") != null) {
                ljCount++;
                ljValue += Double.parseDouble(map.get("lj" + i + "_pc").toString());
            }
        }
        if (map.get("ybx_pc") != null) {
            ybxCount++;
            ybxValue += Double.parseDouble(map.get("ybx_pc").toString());
        }
        if (map.get("by_pc") != null) {
            ybxCount++;
            ybxValue += Double.parseDouble(map.get("by_pc").toString());
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
        double qy_pc = 0.0;
        double zhjs = 0.0;
        double maxt = 0.0;
        double mint = 0.0;
        if ((Objects.isNull(map.get("qy_pc")) || Double.parseDouble(map.get("qy_pc").toString()) == -999.0) &&
            (Objects.isNull(map.get("zhjs")) || Double.parseDouble(map.get("zhjs").toString()) == -999.0) &&
            (Objects.isNull(map.get("maxt")) || Double.parseDouble(map.get("maxt").toString()) == -999.0) &&
            (Objects.isNull(map.get("mint"))  || Double.parseDouble(map.get("maxt").toString()) == -999.0)) return -999.0;
        if (!Objects.isNull(map.get("qy_pc"))) qy_pc = Double.parseDouble(map.get("qy_pc").toString());
        if (!Objects.isNull(map.get("zhjs"))) zhjs = Double.parseDouble(map.get("zhjs").toString());
        if (!Objects.isNull(map.get("maxt"))) maxt = Double.parseDouble(map.get("maxt").toString());
        if (!Objects.isNull(map.get("mint"))) mint = Double.parseDouble(map.get("mint").toString());
        if (qy_pc == -999.0) qy_pc = 0;
        if (zhjs == -999.0) zhjs = 0;
        if (maxt == -999.0) maxt = 0;
        if (mint == -999.0) mint = 0;
        return qy_pc * 0.3 + zhjs * 0.45 + maxt * 0.15 + mint * 0.1;
    }

    private double getTownZhValue(Map<String, Object> map) {
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

    //模式检验评分
    @DS("model")
    public Map<String, Object> modelScore(String year, String month, String feHour, String type, String rtc, String item) {
        Map<String, Object> rsMap = new HashMap<>();
        String[] models = {"欧洲中心", "T639", "GRAPES_GFS", "华南模式", "华东模式",
                "央台指导", "省台(客观)", "省台(融合前)", "省台(融合后)"};
        List<String> categories = new ArrayList<>(Arrays.asList(models));
        //获取模式检验评分
        List<Map<String, Object>> list = mediumShortMapper.getModelScore(year, month, feHour, type, rtc);
        if (Objects.equals("技巧评分", item)) {
            categories.remove("央台指导");
            Map<String, Object> zytRs = new HashMap<>();
            //获取中央台评分
            for (int i = 0; i < list.size(); i++) {
                if (Objects.equals(list.get(i).get("username"), "央台指导")) {
                    zytRs = list.remove(i);
                }
            }
            for (Map<String, Object> map : list) {
                //晴雨技巧
                Double qy = null;
                Double qy_zyt = null;
                if (!Objects.isNull(map.get("qyzql")) && Double.parseDouble(map.get("qyzql").toString()) < 100) {
                    qy = Double.parseDouble(map.get("qyzql").toString());
                }
                if (!Objects.isNull(zytRs.get("qyzql")) && Double.parseDouble(zytRs.get("qyzql").toString()) < 100) {
                    qy_zyt = Double.parseDouble(zytRs.get("qyzql").toString());
                }
                Double qyjq = null;
                if (!Objects.isNull(qy) && !Objects.isNull(qy_zyt)) {
                    qyjq = Arith.round((qy - qy_zyt) * 100 / (100 - qy_zyt), 2);
                    if (qyjq > 100) qyjq = 100.0;
                    if (qyjq < -100) qyjq = -100.0;
                    map.put("qyjq", qyjq);
                }
//                //晴雨雪技巧
//                Double storm = null;
//                Double storm_zyt = null;
//                if (!Objects.isNull(map.get("stormzql")) || Double.parseDouble(map.get("stormzql").toString()) < 100) {
//                    storm = Double.parseDouble(map.get("stormzql").toString());
//                }
//                if (!Objects.isNull(zytRs.get("stormzql")) || Double.parseDouble(zytRs.get("stormzql").toString()) < 100) {
//                    storm_zyt = Double.parseDouble(zytRs.get("stormzql").toString());
//                }
//                Double stormjq = null;
//                if (!Objects.isNull(storm) && !Objects.isNull(storm_zyt)) {
//                    stormjq = Arith.round( storm - storm_zyt,3);
//                }
                //一般性技巧
                Double gen = null;
                Double gen_zyt = null;
                if (!Objects.isNull(map.get("genzql")) && Double.parseDouble(map.get("genzql").toString()) < 100) {
                    gen = Double.parseDouble(map.get("genzql").toString());
                }
                if (!Objects.isNull(zytRs.get("genzql")) && Double.parseDouble(zytRs.get("genzql").toString()) < 100) {
                    gen_zyt = Double.parseDouble(zytRs.get("genzql").toString());
                }
                Double genjq = null;
                if (!Objects.isNull(gen) && !Objects.isNull(gen_zyt)) {
                    genjq = Arith.round(gen - gen_zyt, 2);
                    if (genjq > 100) genjq = 100.0;
                    if (genjq < -100) genjq = -100.0;
                    map.put("genjq", genjq);
                }
                //暴雨技巧
                Double baoyu = null;
                Double baoyu_zyt = null;
                if (!Objects.isNull(map.get("stormzql")) && Double.parseDouble(map.get("stormzql").toString()) < 100) {
                    baoyu = Double.parseDouble(map.get("stormzql").toString());
                }
                if (!Objects.isNull(zytRs.get("stormzql")) && Double.parseDouble(zytRs.get("stormzql").toString()) < 100) {
                    baoyu_zyt = Double.parseDouble(zytRs.get("stormzql").toString());
                }
                Double baoyujq = null;
                if (!Objects.isNull(baoyu) && !Objects.isNull(baoyu_zyt)) {
                    baoyujq = Arith.round(baoyu - baoyu_zyt, 2);
                    if (baoyujq > 100) baoyujq = 100.0;
                    if (baoyujq < -100) baoyujq = -100.0;
                    map.put("baoyujq", baoyujq);
                }
                //最高温技巧
                Double maxt = null;
                Double maxt_zyt = null;
                if (!Objects.isNull(map.get("tmaxabs")) && Double.parseDouble(map.get("tmaxabs").toString()) < 100) {
                    maxt = Double.parseDouble(map.get("tmaxabs").toString());
                }
                if (!Objects.isNull(zytRs.get("tmaxabs")) && Double.parseDouble(zytRs.get("tmaxabs").toString()) < 100) {
                    maxt_zyt = Double.parseDouble(zytRs.get("tmaxabs").toString());
                }
                Double maxtjq = null;
                if (!Objects.isNull(maxt) && !Objects.isNull(maxt_zyt) && maxt_zyt != 0) {
                    maxtjq = Arith.round((maxt_zyt - maxt) * 100 / maxt_zyt, 2);
                    if (maxtjq > 100) maxtjq = 100.0;
                    if (maxtjq < -100) maxtjq = -100.0;
                    map.put("maxtjq", maxtjq);
                }
                //最低温技巧
                Double mint = null;
                Double mint_zyt = null;
                if (!Objects.isNull(map.get("tminabs")) && Double.parseDouble(map.get("tminabs").toString()) < 100) {
                    mint = Double.parseDouble(map.get("tminabs").toString());
                }
                if (!Objects.isNull(zytRs.get("tminabs")) && Double.parseDouble(zytRs.get("tminabs").toString()) < 100) {
                    mint_zyt = Double.parseDouble(zytRs.get("tminabs").toString());
                }
                Double mintjq = null;
                if (!Objects.isNull(mint) && !Objects.isNull(mint_zyt) && mint_zyt != 0) {
                    mintjq = Arith.round((mint_zyt - mint) * 100 / mint_zyt, 2);
                    if (mintjq > 100) mintjq = 100.0;
                    if (mintjq < -100) mintjq = -100.0;
                    map.put("mintjq", mintjq);
                }
                //综合降水技巧
                Double zh = null;
                Double zh_zyt = null;
                if (!Objects.isNull(map.get("zhjs")) && Double.parseDouble(map.get("zhjs").toString()) < 100) {
                    zh = Double.parseDouble(map.get("zhjs").toString());
                }
                if (!Objects.isNull(zytRs.get("zhjs")) && Double.parseDouble(zytRs.get("zhjs").toString()) < 100) {
                    zh_zyt = Double.parseDouble(zytRs.get("zhjs").toString());
                }
                Double zhjsjq = null;
                if (!Objects.isNull(zh) && !Objects.isNull(zh_zyt)) {
                    zhjsjq = zh - zh_zyt;
                }
                double _zh = Objects.isNull(zhjsjq) ? 0 : zhjsjq;
                double _qy = Objects.isNull(qyjq) ? 0 : qyjq;
                double _maxt = Objects.isNull(maxtjq) ? 0 : maxtjq;
                double _mint = Objects.isNull(mintjq) ? 0 : mintjq;
                double zhjq = Arith.round(_qy * 0.3 + _maxt * 0.2 + _mint * 0.2 + _zh * 0.3, 2);
                map.put("zhjq", zhjq);
                map.put("qyjq", Objects.isNull(qyjq) ? Double.NaN : qyjq);
                map.put("genjq", Objects.isNull(genjq) ? Double.NaN : genjq);
                map.put("baoyujq", Objects.isNull(baoyujq) ? Double.NaN : baoyujq);
                map.put("maxtjq", Objects.isNull(maxtjq) ? Double.NaN : maxtjq);
                map.put("mintjq", Objects.isNull(mintjq) ? Double.NaN : mintjq);
            }
        }
        rsMap.put("categories", categories);
        rsMap.put("data", list);
        return rsMap;
    }

    //降水检验
    public List<Map<String, Object>> rainScore(String start, String end, String fTime, String type) {
        Map<String, String> modelMap = shortApproachService.getModelName();
        List<Map<String, Object>> list = mediumShortMapper.rainScore(start, end, fTime, type);
        Map<String, Map<String, Object>> zytMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            System.out.println(map);
            String wfsrc = map.get("wfsrc").toString();
            String zwname = modelMap.get(wfsrc);
            map.put("zwname", zwname == null ? wfsrc : zwname);
            map.put("zhjs", getZhValue(map));
            if (Objects.equals("BABJ", wfsrc)) {
                String wfhour = map.get("wfhour").toString();
                zytMap.put(wfhour, map);
            }
        }
        for (Map<String, Object> map : list) {
            String wfsrc = map.get("wfsrc").toString();
            String wfhour = map.get("wfhour").toString();
            Map<String, Object> zytRs = zytMap.get(wfhour);
            if (Objects.equals("BABJ", wfsrc) || zytRs == null) continue;
            //晴雨技巧
            if (!Objects.isNull(map.get("pc")) && !Objects.isNull(zytRs.get("pc"))) {
                double pcf = Double.parseDouble(map.get("pc").toString());
                double pcn = Double.parseDouble(zytRs.get("pc").toString());
                double pc_jq = Arith.round((pcf - pcn) / (100 - pcn), 2);
                map.put("pc_jq", pc_jq);
            }
            //一般性降水技巧
            if (!Objects.isNull(map.get("ybx_pc")) && !Objects.isNull(zytRs.get("ybx_pc"))) {
                double genf = Double.parseDouble(map.get("ybx_pc").toString());
                double genn = Double.parseDouble(zytRs.get("ybx_pc").toString());
                double gen_jq = Arith.round(genf - genn, 2);
                map.put("gen_jq", gen_jq);
            }
            //暴雨以上技巧
            if (!Objects.isNull(map.get("by_pc")) && !Objects.isNull(zytRs.get("by_pc"))) {
                double stormf = Double.parseDouble(map.get("by_pc").toString());
                double stormn = Double.parseDouble(zytRs.get("by_pc").toString());
                double storm_jq = Arith.round(stormf - stormn, 2);
                map.put("storm_jq", storm_jq);
            }
            //强降水技巧
            if (!Objects.isNull(map.get("qjs_pc")) && !Objects.isNull(zytRs.get("qjs_pc"))) {
                double heavyf = Double.parseDouble(map.get("qjs_pc").toString());
                double heavyn = Double.parseDouble(zytRs.get("qjs_pc").toString());
                double heavy_jq = Arith.round(heavyf - heavyn, 2);
                map.put("heavy_jq", heavy_jq);
            }
        }
        return list;
    }

    public List<Map<String, Object>> rainScore2(String start, String end, String fTime, String type) {
        List<Map<String, Object>> rsList = new ArrayList<>();
        Date startDate = DateKit.parse(start);
        Date endDate = DateKit.parse(end);
        if (endDate.getTime() - startDate.getTime() > 48 * 60 * 60 *1000) endDate = DateKit.addHour(startDate, 48);
        while (!startDate.after(endDate)) {
            int hour = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60) + 24);
            String dateTime = DateKit.format(startDate, "yyyyMMdd");
            List<Map<String, Object>> list = mediumShortMapper.rainScore2(dateTime, fTime, hour, type);
            rsList.addAll(list);
            startDate = DateKit.addHour(startDate, 24);
        }
        System.out.println(rsList.size());
//        System.out.println(DateKit.format(startDate, "yyyy-MM-dd HH:mm:ss"));
//        System.out.println(DateKit.format(endDate, "yyyy-MM-dd HH:mm:ss"));
        return rsList;
    }

    //温度检验
    public List<Map<String, Object>> tempScore(String start, String end, String fTime, String type, String wfhours, String facname) {
        Map<String, String> modelMap = shortApproachService.getModelName();
        List<Map<String, Object>> list = mediumShortMapper.tempScore(start, end, fTime, type, wfhours, facname);
        //中央台的查询结果过滤出来放到map集合中
        Map<String, Object> babjMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            String wfsrc = map.get("wfsrc").toString();
            String zwname = modelMap.get(wfsrc);
            map.put("zwname", zwname == null ? wfsrc : zwname);
            if (Objects.equals("BABJ", map.get("wfsrc"))) {
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

    @DS("live")
    public List<Map<String, Object>> getLiveObtData(String ddatetime, String obtFacname, String obtType) {
        if (Objects.equals(obtFacname, "win_s_inst")) obtFacname = "win_s_inst_max";
        return mediumShortMapper.getLiveObtData(ddatetime, obtFacname, obtType);
    }

    @DS("live")
    public List<Map<String, Object>> getLiveObtData_cal(String begin_ddatetime, String cal_facname, String cal_fun, String ddatetime, String obtFacname, String obtType) {
        return mediumShortMapper.getLiveObtData_cal(begin_ddatetime, cal_facname, cal_fun, ddatetime, obtFacname, obtType);
    }

    @DS("model")
    public List<Map<String, Object>> getWFGribData(String wfinterval, String wfhour, String facname, String wfsrc, String wfdatetime, String datatype) {
        return mediumShortMapper.getWFGribData(wfinterval, wfhour, facname, wfsrc, wfdatetime, datatype);
    }

    @DS("model")
    public List<Map<String, Object>> getChartLiveHourObt(String obtFacname, String obtid, String startDatetime, String endDatetime) {
        return mediumShortMapper.getChartLiveHourObt(obtFacname, obtid, startDatetime, endDatetime);
    }

    @DS("model")
    public List<Map<String, Object>> getChartLiveHourObtCal(String cal_fun, String obtFacname, String obtid, String startDatetime, String endDatetime) {
        return mediumShortMapper.getChartLiveHourObtCal(cal_fun, obtFacname, obtid, startDatetime, endDatetime);
    }

    @DS("master")
    public List<Map<String, Object>> getChartLiveHourGrib(Integer index, String table, String facname, String startDatetime, String endDatetime) {
        return mediumShortMapper.getChartLiveHourGrib(index, table, facname, startDatetime, endDatetime);
    }

    @DS("model")
    public List<Map<String, Object>> getChartWfGrib(Integer index, String facname, String srcCode, String wfdatetime) {
        return mediumShortMapper.getChartWfGrib(index, facname, srcCode, wfdatetime);
    }

    @DS("master")
    public List<Map<String, Object>> getLiveGribData(String ddatetime, String facname, String table) {
        return mediumShortMapper.getLiveGribData(ddatetime, facname, table);
    }

    public void downloadWord(String fileName, HttpServletResponse response) {
        String wordPath = "E:\\湖南省智能网格\\02_project\\参考文档";
        String filePath = wordPath + File.separator + fileName;
        FileUtils.downloadFile(response, filePath);
    }
}
