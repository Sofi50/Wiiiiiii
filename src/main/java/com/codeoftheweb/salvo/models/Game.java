package com.codeoftheweb.salvo.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
public class Game {
    //------------------------------------SPRING DEPENDENCIES--------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime dateGame;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<Score> scores = new HashSet<>();

    //----------------------------------------Constructors------------------------------------------
    //----------Empty One--------
    public Game() {
    }
    //----------Parameters One-------
    public Game(LocalDateTime dateGame) {
        this.dateGame = dateGame;
    }
    //----------Getters and Setters------
    public LocalDateTime getDateGame() {
        return dateGame;
    }

    public void setDateGame(LocalDateTime dateGame) {
        this.dateGame = dateGame;
    }

    public long getId() {
        return id;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    //---------------------------------------GAME DATA TRANSFER OBJECT-----------------------------
    public Map<String, Object> gameDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.getId());
        dto.put("created", this.getDateGame());
        dto.put("gamePlayers", gamePlayers.stream().map(gamePlayer -> gamePlayer.gamePlayerDTO()).collect(Collectors.toList()));
        dto.put("scores", this.scores.stream().map(Score::scoreDTO).collect(Collectors.toList()));

        return dto;
    }


}
