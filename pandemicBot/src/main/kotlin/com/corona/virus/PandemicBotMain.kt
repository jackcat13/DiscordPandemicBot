package com.corona.virus

import com.corona.virus.config.PandemicBotProperties
import com.corona.virus.game.GameStatus
import com.corona.virus.game.PandemicGame
import com.corona.virus.game.Player
import com.corona.virus.service.PlayerService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom


val TOKEN = System.getenv("PANDEMIC_TOKEN")

@Component
class PandemicBotMain() : ListenerAdapter() {

    @Autowired private lateinit var botProperties: PandemicBotProperties
    @Autowired private lateinit var playerService: PlayerService
    @Autowired private lateinit var pandemicGame: PandemicGame
    private var currentChannel: String? = null
    private val jda: JDA = JDABuilder.createDefault(TOKEN)
        .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
        .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
        .enableIntents(GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(this)
        .build()
    private val LOG = LoggerFactory.getLogger(PandemicBotMain::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val inputMessage = event.message.contentRaw
        if (event.author.isBot) {
            return
        } else if (inputMessage == botProperties.getStartPandemicCommand()) {
            currentChannel = event.channel.name
            startGameEvent(event.channel, getPlayers())
        } else if (inputMessage == botProperties.getHealCommand()) {
            healPlayers(event)
        } else if (inputMessage == botProperties.getScoreCommand()) {
            event.channel.sendMessage(getScoresOrdered()).queue()
        } else if (inputMessage.startsWith(""+botProperties.getAddUserCommand())) {
            addUser(inputMessage, event)
        } else if (inputMessage == botProperties.getGetUsersCommand()) {
            val players = getPlayers()
            val playersStringBuilder = StringBuilder()
            players.forEach { playersStringBuilder.append(it.toString()) }
            event.message.channel.sendMessage(playersStringBuilder.toString()).queue()
        } else if (inputMessage == botProperties.getLoadUsersFromServerCommand()) {
            LOG.debug(botProperties.getLoadUsersFromServerMessage())
            jda.guilds.forEach{it.members.forEach{
                var player = Player(it.id)
                var playerName = jda.getUserById(it.id)?.name
                if (playerName != null){
                    player.playerName = playerName
                }
                savePlayer(player)
            }}
        } else if (inputMessage.startsWith(""+botProperties.getMyScoreCommand())){
            event.channel.sendMessage(getOnePlayerScore(event.message.author.id)).queue()
        }
    }

    private fun addUser(inputMessage: String, event: MessageReceivedEvent){
        var userInfos = inputMessage.substring(botProperties.getAddUserCommand()!!.length + 1).split(" ")
        LOG.info(userInfos[0] + userInfos[1])
        LOG.debug(botProperties.getCreateUserMessage(), userInfos[0])
        var player = Player(userInfos[0])
        player.playerName = userInfos[1]
        savePlayer(player)
    }

    private fun startGameEvent(channel: MessageChannel, users: List<Player>) {
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

    private fun healPlayers(event: MessageReceivedEvent) {
        if (pandemicGame.gameStatus === GameStatus.IN_PROGRESS) {
            val author = event.author
            pandemicGame.healPlayers(author.id)
            val healerPlayer = pandemicGame.getPlayers().find { it.id == author.id }
            if (pandemicGame.isHealAction) {
                //Save game at each step to avoid losses in case of crash
                savePlayer(healerPlayer!!)
                event.channel.sendMessage(""+botProperties.getEffectiveHealMessage()).queue()
                event.channel.sendMessage(getScoresOrdered()).queue()
                pandemicGame.isHealAction = false
            }
        }
    }

    private fun getScoresOrdered(): MessageEmbed {
        var embedBuilder = EmbedBuilder()
        embedBuilder.setTitle(botProperties.getScoreTitle(), null)
        embedBuilder.setColor(Color.GREEN)
        embedBuilder.setDescription(botProperties.getScoreDescriptionMessage())
        embedBuilder.addBlankField(false)
        embedBuilder.setImage(botProperties.getDiscoRaoultGifUrl())
        embedBuilder.setThumbnail(botProperties.getPandemicBotLogoUrl())
        getPlayersOrderedByScores().forEachIndexed{ index, element ->  embedBuilder.addField("" + (index+1), element.toString(), false) }
        return embedBuilder.build()
    }

    private fun getOnePlayerScore(authorId: String): MessageEmbed {
        var embedBuilder = EmbedBuilder()
        embedBuilder.setTitle(botProperties.getScoreTitle(), null)
        embedBuilder.setColor(Color.GREEN)
        embedBuilder.setDescription(botProperties.getOnePlayerScoreDescriptionMessage())
        embedBuilder.addBlankField(false)
        embedBuilder.setImage(botProperties.getDiscoRaoultGifUrl())
        embedBuilder.setThumbnail(botProperties.getPandemicBotLogoUrl())
        val currentPlayer = getPlayers().find{it.id == authorId}
        embedBuilder.addField("" + (1), currentPlayer.toString(), false)
        return embedBuilder.build()
    }

    fun savePlayer(player: Player) {
        playerService.savePlayer(player)
    }

    fun getPlayers() = playerService.getAllPlayer()

    fun getPlayersOrderedByScores() = playerService.getAllPlayersOrderedByScore()
}