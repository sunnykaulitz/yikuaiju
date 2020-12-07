package com.example.yikuaiju.bean;

import java.io.Serializable;
import java.util.Date;

public class Ykj_player implements Serializable {
    private Integer id;

    private Integer gameid;

    private Integer userid;

    private String playername;

    private Date jointime;

    private Integer playstatus;

    private Date ts;

    private Byte isshare;

    private Integer gamerank;

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

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String playername) {
        this.playername = playername == null ? null : playername.trim();
    }

    public Date getJointime() {
        return jointime;
    }

    public void setJointime(Date jointime) {
        this.jointime = jointime;
    }

    public Integer getPlaystatus() {
        return playstatus;
    }

    public void setPlaystatus(Integer playstatus) {
        this.playstatus = playstatus;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public Byte getIsshare() {
        return isshare;
    }

    public void setIsshare(Byte isshare) {
        this.isshare = isshare;
    }

    public Integer getGamerank() {
        return gamerank;
    }

    public void setGamerank(Integer gamerank) {
        this.gamerank = gamerank;
    }
}