package com.galaxy.score.utils;

import com.jfinal.kit.Kv;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * @Author hfr
 * @Date 2021/3/17 11:07
 */
public class GradsKit {
    private static Engine engine = null;

    private static Engine getEngine() {
        if (engine == null) {
            engine = Engine.create("grads");
            engine.setBaseTemplatePath(PathKit.getRootClassPath().replace("test-classes", "classes"));
        }
        return engine;
    }

    public static boolean outForecastGs() {
        boolean b = false;
        Template template = getEngine().getTemplate("grads_template/forecast/forecast_r03h_gd.gs");
        Kv cond = Kv.create();
        cond.put("ncFile", "E:\\yhxw\\mnt\\highmodel\\forecast\\szgridgame\\SZGRIDGAME_20210305_06_168_06.nc");// 数据文件
        cond.put("outFile", "E:\\mnt\\aaa.png");// 输出图片文件
        cond.put("time", 4);// 时次
        cond.put("valuename", "r03h");// 要素

        cond.put("fontpath", "E:\\mnt\\opengrads\\font\\");// 字体库路径
        cond.put("mappath", "E:\\mnt\\opengrads\\map\\");// 地图路径
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream("E:\\mnt\\aaabb.gs"),"GBK");
            template.render(cond, fw);
            b = true;
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
}
