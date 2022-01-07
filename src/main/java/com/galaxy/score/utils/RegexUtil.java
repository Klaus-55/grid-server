package com.galaxy.score.utils;

import com.galaxy.score.model.WarningModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author hfr
 * @Date 2021/12/27 16:10
 */
public class RegexUtil {
    public static void main(String[] args) {
//        String text = "麻阳县自然资源局、麻阳县气象局8月24日9时20分联合发布地质灾害气象风险短临预警：24日3时10分至9时10分麻阳县岩门镇（黄土溪）过去6小时雨量已达129.4毫米，预计未来1小时降雨量将有15毫米，已达到乡镇强降雨橙色警报等级。麻阳县高村镇（谷达坡）过去6小时雨量已达122.3毫米，预计未来1小时降雨量将有10毫米，已达到乡镇强降雨橙色警报等级。麻阳县郭公坪镇过去6小时雨量已达89.9毫米，已达到乡镇强降雨黄色警报等级。麻阳县文昌阁乡（西晃山）过去6小时雨量已达55.8毫米，已达到乡镇强降雨黄色警报等级。麻阳县板栗树乡过去6小时雨量已达107.2毫米，预计未来1小时降雨量将有10毫米，已达到乡镇强降雨橙色警报等级。麻阳县尧市镇（拖冲）过去6小时雨量已达57毫米，已达到乡镇强降雨黄色警报等级。根据地质灾害成灾规律和孕灾条件分析预测，上述地区地质灾害成灾可能性大，请重点防范。\n";
//        String text = "凤凰县气象局和自然资源局5月19日5时11分发布乡镇强降雨黄色警报：5月18日23时09分至5月19日5时09分廖家桥镇（廖家桥本站）过去6小时累计雨量已达50.2毫米（更正为过去6小时），未来1小时降雨仍有10毫米以上，请加强防范山洪地质灾害。";
        String text = "绥宁县气象台9月6日15时5分发布乡镇强降雨警报：6日14时至15时绥宁县唐家坊镇曾家湾站累计雨量已达37.9毫米(橙色级别)，未来1小时降雨仍有15毫米，绥宁县寨市乡云雾山站累计雨量已达31.1毫米(橙色级别)，未来1小时降雨仍有5.1毫米，请加强防范山洪地质灾害。";
        List<WarningModel> list = new ArrayList<>();
        getWarningModels(text, list);
        for (WarningModel warningModel : list) {
            System.out.println(warningModel);
        }
        String startTime = "9月6日15时5分";
        String endTime = "9月6日16时5分";

    }

    private static void getWarningModels(String text, List<WarningModel> list) {
        String[] strRs = text.split("。");
        for (String str : strRs) {
            String district = "";
            String past = "";
            String pastDiff = "";
            String future = "";
            String futureDiff = "";
            String pastRain = "";
            String futureRain = "";
//            Pattern p = Pattern.compile("(：)((\\d+月)?\\d+日\\d+时\\d+分至(\\d+月)?(\\d+日)?\\d+时\\d+分[，,]?)?(\\W+)(\\d*[月小累])");
//            Pattern p = Pattern.compile("(：)((\\d+月)?\\d+日\\d+时\\d+分至(\\d+月)?(\\d+日)?\\d+时\\d+分[，,]?)?(\\W+)(\\d*)(\\W+(?:已达)?)([0-9]\\d*\\.?\\d*)(\\W+(?:\\d+)?(?:\\W+)?未来)?(\\d+)?(\\W+)?([0-9]\\d*\\.?\\d*)?");
            Pattern p = Pattern.compile("(：)((\\d+月)?\\d+日\\d+时(?:\\d+分)?至(\\d+月)?(\\d+日)?\\d+时(?:\\d+分)?[，,]?)?(\\W+)(\\d*)?([小]?[时]?[累]?[计]?[雨|降][量|水](?:已达)?)([0-9]\\d*\\.?\\d*)(\\W+(?:\\d+)?(?:\\W+)?未来)?(\\d+)?(\\W+)?([0-9]\\d*\\.?\\d*)?");
            Matcher matcher = p.matcher(str);
            while (matcher.find()) {
                district = matcher.group(6);
                past = "1";
                pastDiff = matcher.group(7);
                pastRain = matcher.group(9);
                if (!Objects.isNull(matcher.group(10))) {
                    future = "1";
                    futureDiff = matcher.group(11);
                    futureRain = matcher.group(13);
                }
            }
            if (Objects.equals(district, "")) {
//                p = Pattern.compile("(\\W+)(过去\\d*小时)");
                p = Pattern.compile("(\\W+)(\\d*)(\\W+已达)([0-9]\\d*\\.?\\d*)(\\W+未来)?(\\d+)?(\\W+)?([0-9]\\d*\\.?\\d*)?");
                matcher = p.matcher(str);
                while (matcher.find()) {
                    district = matcher.group(1);
                    past = "1";
                    pastDiff = matcher.group(2);
                    pastRain = matcher.group(4);
                    if (!Objects.isNull(matcher.group(5))) {
                        future = "1";
                        futureDiff = matcher.group(6);
                        futureRain = matcher.group(8);
                    }
                }
            }
            if (Objects.equals(district, "")) continue;
            district = district.replace("过去", "");
            district = district.replace("累计", "");
            WarningModel wm = new WarningModel();
            wm.setDistrict(district);
            wm.setPast(past);
            wm.setPastDiff(pastDiff);
            wm.setPastRain(pastRain);
            if (!Objects.equals(future, "")) {
                wm.setFuture(future);
                wm.setFutureDiff(futureDiff);
                wm.setFutureRain(futureRain);
            }
            getTimes(wm, text);
            getLevel(wm, str);
            list.add(wm);
        }
        findMoreDistricts(text, list);
    }

