package com.galaxy.score.model;

import lombok.Data;

/**
 * @Author hfr
 * @Date 2021/12/27 15:51
 */
@Data
public class WarningModel {
    private String depTime;     //预警发布时间
    private String startTime;   //开始时间
    private String endTime;     //结束时间
    private String district;    //影响区域
    private String level;       //级别
    private String past;        //1:过去
    private String pastDiff;    //1小时，3小时，6小时,12小时
    private String future;      //1:未来
    private String futureDiff;  //1小时，3小时，6小时,12小时
    private String pastRain;    //过去降水量
    private String futureRain;  //未来降水量
}
