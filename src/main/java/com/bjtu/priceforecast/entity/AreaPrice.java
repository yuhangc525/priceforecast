package com.bjtu.priceforecast.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Date;

/**
 * @author 程宇航
 * @version 1.0
 * @date 2022-09-17 16:10
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaPrice {
//    private int id;
    private Date time;
    private String area;
    private String name;
    private double ash;
    private double volatileMatter;
    private double sulphur;
    private double adhesionIndex;
    private int calorificValue;
    private double price;
    private int compared;
    private String priceType;
}
