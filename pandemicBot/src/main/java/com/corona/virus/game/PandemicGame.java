package com.corona.virus.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PandemicGame {

    public static final String NEW_PLAYER_ADDED = "New player added";

    private GameStatus gameStatus;

    private List<Player> players;

    public PandemicGame(){
        gameStatus = GameStatus.STOPPED;
        players = new ArrayList<>();
    }

    public GameStatus startGame(){
        if (gameStatus == GameStatus.STOPPED){
            gameStatus = GameStatus.STARTED;
        }
        else if (gameStatus == GameStatus.STARTED || gameStatus == GameStatus.IN_PROGRESS){
            gameStatus = GameStatus.IN_PROGRESS;
        }
        else{
            gameStatus = GameStatus.STOPPED;
        }
        return gameStatus;
    }

    public void addPlayerInGame(String playerId){
        players.add(new Player(playerId));
        System.out.println(NEW_PLAYER_ADDED + playerId);
    }

    public boolean isUserCoronned(String authorId) {
        Player currentPlayer = players.stream().filter(player -> player.getId().equals(authorId)).findFirst().get();
        int randomNum = ThreadLocalRandom.current().nextInt(1, 3);
        if (randomNum == 1){
            currentPlayer.setCoronned(true);
            return true;
        }
        else{
            return false;
        }
    }

    public void healPlayers(String authorId) {
        players.forEach((player) -> healPlayer(player, authorId));
    }

    private void healPlayer(Player currentPlayer, String authorId){
        Player healerPlayer = players.stream().filter(player -> player.getId().equals(authorId)).findFirst().get();
        if (currentPlayer.isCoronned()){
            healerPlayer.setScore(healerPlayer.getScore()+1);
            currentPlayer.setCoronned(false);
        }
    }

    public String getGameScores(){
        StringBuilder scoresStringBuilder = new StringBuilder();
        players.forEach((player) -> scoresStringBuilder.append(player.toString()));
        return scoresStringBuilder.toString();
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}
