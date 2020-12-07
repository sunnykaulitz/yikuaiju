package com.example.yikuaiju.bean;

import java.io.Serializable;
import java.util.Date;

public class Ykj_game implements Serializable {
    private Integer id;

    private String name;

    private Date creationtime;

    private Date endtime;

    private String address;

    private Integer personnum;

    private String taketime;

    private Integer gcount;

    private Integer gstatus;

    private Integer creator;

    private Date ts;

    private Date modifytime;

    private Byte invalid;

    private Byte isshare;

    private Integer finisher;

    private Integer formtype;

    private Integer sharetimes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Date getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(Date creationtime) {
        this.creationtime = creationtime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Integer getPersonnum() {
        return personnum;
    }

    public void setPersonnum(Integer personnum) {
        this.personnum = personnum;
    }

    public String getTaketime() {
        return taketime;
    }

    public void setTaketime(String taketime) {
        this.taketime = taketime == null ? null : taketime.trim();
    }

    public Integer getGcount() {
        return gcount;
    }

    public void setGcount(Integer gcount) {
        this.gcount = gcount;
    }

    public Integer getGstatus() {
        return gstatus;
    }

    public void setGstatus(Integer gstatus) {
        this.gstatus = gstatus;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public Date getModifytime() {
        return modifytime;
    }

    public void setModifytime(Date modifytime) {
        this.modifytime = modifytime;
    }

    public Byte getInvalid() {
        return invalid;
    }

    public void setInvalid(Byte invalid) {
        this.invalid = invalid;
    }

    public Byte getIsshare() {
        return isshare;
    }

    public void setIsshare(Byte isshare) {
        this.isshare = isshare;
    }

    public Integer getFinisher() {
        return finisher;
    }

    public void setFinisher(Integer finisher) {
        this.finisher = finisher;
    }

    public Integer getFormtype() {
        return formtype;
    }

    public void setFormtype(Integer formtype) {
        this.formtype = formtype;
    }

    public Integer getSharetimes() {
        return sharetimes;
    }

    public void setSharetimes(Integer sharetimes) {
        this.sharetimes = sharetimes;
    }
}