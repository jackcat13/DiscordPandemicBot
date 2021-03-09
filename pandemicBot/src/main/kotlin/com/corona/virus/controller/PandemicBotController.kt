package com.corona.virus.controller

import com.corona.virus.config.PandemicBotProperties
import com.corona.virus.game.GameStatus
import com.corona.virus.game.PandemicGame
import com.corona.virus.game.Player
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.awt.Color
import java.lang.IllegalArgumentException
import java.util.concurrent.ThreadLocalRandom

@Component
class PandemicBotController() {

    @Autowired private lateinit var botProperties: PandemicBotProperties
    @Autowired private lateinit var playerController: PlayerController
    lateinit var pandemicGame: PandemicGame
    lateinit var jda: JDA
    lateinit var currentChannel: String
    private val LOG = LoggerFactory.getLogger(PandemicBotController::class.java)

    fun addUser(inputMessage: String){
        if (inputMessage.length == botProperties.getAddUserCommand()!!.length){
            throw IllegalArgumentException("Add user command must respect following syntax : '!addUser \$playerId \$playerName'")
        }
        var userInfos = inputMessage.substring(botProperties.getAddUserCommand()!!.length + 1).split(" ")
        if (userInfos.size != 2){
            throw IllegalArgumentException("Add user command must respect following syntax : '!addUser \$playerId \$playerName'")
        }
        LOG.info(userInfos[0] + userInfos[1])
        LOG.debug(botProperties.getCreateUserMessage(), userInfos[0])
        var player = Player(userInfos[0])
        player.playerName = userInfos[1]
        if (pandemicGame.gameStatus != GameStatus.STOPPED) pandemicGame.addPlayerInGame(player.id, player.playerName, 0)
        playerController.savePlayer(player)
    }

    fun startGameEvent(channel: MessageChannel, users: List<Player>) {
        val gameStatus = pandemicGame.startGame()
        if (gameStatus === GameStatus.STARTED) {
            channel.sendMessage(""+botProperties.getStartPandemicMessage()).queue()
            createPlayers(users)
            pandemicGame.gameStatus = GameStatus.IN_PROGRESS
            val loopGame = Thread(gameLoopThread)
            loopGame.start()
        }
    }

    private fun createPlayers(users: List<Player>) {
        users.forEach {
            pandemicGame.addPlayerInGame(it.id, it.playerName, it.score)
        }
    }

    private val gameLoopThread = Runnable {
        while (true) {
            gameLoop()
            try {
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun gameLoop() {
        if (pandemicGame.gameStatus === GameStatus.IN_PROGRESS) {
            var embedBuilder = EmbedBuilder()
            embedBuilder.setTitle(botProperties.getVirusAttackTitle(), null)
            embedBuilder.setColor(Color.RED)
            embedBuilder.setDescription(botProperties.getPlayerCoronnedMessage())
            embedBuilder.addBlankField(false)
            embedBuilder.setImage(botProperties.getPandemicPoutineGifUrl())
            embedBuilder.setThumbnail(botProperties.getPandemicBotLogoUrl())
            val players = pandemicGame.getPlayers()
            val player = players[ThreadLocalRandom.current().nextInt(players.size)]
            if (pandemicGame.isUserCoronned(player.id)) {
                embedBuilder.addField(""+botProperties.getCoronnedTitle(), botProperties.getBoldMarkdown() + player.playerName + botProperties.getCoronnedMessage(), false)
                jda.getTextChannelsByName(currentChannel!!, true)[0].sendMessage(embedBuilder.build()).queue()
            }
        }
    }

    fun healPlayers(event: MessageReceivedEvent) {
        if (pandemicGame.gameStatus === GameStatus.IN_PROGRESS) {
            val author = event.author
            pandemicGame.healPlayers(author.id)
            val healerPlayer = pandemicGame.getPlayers().find { it.id == author.id }
            if (pandemicGame.isHealAction) {
                //Save game at each step to avoid losses in case of crash
                playerController.savePlayer(healerPlayer!!)
                event.channel.sendMessage(""+botProperties.getEffectiveHealMessage()).queue()
                event.channel.sendMessage(getScoresOrdered()).queue()
                pandemicGame.isHealAction = false
            }
        }
    }

    fun getScoresOrdered(): MessageEmbed {
        var embedBuilder = EmbedBuilder()
        embedBuilder.setTitle(botProperties.getScoreTitle(), null)
        embedBuilder.setColor(Color.GREEN)
        embedBuilder.setDescription(botProperties.getScoreDescriptionMessage())
        embedBuilder.addBlankField(false)
        embedBuilder.setImage(botProperties.getDiscoRaoultGifUrl())
        embedBuilder.setThumbnail(botProperties.getPandemicBotLogoUrl())
        playerController.getPlayersOrderedByScores().forEachIndexed{ index, element ->  embedBuilder.addField("" + (index+1), element.toString(), false) }
        return embedBuilder.build()
    }

    fun getOnePlayerScore(authorId: String): MessageEmbed {
        var embedBuilder = EmbedBuilder()
        embedBuilder.setTitle(botProperties.getScoreTitle(), null)
        embedBuilder.setColor(Color.GREEN)
        embedBuilder.setDescription(botProperties.getOnePlayerScoreDescriptionMessage())
        embedBuilder.addBlankField(false)
        embedBuilder.setImage(botProperties.getDiscoRaoultGifUrl())
        embedBuilder.setThumbnail(botProperties.getPandemicBotLogoUrl())
        val currentPlayer = playerController.getPlayers().find{it.id == authorId}
        embedBuilder.addField("" + (1), currentPlayer.toString(), false)
        return embedBuilder.build()
    }

    fun getUsers(): StringBuilder {
        val players = playerController.getPlayers()
        val playersStringBuilder = StringBuilder()
        players.forEach { playersStringBuilder.append(it.toString()) }
        return playersStringBuilder
    }

    fun loadUsersFromServer(){
        LOG.debug(botProperties.getLoadUsersFromServerMessage())
        jda.guilds.forEach{it.members.forEach{
            var player = Player(it.id)
            var playerName = jda.getUserById(it.id)?.name
            if (playerName != null){
                player.playerName = playerName
            }
            playerController.savePlayer(player)
        }}
    }

}