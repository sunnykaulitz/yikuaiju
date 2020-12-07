package com.example.yikuaiju.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Ykj_score implements Serializable {
    private Integer id;

    private Integer setid;

    private Integer playerid;

    private Byte iswin;

    private BigDecimal score;

    private Date ts;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSetid() {
        return setid;
    }

    public void setSetid(Integer setid) {
        this.setid = setid;
    }

    public Integer getPlayerid() {
        return playerid;
    }

    public void setPlayerid(Integer playerid) {
        this.playerid = playerid;
    }

    public Byte getIswin() {
        return iswin;
    }

    public void setIswin(Byte iswin) {
        this.iswin = iswin;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }
}