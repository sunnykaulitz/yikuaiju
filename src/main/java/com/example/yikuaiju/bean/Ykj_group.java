package com.example.yikuaiju.bean;

import java.io.Serializable;
import java.util.Date;

public class Ykj_group implements Serializable {
    private Integer id;

    private String name;

    private Date creationtime;

    private Integer personnum;

    private Integer gameid;

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

    public Integer getPersonnum() {
        return personnum;
    }

    public void setPersonnum(Integer personnum) {
        this.personnum = personnum;
    }

    public Integer getGameid() {
        return gameid;
    }

    public void setGameid(Integer gameid) {
        this.gameid = gameid;
    }
}