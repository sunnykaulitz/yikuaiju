package com.example.yikuaiju.bean.resultbean.operation;


import org.apache.ibatis.annotations.Result;

import java.io.Serializable;

public class OperatorUser implements Serializable {

    private Integer id;
    private String nickName;
    private String gender;
    private String city;
    private String mobile;
    private String creationtime;
    private String modifytime;
    private String activedays;
    private String source;
    private String mark;
    private String groupcount;
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(String creationtime) {
        this.creationtime = creationtime;
    }

    public String getModifytime() {
        return modifytime;
    }

    public void setModifytime(String modifytime) {
        this.modifytime = modifytime;
    }

    public String getActivedays() {
        return activedays;
    }

    public void setActivedays(String activedays) {
        this.activedays = activedays;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getGroupcount() {
        return groupcount;
    }

    public void setGroupcount(String groupcount) {
        this.groupcount = groupcount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
