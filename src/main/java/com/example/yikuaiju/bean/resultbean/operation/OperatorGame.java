package com.example.yikuaiju.bean.resultbean.operation;


import java.io.Serializable;
import java.util.Date;

public class OperatorGame implements Serializable {

    private Integer id;
    private String creationtime;
    private Integer creator;
    private String name;
    private String gstatus;
    private String formtype;
    private String groupname;
    private Integer pcount;
    private Integer wechatpcount;
    private Integer setcount;
    private String endtime ;
    private Integer sharetimes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(String creationtime) {
        this.creationtime = creationtime;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGstatus() {
        return gstatus;
    }

    public void setGstatus(String gstatus) {
        this.gstatus = gstatus;
    }

    public String getFormtype() {
        return formtype;
    }

    public void setFormtype(String formtype) {
        this.formtype = formtype;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public Integer getPcount() {
        return pcount;
    }

    public void setPcount(Integer pcount) {
        this.pcount = pcount;
    }

    public Integer getWechatpcount() {
        return wechatpcount;
    }

    public void setWechatpcount(Integer wechatpcount) {
        this.wechatpcount = wechatpcount;
    }

    public Integer getSetcount() {
        return setcount;
    }

    public void setSetcount(Integer setcount) {
        this.setcount = setcount;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public Integer getSharetimes() {
        return sharetimes;
    }

    public void setSharetimes(Integer sharetimes) {
        this.sharetimes = sharetimes;
    }
}