    private static void findMoreDistricts(String text, List<WarningModel> list) {
//        Pattern p = Pattern.compile("([^分][，,])(\\W+)(\\d*)(小时累计雨量已达)([0-9]\\d*\\.?\\d*)");
        Pattern p = Pattern.compile("([^分][，,；])(\\W+)(\\d*)?([小]?[时]?[累]?[计]?[雨|降][量|水](?:已达)?)([0-9]\\d*\\.?\\d*)(\\W+未来)?(\\d+)?(\\W+)?([0-9]\\d*\\.?\\d*)?");
        Matcher matcher = p.matcher(text);
        while (matcher.find()) {
            WarningModel wm = new WarningModel();
            getTimes(wm, text);
            String district = matcher.group(2).replace("过去", "");
            WarningModel warningModel = list.stream().filter(item -> district.contains(item.getDistrict())).findAny().orElse(null);
            if (!Objects.isNull(warningModel)) continue;
            wm.setDistrict(district);
            String hour = matcher.group(3);
            String rain = matcher.group(5);
            String level = getLevelByRain(hour, rain);
            wm.setPast("1");
            wm.setPastDiff(hour);
            wm.setPastRain(rain);
            wm.setLevel(level);
            if (!Objects.isNull(matcher.group(6))) {
                wm.setFuture("1");
                wm.setFutureDiff(matcher.group(7));
                wm.setFutureRain(matcher.group(9));
            }
            list.add(wm);
        }
    }


    private static void getLevel(WarningModel wm, String str) {
        String level = "";
        Pattern p = Pattern.compile("\\W色");
        Matcher matcher = p.matcher(str);
        while (matcher.find()) {
            level = matcher.group();
        }
        if (Objects.equals(wm.getPastDiff(), "")) {

        }
        if (Objects.equals(level, "")) {
            p = Pattern.compile("(\\d+)(小时累计雨量已达)([0-9]\\d*\\.?\\d*)(毫米)");
            matcher = p.matcher(str);
            String hour = "";
            String rain = "";
            while (matcher.find()) {
                hour = matcher.group(1);
                rain = matcher.group(3);
            }
            level = getLevelByRain(hour, rain);
        }
        wm.setLevel(level);
    }

    private static String getLevelByRain(String hour, String rain) {
        String level = "";
        if (Objects.equals(rain, "")) return "";
        double value = Double.parseDouble(rain);
        if (Objects.equals(hour, "1")) {
            if (value >= 30.0) {
                level = "橙色";
            } else if (value >= 50.0) {
                level = "红色";
            }
        } else if (Objects.equals(hour, "3")) {
            if (value >= 50.0) {
                level = "橙色";
            } else if (value >= 100.0) {
                level = "红色";
            }
        } else if (Objects.equals(hour, "6")) {
            if (value >= 50.0) {
                level = "黄色";
            } else if (value >= 100.0) {
                level = "橙色";
            } else if (value >= 150.0) {
                level = "红色";
            }
        } else if (Objects.equals(hour, "12")) {
            if (value >= 100.0) {
                level = "黄色";
            } else if (value >= 150.0) {
                level = "橙色";
            } else if (value >= 200.0) {
                level = "红色";
            }
        }
        return level;
    }

    private static void getTimes(WarningModel wm, String text) {
        Pattern p = Pattern.compile("(\\d+月\\d+日\\d+时\\d+分)(\\W+：(?:\\W+)?)((\\d+月)?(\\d+日)?\\d+时\\d+分)?(至)?((\\d+月)?(\\d+日)?\\d+时\\d+分)?");
        Matcher matcher = p.matcher(text);
        String depTime = "";
        String startTime = "";
        String endTime = "";
        while (matcher.find()) {
            depTime = matcher.group(1);
            startTime = matcher.group(3);
            endTime = matcher.group(7);
        }
        if (Objects.isNull(startTime)) {
            endTime = depTime;
            p = Pattern.compile("(\\d*)(小时)");
            matcher = p.matcher(text);
            while (matcher.find()) {
                int hours = Integer.parseInt("-" + matcher.group(1));
                SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH时mm分");
                try {
                    Date date = sdf.parse(depTime);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.HOUR_OF_DAY, hours);
                    startTime = sdf.format(cal.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        String month = depTime.substring(0, depTime.indexOf("月") + 1);
        String day = depTime.substring(depTime.indexOf("月") + 1, depTime.indexOf("日") + 1);
        if (!startTime.contains("日")) startTime = day + startTime;
        if (!startTime.contains("月")) startTime = month + startTime;
        if (!endTime.contains("日")) endTime = day + endTime;
        if (!endTime.contains("月")) endTime = month + endTime;
        wm.setDepTime(depTime);
        wm.setStartTime(startTime);
        wm.setEndTime(endTime);
    }


}
