package com.corona.virus.game

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

class PandemicGame {

    var gameStatus: GameStatus
    private val players: MutableList<Player>
    var isHealAction: Boolean


    fun startGame(): GameStatus {
        gameStatus = if (gameStatus === GameStatus.STOPPED) {
            GameStatus.STARTED
        } else if (gameStatus === GameStatus.STARTED || gameStatus === GameStatus.IN_PROGRESS) {
            GameStatus.IN_PROGRESS
        } else {
            GameStatus.STOPPED
        }
        return gameStatus
    }

    fun addPlayerInGame(playerId: String, playerName: String, playerScore: Int) {
        var player = Player(playerId)
        player.playerName = playerName
        player.score = playerScore
        players.add(player)
        println(NEW_PLAYER_ADDED + playerName)
    }

    fun isUserCoronned(authorId: String): Boolean {
        val currentPlayer = players.stream().filter { player: Player -> player.id == authorId }.findFirst().get()
        val randomNum = ThreadLocalRandom.current().nextInt(1, 10)
        return if (randomNum == 1 && !currentPlayer.isCoronned) {
            currentPlayer.isCoronned = true
            true
        } else {
            false
        }
    }

    fun healPlayers(authorId: String) {
        players.forEach(Consumer { player: Player -> healPlayer(player, authorId) })
    }

    private fun healPlayer(currentPlayer: Player, authorId: String) {
        val healerPlayer = players.stream().filter { player: Player -> player.id == authorId }.findFirst().get()
        if (currentPlayer.isCoronned) {
            healerPlayer.score = healerPlayer.score + 1
            currentPlayer.isCoronned = false
            isHealAction = true
        }
    }

    fun getPlayers(): List<Player> {
        return players
    }

    companion object {
        const val NEW_PLAYER_ADDED = "New player added"
    }

    init {
        gameStatus = GameStatus.STOPPED
        players = ArrayList()
        isHealAction = false
    }
}