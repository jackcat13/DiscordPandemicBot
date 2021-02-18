package com.corona.virus

import com.corona.virus.game.Player
import com.corona.virus.service.PlayerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PandemicSpringBootApplication {

    @Autowired private lateinit var playerService: PlayerService
    private val pandemicBotMain = PandemicBotMain(this)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<PandemicSpringBootApplication>(*args)
        }
    }

    fun savePlayer(player: Player) {
        playerService.savePlayer(player)
    }

    fun getPlayers() = playerService.getAllPlayer()

    fun getPlayersOrderedByScores() = playerService.getAllPlayersOrderedByScore()
}