package com.bjtu.priceforecast.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bjtu.priceforecast.entity.AreaPrice;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author 程宇航
 * @version 1.0
 * @date 2022-09-17 17:00
 * @description
 */

@Component
@Data
public class HttpUtils {

    @Value("${cctd.url}")
    private String url;

    @Value("${cctd.cookies}")
    private String cookies;

    @Value("${cctd.userAgent}")
    private String userAgent;

    private List<String> areaNames;
    private List<String> portNames;

    {
        Map<String, List<String>> names = this.getContent();
        areaNames = names.get("地区价格");
        portNames = names.get("港口价格");
    }
    /**
     * 默认查询所有日期数据
     * @param data
     * @param name
     * @return
     */

    public List<AreaPrice> getData(String data, String name) throws URISyntaxException {
        String time = " where DATE_FORMAT(END_DATE,'%Y-%m-%d') >= '-0006-11-30'";
        JSONArray jsonArray = doGetHtml(data, name, time);
        return transform(jsonArray);
    }

    /**
     * 查询指定时间到当天的数据
     * @param data
     * @param name
     * @param dtime
     * @return
     */
    public List<AreaPrice> getData(String data, String name, String dtime) throws URISyntaxException {
        String time = "where DATE_FORMAT(END_DATE,'%Y-%m-%d') >= '" + dtime + "'";
        JSONArray jsonArray = doGetHtml(data, name, time);
        return transform(jsonArray);
    }


