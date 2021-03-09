package com.corona.virus.controller

import com.corona.virus.game.Player
import com.corona.virus.service.PlayerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PlayerController {

    @Autowired
    private lateinit var playerService: PlayerService

    fun savePlayer(player: Player) {
        playerService.savePlayer(player)
    }

    fun getPlayers() = playerService.getAllPlayer()

    fun getPlayersOrderedByScores() = playerService.getAllPlayersOrderedByScore()
}