package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    GamePlayerRepository gamePlayerRepository;

    @Autowired
    SalvoRepository salvoRepository;

    @Autowired
    ScoreRepository scoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/games")

    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        if (!isGuest(authentication)) {
            dto.put("player", playerRepository.findByUserName(authentication.getName()).playerDTO());
        } else {
            dto.put("player", "Guest");
        }
        dto.put("games", gameRepository.findAll().stream().map(game -> game.gameDTO()).collect(Collectors.toList()));
        return dto;
    }

    public static boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    public static Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        ResponseEntity<Map<String, Object>> response;
        if (isGuest(authentication)) {
            response = new ResponseEntity<>(makeMap("error", "Player is unauthorized"), HttpStatus.UNAUTHORIZED);
        } else {
            Game newGame = gameRepository.save(new Game(LocalDateTime.now()));
            Player newPlayer = playerRepository.findByUserName(authentication.getName());
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newGame, playerRepository.save(newPlayer)));
            response = new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
        return response;
    }

    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {
        ResponseEntity<Map<String, Object>> responses;
        Optional<Game> gameHouse = gameRepository.findById(gameId);
        if (isGuest(authentication)) {
            responses = new ResponseEntity<>(makeMap("error", "Player is unauthorized"), HttpStatus.UNAUTHORIZED);
        } else if (!gameHouse.isPresent()) {
            responses = new ResponseEntity<>(makeMap("error", "Game ID doesn't exist"), HttpStatus.FORBIDDEN);
        } else if (gameHouse.get().getGamePlayers().size() > 1) {
            responses = new ResponseEntity<>(makeMap("error", "Sorry, game is full"), HttpStatus.FORBIDDEN);
        } else {
            Player player = playerRepository.findByUserName(authentication.getName());
            GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(gameHouse.get(), player));
            responses = new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
        }
        return responses;
    }

    @GetMapping("/players")
    private List<Map<String, Object>> getPlayers() {
        return playerRepository.findAll().stream().map(player -> player.playerDTO()).collect(Collectors.toList());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addPlayer(@RequestParam String email, @RequestParam String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("Error", "missing data"), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByUserName(email) != null) {
            return new ResponseEntity<>(makeMap("Error", "email in use"), HttpStatus.FORBIDDEN);
        }
        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/gamePlayer")
    private List<Map<String, Object>> getGamePlayer() {
        return gamePlayerRepository.findAll().stream().map(gamePlayer -> gamePlayer.gamePlayerDTO()).collect(Collectors.toList());
    }

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String, Object>> getGameViewByGamePlayerID(@PathVariable Long nn, Authentication authentication) {

        if(isGuest(authentication)){
            return new  ResponseEntity<>(makeMap("error", "Oops i did it again"),HttpStatus.UNAUTHORIZED);
        }
        Player  player  = playerRepository.findByUserName(authentication.getName());
        GamePlayer gamePlayer = gamePlayerRepository.findById(nn).orElse(new GamePlayer());
        if(player ==  null){
            return new  ResponseEntity<>(makeMap("error", "I played with your heart"),HttpStatus.UNAUTHORIZED);
        }
        if(gamePlayer.getId() ==  0){
            return new  ResponseEntity<>(makeMap("error", "Got lost in the game"),HttpStatus.UNAUTHORIZED);
        }
        if(gamePlayer.getPlayer().getId() !=  player.getId()){
            return new  ResponseEntity<>(makeMap("error", "Ooh, babe babe!!!! >.<"),HttpStatus.CONFLICT);
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        Map<String, Object> hits = new LinkedHashMap<>();
        GamePlayer getOtherOne = gamePlayer.getOtherOne();
        if (getOtherOne.getId() != 0) {
            hits.put("self", hitsAndSinks(gamePlayer, getOtherOne));
            hits.put("opponent", hitsAndSinks(getOtherOne, gamePlayer));
        } else {
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());
        }

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDateGame());
        dto.put("gameState", getGameState(gamePlayer));


        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers()
                .stream()
                .map(gamePlayer1 -> gamePlayer1.gamePlayerDTO())
                .collect(Collectors.toList()));
        dto.put("ships", gamePlayer.getShip()
                .stream()
                .map(ship -> ship.shipDTO())
                .collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvo()
                        .stream()
                        .map(salvo -> salvo.salvoDTO()))
                .collect(Collectors.toList()));
        dto.put("hits", hits);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

        @RequestMapping(value = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
        public ResponseEntity<Map<String, Object>> newShip (@PathVariable long gamePlayerId, @RequestBody Set<Ship> ships, Authentication authentication) {
            ResponseEntity<Map<String, Object>> answer;
            Optional<GamePlayer> gPSpot = gamePlayerRepository.findById(gamePlayerId);
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            if (isGuest(authentication)) {
                answer = new ResponseEntity<>(makeMap("error", "There is no current user logged in ._."), HttpStatus.UNAUTHORIZED);
            } else if (!gPSpot.isPresent()) {
                answer = new ResponseEntity<>(makeMap("error", "There is no game player with the give ID!!! -.-"), HttpStatus.UNAUTHORIZED);
            } else if (gPSpot.get().getPlayer().getId() != currentPlayer.getId()) {
                answer = new ResponseEntity<>(makeMap("error", "The current user is not the game player the ID references D:"), HttpStatus.UNAUTHORIZED);
            } else if (gPSpot.get().getShip().size() > 0) {
                answer = new ResponseEntity<>(makeMap("error", "The user already has ships placed o.O"), HttpStatus.FORBIDDEN);
            } else if (ships.size() > 0) {
                gPSpot.get().addShips(ships);
                gamePlayerRepository.save(gPSpot.get());
                answer = new ResponseEntity(makeMap("OK", "Success!!! OvO"), HttpStatus.CREATED);
            } else {
                answer = new ResponseEntity<>(makeMap("error", "You are not sending any ships!!!! >.<"), HttpStatus.FORBIDDEN);
            }
            return answer;
        }

        @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
        public ResponseEntity<Map<String, Object>> salvoShots (@PathVariable long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {
            ResponseEntity<Map<String, Object>> shot = null;
            Optional<GamePlayer> gPSpot = gamePlayerRepository.findById(gamePlayerId);
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            GamePlayer otherOne = gPSpot.get().getOtherOne();
            if (isGuest(authentication)) {
                shot = new ResponseEntity<>(makeMap("error", "There is no current user logged in ._."), HttpStatus.UNAUTHORIZED);
            } else if (!gPSpot.isPresent()) {
                shot = new ResponseEntity<>(makeMap("error", "There is no game player with the give ID!!! -.-"), HttpStatus.UNAUTHORIZED);
            } else if (gPSpot.get().getPlayer().getId() != currentPlayer.getId()) {
                shot = new ResponseEntity<>(makeMap("error", "The current user is not the game player the ID references D:"), HttpStatus.UNAUTHORIZED);
            } else if (otherOne.getId() == 0) {
                shot = new ResponseEntity<>(makeMap("error", "There's no other one :'c"), HttpStatus.UNAUTHORIZED);
            } else if (gPSpot.get().getSalvo().size() <= otherOne.getSalvo().size()) {
                salvo.setTurn(gPSpot.get().getSalvo().size() + 1);
                salvo.setGamePlayer(gPSpot.get());
                salvoRepository.save(salvo);
                shot = new ResponseEntity<>(makeMap("OK", "Salvo creation succeed!! ^u^"), HttpStatus.CREATED);
            } else {
                shot = new ResponseEntity<>(makeMap("error", "You've already had your turn .^."), HttpStatus.FORBIDDEN);
            }
            return shot;
        }

    private List<Map> hitsAndSinks(GamePlayer self, GamePlayer enemy){

        // se declara el array hits
        List<Map>hits = new ArrayList<>();

        int carrierHits = 0;
        int battleshipHits = 0;
        int submarineHits = 0;
        int destroyerHits = 0;
        int patrolboatHits = 0;

        List<String> carrierLocations = findShipLocations(enemy, "carrier");
        List<String> battleshipLocations = findShipLocations(enemy, "battleship");
        List<String> submarineLocations = findShipLocations(enemy, "submarine");
        List<String> destroyerLocations = findShipLocations(enemy, "destroyer");
        List<String> patrolboatLocations = findShipLocations(enemy, "patrolboat");

        for (Salvo salvo : self.getSalvo()){
            // se recorren los salvos del enemigo para ver qué salvos coinciden con las locaciones de las naves del enemigo

            Map<String, Object> damagesPerTurn= new LinkedHashMap<>();
            Map<String, Object> hitsPerTurn= new LinkedHashMap<>();

            List<String> hitCellsList = new ArrayList<>();  // celdas con las locaciones de las naves golpeadas

            // hits in turn counter
            int carrierTurn = 0;
            int battleshipTurn = 0;
            int submarineTurn = 0;
            int destroyerTurn = 0;
            int patrolboatTurn = 0;

            // missed shots
            int missedShots = salvo.getSalvoLocations().size();

            for (String location : salvo.getSalvoLocations()){
                // se recorren las locaciones de cada salvo para ver qué locaciones
                // coinciden con las locaciones de las naves del enemigo
                if(carrierLocations.contains(location)){
                    carrierHits++;
                    carrierTurn++;
                    missedShots--;
                    hitCellsList.add(location);
                }
                if(battleshipLocations.contains(location)){
                    battleshipHits++;
                    battleshipTurn++;
                    missedShots--;
                    hitCellsList.add(location);

                }
                if(submarineLocations.contains(location)){
                    submarineHits++;
                    submarineTurn++;
                    missedShots--;
                    hitCellsList.add(location);
                }
                if(destroyerLocations.contains(location)){
                    destroyerHits++;
                    destroyerTurn++;
                    missedShots--;
                    hitCellsList.add(location);
                }
                if (patrolboatLocations.contains(location)){
                    patrolboatHits++;
                    patrolboatTurn++;
                    missedShots--;
                    hitCellsList.add(location);
                }
            }

            damagesPerTurn.put("carrierHits", carrierTurn);
            damagesPerTurn.put("battleshipHits", battleshipTurn);
            damagesPerTurn.put("submarineHits", submarineTurn);  // turnos en los que se produjeron un hit
            damagesPerTurn.put("destroyerHits", destroyerTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatTurn);

            damagesPerTurn.put("carrier", carrierHits);
            damagesPerTurn.put("battleship", battleshipHits);
            damagesPerTurn.put("submarine", submarineHits);    // cantidad de golpes que sufrió cada nave por los salvos del viewer
            damagesPerTurn.put("destroyer", destroyerHits);
            damagesPerTurn.put("patrolboat", patrolboatHits);

            hitsPerTurn.put("turn", salvo.getTurn());
            hitsPerTurn.put("missed", missedShots);
            hitsPerTurn.put("damages", damagesPerTurn);
            hitsPerTurn.put("hitLocations", hitCellsList);

            hits.add(hitsPerTurn);
        }
        return hits;
    }

    private List<String> findShipLocations(GamePlayer enemy, String type){
        Optional<Ship> response;
        response = enemy.getShip().stream().filter(ship -> ship.getType() == type).findFirst();
        if(!response.isPresent()){
            return new ArrayList<String>();
        }
        return response.get().getLocations();
    }

    private List<Salvo> order(Set<Salvo> salvoes) {
        return salvoes.stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList());
    }

    public String getGameState(GamePlayer gamePlayer) {
        if (gamePlayer.getShip().size() == 0) {
            return "PLACESHIPS";
        }
        if (gamePlayer.getGame().getGamePlayers().size() == 1) {
            return "WAITINGFOROPP";
        }
        if (gamePlayer.getGame().getGamePlayers().size() == 2) {
            if ((gamePlayer.getSalvo().size() > gamePlayer.getOtherOne().getSalvo().size())) {
                return "WAIT";
            }
            if (gamePlayer.getOtherOne().getShip().size() == 0) {
                return "WAIT";
            }
            if (gamePlayer.getSalvo().size() < gamePlayer.getOtherOne().getSalvo().size()) {
                return "PLAY";
            }
            if (gamePlayer.getSalvo().size() == gamePlayer.getOtherOne().getSalvo().size()) {
                boolean gamePlayerLost = getIfAllSunk(gamePlayer, gamePlayer.getOtherOne());
                boolean opponentLost = getIfAllSunk(gamePlayer.getOtherOne(), gamePlayer);
                if (gamePlayerLost && opponentLost) {
                    scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), LocalDateTime.now(), 0.5));
                    return "TIE";
                }
                if (gamePlayerLost && !opponentLost) {
                    scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), LocalDateTime.now(), 0.0));
                    return "LOST";
                }
                if (opponentLost && !gamePlayerLost) {
                    scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), LocalDateTime.now(), 1.0));
                    return "WON";
                }
                if (gamePlayer.getId() > gamePlayer.getOtherOne().getId()){
                    return "WAIT";
                } else{
                    return "PLAY";
                }
            } else {
                return "UNDEFINED";
            }
        }
        return "WAIT";
    }

    private Boolean getIfAllSunk (GamePlayer self, GamePlayer enemy){
        if (!enemy.getShip().isEmpty() && !self.getSalvo().isEmpty()){
            return enemy.getSalvo().stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList()).containsAll(self.getShip().stream()
                    .flatMap(ship -> ship.getLocations().stream()).collect(Collectors.toList()));
        }
        return false;
    }

}