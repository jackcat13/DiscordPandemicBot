package com.corona.virus.game

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PandemicGameTest {

    lateinit var pandemicGame: PandemicGame

    @BeforeEach
    fun setUp(){
        pandemicGame = PandemicGame()
    }

    @Test
    fun `should add player in game`(){
        val expectedUser = "jackCat13"
        pandemicGame.addPlayerInGame("0", expectedUser, 0)
        assertThat(pandemicGame.getPlayers()[0].playerName).isEqualTo(expectedUser)
    }

    @Test
    fun `should heal the coronned player`(){
        val players = ArrayList<Player>()
        val corronedPlayer = Player("0")
        corronedPlayer.isCoronned = true
        val notCoronnedPlayer = Player("1")
        players.add(corronedPlayer)
        players.add(notCoronnedPlayer)
        pandemicGame.addPlayerInGame("0", "jackCat13", 0)
        pandemicGame.getPlayers()[0].isCoronned = true
        pandemicGame.addPlayerInGame("1", "bob", 0)
        assertThat(pandemicGame.getPlayers()[0].isCoronned).isTrue
        assertThat(pandemicGame.getPlayers()[1].isCoronned).isFalse
        pandemicGame.healPlayers("1")
        assertThat(pandemicGame.getPlayers()[0].isCoronned).isFalse
        assertThat(pandemicGame.getPlayers()[1].isCoronned).isFalse
    }

    @Test
    fun `should start the game if the game is in stopped state`(){
        pandemicGame.gameStatus = GameStatus.STOPPED
        assertThat(pandemicGame.gameStatus).isEqualTo(GameStatus.STOPPED)
        pandemicGame.startGame()
        assertThat(pandemicGame.gameStatus).isEqualTo(GameStatus.STARTED)
    }

    @Test
    fun `should set the game in progress if the game is in started state`(){
        pandemicGame.gameStatus = GameStatus.STARTED
        assertThat(pandemicGame.gameStatus).isEqualTo(GameStatus.STARTED)
        pandemicGame.startGame()
        assertThat(pandemicGame.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
    }

    @Test
    fun `should keep the game in progress if the game is in progress state`(){
        pandemicGame.gameStatus = GameStatus.STARTED
        assertThat(pandemicGame.gameStatus).isEqualTo(GameStatus.STARTED)
        pandemicGame.startGame()
        assertThat(pandemicGame.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
    }
}