package com.example.yikuaiju.service;

public interface WechatInfo {
    /**
     * 小程序appid
     */
    String appid = "wx7183b32c17d77412";
    /**
     * 商户号的Appid
     */
    String mch_appid = "";
    /**
     *商户号
     */
    String mch_id = "";
    /**
     *回调地址
     */
    String notify_url = "";
    /**
     *交易类型
     */
    String trade_type = "JSAPI";
    /**
     * 签名类型
     */
    String sign_type = "MD5";
    /**
     * 商户密匙
     */
    String key = "";
    /**
     * 小程序ApiSecret
     */
    String SECRET = "39e4c6df2f79812f33eec6ba8777f4c1";
    /**
     * 证书地址
     */
    String CERTIFICATE_ADDRESS = "";
    /**
     * 二维码图片地址
     */
    String QRImgRootAddress ="";

    /**
     * 静态资源
     */
    String SourceUrl = "";
}
