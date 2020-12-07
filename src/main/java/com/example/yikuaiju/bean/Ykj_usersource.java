package com.example.yikuaiju.bean;

/*组队方式
 *  "若通过其他用户创建的活动的二维码、或者分享页面中的二维码，扫码进入的，则记录为“活动扫码2”；
 * 其他则为“小程序搜索1”"
 */
public class Ykj_usersource {

    public static Integer appsearch = Integer.valueOf(1);   //小程序搜索

    public static Integer scancode = Integer.valueOf(2);   //活动扫码
}
