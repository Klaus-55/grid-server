package com.galaxy.score;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@MapperScan("com.galaxy.score.mapper")
public class ScoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreApplication.class, args);
    }

}
