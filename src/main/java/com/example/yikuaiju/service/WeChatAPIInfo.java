package com.example.yikuaiju.service;

public interface WeChatAPIInfo {
    /**
     * 获取access_token
     */
    String ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";

    /**
     * 登录地址
     */
    String loginUrl = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 统一下单url
     */
    String Create_Order_Prefix_Url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 订单情况查询url
     */
    String Order_check_Url = "https://api.mch.weixin.qq.com/pay/orderquery";

    /**
     * 企业付款到零钱
     */
    String Company_Transfer_Url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";

    /**
     * 企业付款查询url
     */
    String Transfer_Check_Url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo";

    /**
     * 二维码url
     */
    String QRcode = "https://api.weixin.qq.com/wxa/getwxacodeunlimit";


    String SendTemplateMsg_Url = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send";
}
