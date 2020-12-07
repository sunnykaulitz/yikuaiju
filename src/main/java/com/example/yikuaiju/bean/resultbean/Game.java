package com.example.yikuaiju.bean.resultbean;

import com.example.yikuaiju.bean.Ykj_game;

import java.io.Serializable;
import java.util.List;

public class Game implements Serializable {

    private Ykj_game game;
    private List<GameSummary> gameSummary;

    public Ykj_game getGame() {
        return game;
    }

    public void setGame(Ykj_game game) {
        this.game = game;
    }

    public List<GameSummary> getGameSummary() {
        return gameSummary;
    }

    public void setGameSummary(List<GameSummary> gameSummary) {
        this.gameSummary = gameSummary;
    }
}
