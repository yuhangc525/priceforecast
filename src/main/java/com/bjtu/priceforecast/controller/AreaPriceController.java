package com.bjtu.priceforecast.controller;

import com.bjtu.priceforecast.service.AreaPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 程宇航
 * @version 1.0
 * @date 2022-09-17 19:19
 * @description
 */

@RestController
public class AreaPriceController {

    @Autowired
    private AreaPriceService areaPriceService;


    @RequestMapping("/insert")
    public String insertData(){
        int number = areaPriceService.insertMany();
        return "succeed！" + number + "items were inserted.";
    }

    @RequestMapping("/test")
    public List<String> test(){
        return areaPriceService.test();
    }

}
