package com.example.yikuaiju.bean;

import java.io.Serializable;
import java.util.Date;

public class Ykj_set implements Serializable {
    private Integer id;

    private Integer gameid;

    private Integer setnumber;

    private Date creationtime;

    private Date ts;

    private Integer creator;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGameid() {
        return gameid;
    }

    public void setGameid(Integer gameid) {
        this.gameid = gameid;
    }

    public Integer getSetnumber() {
        return setnumber;
    }

    public void setSetnumber(Integer setnumber) {
        this.setnumber = setnumber;
    }

    public Date getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(Date creationtime) {
        this.creationtime = creationtime;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }
}