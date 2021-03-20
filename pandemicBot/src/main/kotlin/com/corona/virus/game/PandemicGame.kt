package com.corona.virus.game

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.absoluteValue

@Component
class PandemicGame {

    var gameStatus = GameStatus.STOPPED
    private val players = ArrayList<Player>()
    var isHealAction = false
    var healMessagesHistory = LinkedHashMap<String, Pair<OffsetDateTime, Boolean>>()

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
        players.forEach{ healPlayer(it, authorId) }
    }

    private fun healPlayer(currentPlayer: Player, authorId: String) {
        val healerPlayer = players.stream().filter { it.id == authorId }.findFirst().get()
        if (currentPlayer.isCoronned) {
            healerPlayer.score++
            currentPlayer.isCoronned = false
            isHealAction = true
        }
    }

    fun getPlayers() =  players

    fun isChehCommandEffective(event: MessageReceivedEvent, authorId: String): Boolean {
        val healMessageHistoryEntries = healMessagesHistory.entries
        val lastHealAuthor = healMessageHistoryEntries.last().key
        val beforeLastHealAuthor = healMessageHistoryEntries.elementAt(healMessageHistoryEntries.size-2).key
        val lastHealMessageEffective = healMessageHistoryEntries.last().value.second
        val beforeLastHealMessageEffective = healMessageHistoryEntries.elementAt(healMessageHistoryEntries.size-2).value.second
        return (lastHealAuthor != beforeLastHealAuthor) &&
            ( (!lastHealMessageEffective && beforeLastHealMessageEffective) ||
            (lastHealMessageEffective && !beforeLastHealMessageEffective) )
    }

    fun processCheh(authorId: String) {
        val cheherPlayer = players.stream().filter { it.id == authorId }.findFirst().get()
        cheherPlayer.score+=2
    }


}