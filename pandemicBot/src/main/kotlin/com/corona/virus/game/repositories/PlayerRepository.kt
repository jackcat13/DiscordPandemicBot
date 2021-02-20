package com.corona.virus.game.repositories

import com.corona.virus.game.Player
import org.springframework.data.mongodb.repository.MongoRepository

interface PlayerRepository: MongoRepository<Player, String> {

    fun findByIsCoronned(isCoronned: String): List<Player>
    fun findTop10AllByOrderByScoreDesc(): List<Player>
}