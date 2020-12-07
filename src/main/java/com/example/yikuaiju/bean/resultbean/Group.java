package com.example.yikuaiju.bean.resultbean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Group implements Serializable {

    private Integer groupid;
    private String gamename;
    private Integer gameid;
    private Date creationtime;
    private Integer personnum;
    private List<Map<String,Object>> userInfo;

    public Integer getGroupid() {
        return groupid;
    }

    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
    }

    public String getGamename() {
        return gamename;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public Integer getGameid() {
        return gameid;
    }

    public void setGameid(Integer gameid) {
        this.gameid = gameid;
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

    public List<Map<String, Object>> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(List<Map<String, Object>> userInfo) {
        this.userInfo = userInfo;
    }
}