    public JSONArray doGetHtml(String data, String name, String time) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url, Charset.forName("GBK"));
        JSONArray jsonArray = null;
        List<NameValuePair> params = new ArrayList<>();
        //封装表单中的数据
        params.add(new BasicNameValuePair("data", data));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("time",time));
        uriBuilder.setParameters(params);
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        //设置请求头信息
        httpPost.setHeader("User-Agent", userAgent);
        httpPost.setHeader("Cookie", cookies);
        CloseableHttpResponse response = null;
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient;

        try {
            httpClient = httpClientBuilder.setConnectionManager(getHttpClientConnectionManager()).build();
            response = httpClient.execute(httpPost);
            if(response.getCode() == 200){
                String content = EntityUtils.toString(response.getEntity(), Charset.forName("gbk"));
//                System.out.println(content);
                JSONObject jsonObject = JSON.parseObject(content);
                jsonArray = JSON.parseArray(jsonObject.getString("data"));
//                System.out.println(jsonArray);
            }


        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }finally {
            try {
                //关闭response
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jsonArray;

    }

    //创建连接池
    private static HttpClientConnectionManager getHttpClientConnectionManager() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(getSslConnectionSocketFactory())
                .build();
    }
    //忽略ssl配置
    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = ((x509Certificates, s) -> true);
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    }

    /**
     * 将Jasonarray格式转换为AreaPrice格式的列表，用于写入数据库
     * @param jsonArray 格式的列表
     * @return AreaPrice的列表
     */
    public List<AreaPrice> transform(JSONArray jsonArray){
        ArrayList<AreaPrice> areaPrices = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray array = JSON.parseArray(jsonArray.getObject(i, String.class));
            //新建空对象，
            AreaPrice areaPrice = new AreaPrice();

            String time = array.getObject(0, String.class);
            if (time == null){
                continue;
            }else {
                areaPrice.setTime(array.getObject(0, Date.class));
//            System.out.println(areaPrice.getTime());
            }

            areaPrice.setArea(array.getObject(1, String.class));
//            System.out.println(areaPrice.getArea());

            areaPrice.setName(array.getObject(2, String.class));
//            System.out.println(areaPrice.getName());

            //排除灰份为null的情况
            String ash = array.getObject(3, String.class);
            if (ash == null){
                areaPrice.setAsh(0.0);
            }else {
                areaPrice.setAsh(Double.parseDouble(ash));
//            System.out.println(areaPrice.getAsh());
            }

            //排除为null的情况
            String volatileMatter = array.getObject(4, String.class);
            if (volatileMatter == null){
                areaPrice.setVolatileMatter(0.0);
            }else {
                areaPrice.setVolatileMatter(Double.parseDouble(volatileMatter));
//            System.out.println(areaPrice.getVolatileMatter());
            }

            //排除为null的情况
            String sulphur = array.getObject(5, String.class);
            if (volatileMatter == null){
                areaPrice.setSulphur(0.0);
            }else {
                areaPrice.setSulphur(Double.parseDouble(sulphur));
//            System.out.println(areaPrice.getVolatileMatter());
            }

            //排除粘结指数为null的情况
            String adhesionIndex = array.getObject(6, String.class);
            if (adhesionIndex == null){
                areaPrice.setAdhesionIndex(0.0);
            }else {
                areaPrice.setAdhesionIndex(Double.parseDouble(adhesionIndex));
            }

            //去除发热量数据前的空格问题
            String calorificValue = array.getObject(7, String.class).trim();
            areaPrice.setCalorificValue(Integer.parseInt(calorificValue));

            areaPrice.setPrice(array.getObject(8, Double.class));
            areaPrice.setCompared(array.getObject(9, Integer.class));
            areaPrice.setPriceType(array.getObject(10, String.class));

            areaPrices.add(areaPrice);
//            System.out.println(areaPrices);
        }
        return areaPrices;
    }


    public Map<String, List<String>> getContent(){
        String iurl = "https://www.cctd.com.cn/list-167-1.html";
//        String cookies = "lNGUp_auth=ed79Pd3gYZCKDlD-n_WJxJbNhz0yw80G5Qy283ULPiuQ0XO_hRo7k1gbgFtI_lEutmFSOG_ZeJvoFpWsXTzUP-9LpsNSJ_l91fUwutQNF9diPmzeovx-ocStSecYIoxB98ozce4XMlyN96zdJyIxQBX_b0TAcJ0; lNGUp__userid=ca49mZ9hKsWk86OoRdfoe7WQG7_qiAIbh4U1X67SWpya7A; lNGUp__username=1b44_EtN5I1H7xncuPZdDXIAA5dqz2Pm32OUy_tJunXS; lNGUp__groupid=35f4f4vu8uY9tztP0PGEMxfAx4XBXpWGxWdol-P8yw; lNGUp__nickname=2e50nRH0Ueygtj7EXdtDjIPwfb_nkRlZ9D5EYvN68dVjj1ohbwpsR1U; Hm_lvt_594886944cf2480d17095af56ff618e2=1661859198,1662127653,1662196355,1662433052; Hm_lpvt_594886944cf2480d17095af56ff618e2=1662433052";
//        String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:102.0) Gecko/20100101 Firefox/102.0";

        Map<String, List<String>> names = new HashMap<>();
        // 新建arraylist用来保存所有地区名称
        List<String> areaNames = new ArrayList<>();
        // 新建arraylist用来保存所有港口名称
        List<String> portNames = new ArrayList<>();

        HttpPost httpPost = new HttpPost(iurl);
        httpPost.setHeader("User-Agent", userAgent);
        httpPost.setHeader("Cookie", cookies);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        String content = null;

        try {
            httpClient = httpClientBuilder.setConnectionManager(getHttpClientConnectionManager()).build();
            response = httpClient.execute(httpPost);
            if (response.getCode() == 200) {
                content = EntityUtils.toString(response.getEntity(), Charset.forName("gbk"));
//                System.out.println(content);
                Document document = Jsoup.parse(content);
//                Elements elements = document.getElementsByClass("easyui-tree");
//                String key = "a[target]";
                String key1 = ".easyui-tree";
                String key2 = "a[href]";
                Elements areaEle = document.select(key1).first().child(0).child(1).child(0).child(1).child(0).select(key2);
                for (Element element : areaEle
                ) {
                    areaNames.add(element.text());
                }
                // 获得所有港口名称
                Elements portEle = document.select(key1).first().child(0).child(1).child(0).child(1).child(1).select(key2);
                for (Element element: portEle
                ) {
                    portNames.add(element.text());
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        names.put("地区价格", areaNames);
        names.put("港口价格", portNames);
        return names;
    }

}
