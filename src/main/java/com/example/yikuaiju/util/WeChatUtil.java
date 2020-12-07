package com.example.yikuaiju.util;

import com.alibaba.fastjson.JSONObject;
import com.example.yikuaiju.service.WeChatAPIInfo;
import com.example.yikuaiju.service.WechatInfo;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.util.*;

public class WeChatUtil {

    private static Logger logger = LoggerFactory.getLogger(WeChatUtil.class);
    public static boolean initialized = false;

    //自动生成ID
    public static String getUUID() {
        String id =null;
        UUID uuid = UUID.randomUUID();
        id = uuid.toString();
        //去掉随机ID的短横线
        id = id.replace("-", "");
        //将随机ID换成数字
        int num = id.hashCode();
        //去绝对值
        num = num < 0 ? -num : num;
        id = String.valueOf(num);
        return id;
    }



    /**
     * 获取一个32位的随机字符串
     *
     * @return
     */
    public static String getOneRandomString() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 31; i++) {
            int number = random.nextInt(31);
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }


    /**
     * 获取真实的ip地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaderNames();
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }


    /**
     * 连接订单参数，空则忽略，连接符&
     * 使用详解：符合条件的参数按字段名称由小到大（字典顺序）排序，并连接
     *
     * @param createOrderParams
     * @return
     */
    public static String concatOrderParams(Object createOrderParams) throws Exception {

        TreeMap<String, String> tree = new TreeMap<>(); //用于排序
        Class clazz = createOrderParams.getClass();
        Field[] fields = clazz.getDeclaredFields();

        //查找字段
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String methodName = getFiledMethodName(fieldName);
            try {
                Method method = clazz.getMethod(methodName);
                Object value = method.invoke(createOrderParams);
                if (value != null) {//不为空
                    tree.put(fieldName, value.toString());
                }
            } catch (NoSuchMethodException e) {
                logger.error(e.getMessage());
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage());
            } catch (InvocationTargetException e) {
                logger.error(e.getMessage());
            }
        }
        if (tree.size() == 0) {
            throw new Exception("No field can be linked ! ");
        }
        String str = linkMapKeyValue(tree, "&");

        return str.substring(1);//截取第一个&符号之后的内容
    }


    /**
     * 从map创建签名
     * @param parameters
     * @return
     */
    public static String getSignFromMap(SortedMap<String, String> parameters){
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)){
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + WechatInfo.key);
        logger.info("\t\n由MAP生产的字符串："+sb.toString());
        String sign = null;
        try {
            sign = Algorithm.MD5(sb.toString()).toUpperCase();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.error("MD5加密失败："+e.getClass()+">>>>"+e.getMessage());
        }
        return sign;
    }




    /**
     * 连接字符串:
     *      * 将map值连接为key = value & key = value……形式
     * @param map
     * @param character 连接符号，如：&
     * @return
     */
    public static String linkMapKeyValue(TreeMap<String, String> map, String character) {
        if (map == null || map.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Set<String> keys = map.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            sb.append(character + key + "=" + map.get(key));
        }
        return sb.toString();
    }



    /**
     * 获取字段方法名称
     *
     * @param fieldName
     * @return
     */
    public static String getFiledMethodName(String fieldName) {
        char firstChar = fieldName.toCharArray()[0];
        return "get" + String.valueOf(firstChar).toUpperCase() + fieldName.substring(1, fieldName.length());
    }






    /**
     * 将对象非空参数转化为XML
     *
     * @param obj
     * @return
     */
    public static String transToXML(Object obj) {
        //解决XStream对出现双下划线的bug
        XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
//        XStream xstream = XStreamFactory.getXStream();
        xstream.alias("xml", obj.getClass());
        return xstream.toXML(obj);
    }





    /**
     * 将字符串格式的xml内容转化为对象
     * 注意：该方法存在一个不可避免风险，即：当微信官方文档反馈字段增加或改变时，该方法将不能映射进pojo里。
     *       因为本地pojo(OrderResult)可能没有做对应调整。
     * @param str
     * @return
     */
