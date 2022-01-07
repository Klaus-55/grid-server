package com.galaxy.score;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.galaxy.score.mapper.MediumShortMapper;
import com.galaxy.score.mapper.ObjectiveMapper;
import com.galaxy.score.service.MediumShortService;
import com.galaxy.score.service.ObjectiveService;
import com.galaxy.score.service.ShortApproachService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ScoreApplicationTests {

    @Autowired
    private MediumShortMapper mediumShortMapper;
    @Autowired
    private ObjectiveMapper objectiveMapper;
    @Autowired
    private ObjectiveService objectiveService;
    @Autowired
    private MediumShortService mediumShortService;
    @Autowired
    private ShortApproachService shortApproachService;

    @Test
    public void testObjective() {
//        List<Map<String, Object>> list = er01Mapper.getBABJPreQyPc01("202004020800", "202004100800", "08", "", "1");
//        for (Map<String, Object> map : list) {
//            System.out.println(map);
//        }
        String tableName = "public.tb_score_day_rat_smg_ybjs";
        String start = "202103010000";
        String end = "202103102300";
        String time = "zh";
        String hour = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24";
//        String hour = "1,2,3,4,5,6,7,8,9,10,11,12";
//        String hour = "24";
        String interval = "1";
        String facname = "RAT";
        String type = "BBBUSI";
//        List<Map<String, Object>> list = objectiveMapper.getTemData(start, end, time, hour, interval, facname);
//        for (Map<String, Object> map : list) {
//            System.out.println(map);
//        }
        List<Map<String, Object>> list = objectiveMapper.checkHeavyDayByDay(tableName, start, end, time, interval, facname, type);
        for (Map<String, Object> map : list) {
            System.out.println(map);
        }
        System.out.println(list.size());
    }

    @Test
    public void testMediumShort() {
        String start = "2020-05-01 00:00:00";
        String end = "2020-07-31 23:59:59";
        String year = "2020";
        List<Map<String, Object>> list = mediumShortService.rainstormDetail(start, end);
        for (Map<String, Object> map : list) {
            System.out.println(map);
        }
//        System.out.println(list.size());
    }

    @Test
    public void testShortApproach() {
        String start = "20210701";
        String end = "20210731";
        String ftime = "14";
        String wfinterval = "3";
        String fhours = "3,6,9,12";
        String product = "BBBUSI";
        shortApproachService.rainScore(start, end, ftime, wfinterval, product);
    }
}
