package com.bjtu.priceforecast.service.Impl;

import com.bjtu.priceforecast.entity.AreaPrice;
import com.bjtu.priceforecast.mapper.AreaPriceMapper;
import com.bjtu.priceforecast.service.AreaPriceService;
import com.bjtu.priceforecast.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

;import java.net.URISyntaxException;
import java.util.List;

/**
 * @author 程宇航
 * @version 1.0
 * @date 2022-09-17 19:37
 * @description
 */
@Service
public class AreaPriceServiceImpl implements AreaPriceService {

    @Autowired
    private AreaPriceMapper areaPriceMapper;

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public int insertMany() {
        try {
            List<AreaPrice> areaPrices = httpUtils.getData("咸阳市","煤炭价格");
            System.out.println(areaPrices);
//            int number = areaPrices.size();
            int number = areaPriceMapper.insertMany(areaPrices);
            return number;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<String> test() {
        return httpUtils.getAreaNames();
    }

}