//    public static OrderResult transToObject(String str) throws XStreamException {
//        str = str.replaceAll("xml","OrderResult");//将返回结果的<xml>标签替换为返回结果类
//        XStream xstream = new XStream();
//        xstream.alias("OrderResult", OrderResult.class);
//        OrderResult orderResult = new OrderResult();
//        return (OrderResult) xstream.fromXML(str,orderResult);
//    }




    /**
     * 将xml字符串解析为map集合，兼容性高
     * @param xmlStr
     * @return
     * @throws ParserConfigurationException
     */
    public static Map<String,String> transXMLStrToMap(String xmlStr) throws ParserConfigurationException,
            SAXException, IOException {

        Map<String, String> data = new HashMap<String, String>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        try(InputStream stream = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));) {
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("解析xml结果失败！字符编码不匹配！");
            throw e;
        } catch (IOException e) {
            logger.error("解析xml结果失败！无法读入流");
            throw e;
        }
        return data;
    }




    /**
     * 获取签名
     * @param signStr
     * @return
     */
    public static String getSign(String signStr) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return Algorithm.MD5(signStr + "&key="+ WechatInfo.key).toUpperCase();
    }



    /**
     * 敏感数据对称解密
     * @param content   ——被加密的数据
     * @param keyByte   ——加密密匙
     * @param ivByte ——偏移量
     * @return
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] decrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws Exception {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");

            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));// 初始化
            byte[] result = cipher.doFinal(content);
            return result;
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 添加算法
     */
    public static void initialize(){
        if (initialized) return;
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }


    /**
     * @Description 生成iv
     * @Param [iv]
     * @return java.security.AlgorithmParameters
     **/
    public static AlgorithmParameters generateIV(byte[] iv) throws Exception{
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }


    public static String HttpsPost(String url,String xmlStr) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream instream = new FileInputStream(new File(WechatInfo.CERTIFICATE_ADDRESS));){
            keyStore.load(instream, WechatInfo.mch_id.toCharArray());
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, WechatInfo.mch_id.toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
//        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Accept", "*/*");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.addHeader("Host", "api.mch.weixin.qq.com");
        httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.addHeader("Cache-Control", "max-age=0");
//        httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
        httpPost.setEntity(new StringEntity(xmlStr, "UTF-8"));

        logger.info("执行请求" + httpPost.getRequestLine());

        CloseableHttpResponse response = httpclient.execute(httpPost);
        StringBuffer sbf = new StringBuffer();
        try {
            org.apache.http.HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                String text;
                while ((text = bufferedReader.readLine()) != null) {
                    sbf.append(text);
                }
            }
            EntityUtils.consume(entity);
        } finally {
            response.close();
            httpclient.close();
        }
        return sbf.toString();
    }
     /*
      * @author lifei
      * @Params code
      * @return 微信用户信息
      * @description: 获取微信授权信息
      * @date 2020/11/16 21:46
      */
    public static JSONObject getWxUserInfo(String code) throws Exception {
        //获取 session_key 和 openId
        String url = new StringBuilder().append(WeChatAPIInfo.loginUrl)
                .append("?appid=" + WechatInfo.appid)
                .append("&secret=" + WechatInfo.SECRET)
                .append("&js_code=" + code)
                .append("&grant_type=authorization_code")
                .toString();

        HttpClient client = HttpClientBuilder.create().build();//构建一个Client
        HttpGet get = new HttpGet(url.toString());    //构建一个GET请求
        HttpResponse response = client.execute(get);//提交GET请求
        org.apache.http.HttpEntity result = response.getEntity();//拿到返回的HttpResponse的"实体"
        String content = EntityUtils.toString(result);
        System.out.println(content);//打印返回的信息
        JSONObject res = JSONObject.parseObject(content);//把信息封装为json
        /*2、查询自己的数据库用户表，是否存在这个openid,如果存在则修改session_key，如果不存在则创建一条用户数据*/
        String errmsg = res.getString("errmsg");
        if (errmsg != null)
            throw new Exception(errmsg);
        return res;
    }

    /**
     * (一) 获取 accessToken
     * @param request
     * @param response
     * @return 返回的是JSON类型 ; 获取accessToken:get("access_token");
     */
    public static JSONObject getAccessToken() throws IOException {
        JSONObject json = null;
        String token_url = new StringBuilder().append(WeChatAPIInfo.ACCESS_TOKEN)
                .append("&appid=" + WechatInfo.appid)
                .append("&secret=" + WechatInfo.SECRET).toString();

        HttpClient client = HttpClientBuilder.create().build();//构建一个Client
        HttpGet get = new HttpGet(token_url);    //构建一个GET请求
        HttpResponse httpRes = client.execute(get);//提交GET请求
        org.apache.http.HttpEntity result = httpRes.getEntity();//拿到返回的HttpResponse的"实体"
        String content = EntityUtils.toString(result);
        System.out.println(content);//打印返回的信息
        logger.info("获取 accessToken，"+content);
        json = JSONObject.parseObject(content);//把信息封装为json
        return json;
    }

    /**
     * (二) 二维码生成：通过该接口生成的小程序码，永久有效，数量暂无限制
     * @param sceneStr 参数
     * @param accessToken  密匙
     * @return
     */
    public static void getminiqrQrTwo(String sceneStr, String page, String accessToken, HttpServletResponse response) throws Exception {
        RestTemplate rest = new RestTemplate();
        InputStream inputStream = null;
        OutputStream outputStream = response.getOutputStream();
        try {
            String url = WeChatAPIInfo.QRcode+"?access_token=" + accessToken;
            Map<String, Object> param = new HashMap<String,Object>();
            param.put("scene", sceneStr);// 输入参数 最大32字符"a1wJgLz0Dcg,sw_001"
            if(page != null)
                param.put("page", page);// "pages/index/index" 路径 如果没有默认跳转到首页面微信小程序发布后才可以使用不能添加参数
            param.put("width", "430");// 二维码尺寸
            param.put("is_hyaline", true); // 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码 参数仅对小程序码生效
            param.put("auto_color", false); // 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调 参数仅对小程序码生效
            // 颜色 auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"}
            // 十进制表示
            Map<String, Object> line_color = new HashMap<String,Object>();
            line_color.put("r", 0);
            line_color.put("g", 0);
            line_color.put("b", 0);
            param.put("line_color", line_color);
            System.out.println("调用生成微信URL接口传参:" + param);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String,String>();
            // 头部信息
            List<String> list = new ArrayList<String>();
            list.add("Content-Type");
            list.add("application/json");
            headers.put("header", list);
            HttpEntity<Object> requestEntity = new HttpEntity<Object>(param, headers);
            ResponseEntity<byte[]> entity = rest.exchange(url, HttpMethod.POST, requestEntity, byte[].class,new Object[0]);
            System.out.println("调用小程序生成微信永久小程序码URL接口返回结果:" + entity.getBody());
            byte[] result = entity.getBody();
            System.out.println(Base64.getEncoder().encode(result));
            inputStream = new ByteArrayInputStream(result);

//            File file = new File("D:\\1.png");// 这里返回的是生成的二维码
//            if (!file.exists()) {
//                file.createNewFile();
//            }
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            System.out.println("调用小程序生成微信永久小程序码URL接口异常" + e);
            logger.error("调用小程序生成微信永久小程序码URL接口异常" + e);
            throw new Exception("调用小程序生成微信永久小程序码URL接口异常" + e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        response.setContentType("img/jpeg");
        response.setCharacterEncoding("utf-8");
    }

    // 测试代码
    public static void getErWeiMa(String access_token) {
        try {
            // URL url = new
            // URL("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+access_token);
            URL url = new URL("https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=" + access_token);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");// 提交模式
            // conn.setConnectTimeout(10000);//连接超时 单位毫秒
            // conn.setReadTimeout(2000);//读取超时 单位毫秒
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
            // 发送请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("scene", "a1wJgLz0Dcg,sw_001");
            paramJson.put("page", "pages/index/index");
            paramJson.put("width", "430");
            paramJson.put("auto_color", true);

            // line_color生效
            paramJson.put("auto_color", false);
            JSONObject lineColor = new JSONObject();
            lineColor.put("r", 054);
            lineColor.put("g", 037);
            lineColor.put("b", 159);
            paramJson.put("line_color", lineColor);
            printWriter.write(paramJson.toString());
            // flush输出流的缓冲
            printWriter.flush();
            // 开始获取数据
            BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
            OutputStream os = new FileOutputStream(new File("D:\\erweima3.txt"));
            int len;
            byte[] arr = new byte[1024];
            while ((len = bis.read(arr)) != -1) {
                os.write(arr, 0, len);
                os.flush();
            }
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
