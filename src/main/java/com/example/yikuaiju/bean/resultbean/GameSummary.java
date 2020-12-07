package com.example.yikuaiju.bean.resultbean;


import java.io.Serializable;
import java.math.BigDecimal;

public class GameSummary implements Serializable {

    private Integer gameid;
    private Integer userid;
    private Integer playerid;
    private String playername;
    private String playstatus;
    private Boolean isshare;
    private String avatarUrl;
    private BigDecimal score;
    private Integer wincount;
    private Integer setcount;
    private Integer gamerank;
    private Boolean virtualuser;

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

    public Integer getPlayerid() {
        return playerid;
    }

    public void setPlayerid(Integer playerid) {
        this.playerid = playerid;
    }

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String playername) {
        this.playername = playername;
    }

    public String getPlaystatus() {
        return playstatus;
    }

    public void setPlaystatus(String playstatus) {
        this.playstatus = playstatus;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getWincount() {
        return wincount;
    }

    public void setWincount(Integer wincount) {
        this.wincount = wincount;
    }

    public Integer getSetcount() {
        return setcount;
    }

    public void setSetcount(Integer setcount) {
        this.setcount = setcount;
    }

    public Integer getGamerank() {
        return gamerank;
    }

    public void setGamerank(Integer gamerank) {
        this.gamerank = gamerank;
    }

    public Boolean getIsshare() {
        return isshare;
    }

    public void setIsshare(Boolean isshare) {
        this.isshare = isshare;
    }

    public Boolean getVirtualuser() {
        return virtualuser;
    }

    public void setVirtualuser(Boolean virtualuser) {
        this.virtualuser = virtualuser;
    }
}
