package com.example.yikuaiju.bean;

import java.io.Serializable;
import java.util.Date;

public class Ykj_user implements Serializable {
    private Integer id;

    private String openid;

    private String nickname;

    private Integer gender;

    private String language;

    private String city;

    private String province;

    private String country;

    private String avatarurl;

    private String sessionkey;

    private Date creationtime;

    private Date modifytime;

    private String unionkey;

    private String mark;

    private String mobile;

    private String wechataccount;

    private Integer source;

    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language == null ? null : language.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl == null ? null : avatarurl.trim();
    }

    public String getSessionkey() {
        return sessionkey;
    }

    public void setSessionkey(String sessionkey) {
        this.sessionkey = sessionkey == null ? null : sessionkey.trim();
    }

    public Date getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(Date creationtime) {
        this.creationtime = creationtime;
    }

    public Date getModifytime() {
        return modifytime;
    }

    public void setModifytime(Date modifytime) {
        this.modifytime = modifytime;
    }

    public String getUnionkey() {
        return unionkey;
    }

    public void setUnionkey(String unionkey) {
        this.unionkey = unionkey == null ? null : unionkey.trim();
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark == null ? null : mark.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public String getWechataccount() {
        return wechataccount;
    }

    public void setWechataccount(String wechataccount) {
        this.wechataccount = wechataccount == null ? null : wechataccount.trim();
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}