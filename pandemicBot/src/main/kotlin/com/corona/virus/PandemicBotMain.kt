package com.corona.virus

import com.corona.virus.config.PandemicBotProperties
import com.corona.virus.controller.PandemicBotController
import com.corona.virus.controller.PlayerController
import com.corona.virus.game.GameStatus
import com.corona.virus.game.PandemicGame
import com.corona.virus.game.Player
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

val TOKEN = System.getenv("PANDEMIC_TOKEN")

@Component
class PandemicBotMain() : ListenerAdapter() {

    @Autowired private lateinit var botProperties: PandemicBotProperties
    @Autowired private lateinit var playerController: PlayerController
    @Autowired private lateinit var pandemicGame: PandemicGame
    @Autowired private lateinit var pandemicBotController: PandemicBotController
    private val jda: JDA = JDABuilder.createDefault(TOKEN)
        .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
        .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
        .enableIntents(GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(this)
        .build()
    private val LOG = LoggerFactory.getLogger(PandemicBotMain::class.java)

    @PostConstruct
    fun init(){
        pandemicBotController.pandemicGame = pandemicGame
        pandemicBotController.jda = jda
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val inputMessage = event.message.contentRaw
        when {
            event.author.isBot -> {
                return
            }
            inputMessage == botProperties.getStartPandemicCommand() -> {
                pandemicBotController.currentChannel = event.channel.name
                pandemicBotController.startGameEvent(event.channel, playerController.getPlayers())
            }
            inputMessage == botProperties.getHealCommand() -> {
                pandemicBotController.healPlayers(event)
            }
            inputMessage == botProperties.getScoreCommand() -> {
                event.channel.sendMessage(pandemicBotController.getScoresOrdered()).queue()
            }
            inputMessage.startsWith(""+botProperties.getAddUserCommand()) -> {
                pandemicBotController.addUser(inputMessage)
            }
            inputMessage == botProperties.getGetUsersCommand() -> {
                event.message.channel.sendMessage(pandemicBotController.getUsers().toString()).queue()
            }
            inputMessage == botProperties.getLoadUsersFromServerCommand() -> {
                pandemicBotController.loadUsersFromServer()
            }
            inputMessage.startsWith(""+botProperties.getMyScoreCommand()) -> {
                event.channel.sendMessage(pandemicBotController.getOnePlayerScore(event.message.author.id)).queue()
            }
            inputMessage == botProperties.getChehCommand() -> {
                pandemicBotController.chehCommand(event)
            }
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val user = event.member
        val player = Player(user.id)
        player.playerName = user.effectiveName
        playerController.savePlayer(player)
        if (pandemicGame.gameStatus != GameStatus.STOPPED) pandemicGame.addPlayerInGame(player.id, player.playerName, 0)
        LOG.debug("New player {} joined the server, hence he has been automatically been save in DB as a new player", player.playerName)
    }




}