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
        pandemicGame.addPlayerInGame(expectedUser)
        assertThat(pandemicGame.getPlayers().get(0).id).isEqualTo(expectedUser)
    }

    /*@Test
    fun `should not add empty player in game`(){
        val expectedUser = ""
        assertThrows<IllegalArgumentException> { pandemicGame.addPlayerInGame(expectedUser) }
    }*/
}