package com.galaxy.score.utils;

import java.util.Map;
import java.util.Objects;

/**
 检验工具类
 */
public class ExamineKit {
    private ExamineKit() {

    }

    private static int rainToLevel(double r){
        double[] rainweights = {0.1, 25 ,50, 100, 250}; //24小时量级
        int level = -1;
        for(int i = 0; i < rainweights.length; i++){
            if (i != (rainweights.length-1)) {
                if (r >= rainweights[i] && r < rainweights[i + 1] ) {
                    level = i;
                    break;
                }
            } else {
                if (r>=rainweights[i]) {
                    level = i;
                    break;
                }
            }
        }
        return level;
    }

    public static String examineRainstorm (double forecast, double fact) {
        String rs = "";
        int foreLevel = rainToLevel(forecast);
        int factLevel = rainToLevel(fact);
        if (foreLevel >= 2) {
            if (factLevel <= 2) {
                rs = "NF,-,-";
            } else if (factLevel == 3) {
                if (foreLevel == 4) {
                    rs = "NF,NF,NC";
                } else {
                    rs = "NF,NF,-";
                }
            } else {
                if (foreLevel == 2) {
                    rs = "NF,NF,NC";
                } else {
                    rs = "NF,NF,NF";
                }
            }
        } else if (factLevel >= 2) {
            if (foreLevel == 0 || foreLevel == -1) {
                if (factLevel == 2) {
                    rs = "NC,-,-";
                } else if (factLevel == 3) {
                    rs = "NC,NC,-";
                } else {
                    rs = "NC,NC,NC";
                }
            } else if (foreLevel == 1) {
                if (factLevel == 2) {
                    rs = "NF,-,-";
                } else if (factLevel == 3) {
                    rs = "NF,NC,-";
                } else if (factLevel == 4) {
                    rs = "NF,NC,NC";
                }
            }
        }
        return rs;
    }

    public static void getTownJq(Map<String, Object> map, Map<String, Object> proRs) {
        if (Objects.isNull(map) || Objects.isNull(proRs)) return;
        //晴雨技巧
        Double jq = null;
        Double jq_st = null;
        if (map.get("qyzql") != null) jq = Double.parseDouble(map.get("qyzql").toString());
        if (proRs.get("qyzql") != null) jq_st = Double.parseDouble(proRs.get("qyzql").toString());
        if (jq != null && jq_st != null && jq != -999 && jq_st != -999) {
            if (jq_st == 100.0) {
                map.put("qyjq", 0);
            } else {
                map.put("qyjq", Arith.round((jq - jq_st) / (100 - jq_st), 2));
            }
        } else {
            map.put("qyjq", Double.NaN);
        }
        //一般性技巧
        jq = null;
        jq_st = null;
        if (map.get("genzql") != null) jq = Double.parseDouble(map.get("genzql").toString());
        if (proRs.get("genzql") != null) jq_st = Double.parseDouble(proRs.get("genzql").toString());
        if (jq != null && jq_st != null && jq != -999 && jq_st != -999) {
            map.put("genjq", Arith.round(jq - jq_st, 2));
        } else {
            map.put("genjq", Double.NaN);
        }
        //暴雨技巧
        jq = null;
        jq_st = null;
        if (map.get("stormzql") != null) jq = Double.parseDouble(map.get("stormzql").toString());
        if (proRs.get("stormzql") != null) jq_st = Double.parseDouble(proRs.get("stormzql").toString());
        if (jq != null && jq_st != null && jq != -999 && jq_st != -999) {
            map.put("baoyujq", Arith.round(jq - jq_st, 2));
        } else {
            map.put("baoyujq", Double.NaN);
        }
        //最高温技巧
        jq = null;
        jq_st = null;
        if (map.get("tmaxmae") != null) jq = Double.parseDouble(map.get("tmaxmae").toString());
        if (proRs.get("tmaxmae") != null) jq_st = Double.parseDouble(proRs.get("tmaxmae").toString());
        if (jq != null && jq_st != null && jq != -999 && jq_st != -999) {
            map.put("maxtjq", Arith.round((jq_st - jq) * 100 / jq_st, 2));
        } else {
            map.put("maxtjq", Double.NaN);
        }
        //最低温技巧
        jq = null;
        jq_st = null;
        if (map.get("tminmae") != null) jq = Double.parseDouble(map.get("tminmae").toString());
        if (proRs.get("tminmae") != null) jq_st = Double.parseDouble(proRs.get("tminmae").toString());
        if (jq != null && jq_st != null && jq != -999 && jq_st != -999) {
            map.put("mintjq", Arith.round((jq_st - jq) * 100 / jq_st, 2));
        } else {
            map.put("mintjq", Double.NaN);
        }
        //综合降水技巧
        jq = null;
        jq_st = null;
        if (map.get("zhjs") != null) jq = Double.parseDouble(map.get("zhjs").toString());
        if (proRs.get("zhjs") != null) jq_st = Double.parseDouble(proRs.get("zhjs").toString());
        if (jq != null && jq_st != null && jq != -999 && jq_st != -999) {
            map.put("zhjsjq", Arith.round(jq - jq_st, 2));
        } else {
            map.put("zhjsjq", Double.NaN);
        }
        double zhjq;
        double _zh = Double.isNaN(Double.parseDouble(map.get("zhjsjq").toString())) ? Double.NaN : Double.parseDouble(map.get("zhjsjq").toString());
        double _qy = Double.isNaN(Double.parseDouble(map.get("qyjq").toString())) ? Double.NaN: Double.parseDouble(map.get("qyjq").toString());
        double _maxt = Double.isNaN(Double.parseDouble(map.get("maxtjq").toString())) ? Double.NaN : Double.parseDouble(map.get("maxtjq").toString());
        double _mint = Double.isNaN(Double.parseDouble(map.get("mintjq").toString())) ? Double.NaN : Double.parseDouble(map.get("mintjq").toString());
        if (Double.isNaN(_zh) && Double.isNaN(_qy) && Double.isNaN(_maxt) && Double.isNaN(_mint)) {
            zhjq = Double.NaN;
        } else {
            _zh = Double.isNaN(_zh) ? 0 : _zh;
            _qy = Double.isNaN(_qy) ? 0 : _qy;
            _maxt = Double.isNaN(_maxt) ? 0 : _maxt;
            _mint = Double.isNaN(_mint) ? 0 : _mint;
            zhjq = Arith.round(_qy * 0.3 + _maxt * 0.15 + _mint * 0.1 + _zh * 0.45, 3);
        }
        map.put("zhjq", zhjq);
    }

    public static double getByfjValue(Map<String, Object> map) {
        if (Objects.isNull(map)) return -999.0;
        int byfjCount = 0;
        double byfjValue = 0.0;
        for (int i = 4; i <= 6; i++) {
            if (map.get("fj" + i + "_pc") != null) {
                byfjCount++;
                byfjValue += Double.parseDouble(map.get("fj" + i + "_pc").toString());
            }
        }
        if (byfjCount == 0) {
            byfjValue = -999.0;
        } else {
            byfjValue = byfjValue / byfjCount;
        }
        return Arith.round(byfjValue, 1);
    }
}
