package com.bjtu.priceforecast.mapper;

import com.bjtu.priceforecast.entity.AreaPrice;

import java.util.List;

/**
 * @author 程宇航
 * @version 1.0
 * @date 2022-09-17 16:18
 * @description
 */

public interface AreaPriceMapper {
    //插入一条数据
    int insertOne(AreaPrice areaPrice);

    //插入多条数据
    int insertMany(List<AreaPrice> areaPriceList);
}
