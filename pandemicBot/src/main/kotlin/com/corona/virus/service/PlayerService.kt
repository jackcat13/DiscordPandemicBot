package com.corona.virus.service

import com.corona.virus.game.repositories.PlayerRepository
import com.corona.virus.game.Player
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PlayerService() {

    @Autowired lateinit var playerRepository: PlayerRepository
    private val LOG = LoggerFactory.getLogger(PlayerService::class.java)

    fun savePlayer(player: Player){
        LOG.debug("Saving in DB player {}", player.id)
        playerRepository.save(player)
        LOG.debug("Player {} saved in DB", player.id)
    }

    fun getAllPlayer() = playerRepository.findAll()

    fun getAllPlayersOrderedByScore() = playerRepository.findTop10AllByOrderByScoreDesc()
}