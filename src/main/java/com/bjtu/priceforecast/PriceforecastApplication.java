package com.bjtu.priceforecast;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bjtu.priceforecast.mapper")
public class PriceforecastApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceforecastApplication.class, args);
    }

}
