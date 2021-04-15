package com.codeoftheweb.salvo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

import static java.util.stream.Collectors.toSet;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date dateGame;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Ship> ship;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvo;

    public GamePlayer() {

        this.dateGame = new Date();
    }

    public GamePlayer(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.dateGame = new Date();
    }

    public Set<Ship> getShip() {
        return ship;
    }

    public void setShip(Set<Ship> ship) {
        this.ship = ship;
    }

    public Set<Salvo> getSalvo() {

        return salvo;
    }

    public void setSalvo(Set<Salvo> salvo) {

        this.salvo = salvo;
    }

    public Date getDateGame() {

        return dateGame;
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

    public long getId() {
        return id;
    }

    public Map<String, Object> gamePlayerDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", this.getId());
        dto.put("player", player.playerDTO());

        return dto;
    }

    public Map<String, Object> gameViewDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getId());
        dto.put("created", game.getDateGame());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO).collect(Collectors.toList()));
        dto.put("ships", ship.stream().map(Ship::shipDTO).collect(Collectors.toList()));
        dto.put("salvoes", this.getGame().getGamePlayers().stream().flatMap(gamePlayer1 -> gamePlayer1.getSalvo().stream().map(salvo -> salvo.salvoDTO())).collect(Collectors.toList()));

        return dto;
    }

    public Score getScore() {
        return player.getScore(this.game);
    }
    public void addShips (Set<Ship> ships) {
       ships.forEach(ship -> {
           ship.setGamePlayer(this);
           this.ship.add(ship);
       });
}
    public GamePlayer getOtherOne() {
        return this.getGame().getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getId() != this.getId()).findFirst().orElse(new GamePlayer());
    }

}
