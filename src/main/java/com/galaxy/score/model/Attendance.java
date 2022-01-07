package com.galaxy.score.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author hfr
 * @Date 2021/10/11 10:46
 */
@Data
@TableName("tb_attendance")
public class Attendance implements Serializable {
    private static final long serialVersionUID = 1424098711849L;

    @TableId
    private Long id;
    private Date datetime;
    private String department;
    private String forecaster;
    private String createdby;
    private Date crttime;
}
