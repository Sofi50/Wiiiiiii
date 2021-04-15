package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player")
    private Player player;

    private LocalDateTime finishDate;
    private double scores;

    public Score() {
    }

    public Score(Game game, Player player, LocalDateTime date, double scores) {
        this.game = game;
        this.player = player;
        this.finishDate = date;
        this.scores = scores;
    }

    public double getScores() {
        return scores;
    }

    public void setScores(double scores) {
        this.scores = scores;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public LocalDateTime getDate() {
        return finishDate;
    }

    public void setDate(LocalDateTime date) {
        this.finishDate = date;
    }

    public Map<String, Object> scoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", this.getPlayer().getId());
        dto.put("finishDate", this.getDate());
        dto.put("score", this.getScores());

        return dto;
    }

}
