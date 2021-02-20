package com.corona.virus

import com.corona.virus.game.GameStatus
import com.corona.virus.game.PandemicGame
import com.corona.virus.game.Player
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import java.awt.Color
import java.lang.StringBuilder
import java.util.concurrent.ThreadLocalRandom


val TOKEN = System.getenv("PANDEMIC_TOKEN")
const val START_PANDEMIC = "!startPandemic"
const val START_PANDEMIC_MESSAGE =
    "Pandemic is there. Users in this server will be coronned. Fear my almighty power!!! (To visualize the scores, you can use the !score command)"
const val HEAL_MESSAGE = "!heal"
const val SCORE_MESSAGE = "!score"
const val BOLD_START = "**"
const val CORONNED_MESSAGE =
    " is coronned.** First player to heal him will increase his score. Use command **!heal** to do so."
const val EFFECTIVE_HEAL_MESSAGE = "Chloroquine heal is super effective. Scores: \n"
const val ADD_USER_MESSAGE = "!addUser"
const val GET_USERS_MESSAGE = "!getUsers"
const val LOAD_USERS_FROM_SERVER_MESSAGE = "!loadUsersFromServer"

class PandemicBotMain(private val pandemicSpringBootApplication: PandemicSpringBootApplication) : ListenerAdapter() {

    private val pandemicGame = PandemicGame()
    private var currentChannel: String? = null
    private val jda: JDA = JDABuilder.createDefault(TOKEN)
        .addEventListeners(this)
        .build()
    private val LOG = LoggerFactory.getLogger(PandemicBotMain::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val inputMessage = event.message.contentRaw
        if (event.author.isBot) {
            return
        } else if (inputMessage == START_PANDEMIC) {
            currentChannel = event.channel.name
            startGameEvent(event.channel, pandemicSpringBootApplication.getPlayers())
        } else if (inputMessage == HEAL_MESSAGE) {
            healPlayers(event)
        } else if (inputMessage == SCORE_MESSAGE) {
            event.channel.sendMessage(getScoresOrdered()).queue()
        } else if (inputMessage.startsWith(ADD_USER_MESSAGE)) {
            addUser(inputMessage, event)
        } else if (inputMessage.equals(GET_USERS_MESSAGE)) {
            val players = pandemicSpringBootApplication.getPlayers()
            val playersStringBuilder = StringBuilder()
            players.forEach { playersStringBuilder.append(it.toString()) }
            event.message.channel.sendMessage(playersStringBuilder.toString()).queue()
        } else if (inputMessage.equals(LOAD_USERS_FROM_SERVER_MESSAGE)) {
            LOG.debug("Received message starting loading all users from server to DB")
            jda.guilds.forEach{it.members.forEach{
                var player = Player(it.id)
                var playerName = jda.getUserById(it.id)?.name
                if (playerName != null){
                    player.playerName = playerName
                }
                pandemicSpringBootApplication.savePlayer(player)
            }}
        }
    }

    private fun addUser(inputMessage: String, event: MessageReceivedEvent){
        var userInfos = inputMessage.substring(ADD_USER_MESSAGE.length + 1).split(" ")
        LOG.info(userInfos[0] + userInfos[1])
        LOG.debug("Received message to create user {}", userInfos[0])
        var player = Player(userInfos[0])
        player.playerName = userInfos[1]
        pandemicSpringBootApplication.savePlayer(player)
    }

    private fun startGameEvent(channel: MessageChannel, users: List<Player>) {
        val gameStatus = pandemicGame.startGame()
        if (gameStatus === GameStatus.STARTED) {
            channel.sendMessage(START_PANDEMIC_MESSAGE).queue()
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
            embedBuilder.setTitle("Virus attack", null)
            embedBuilder.setColor(Color.RED)
            embedBuilder.setDescription("A player has been coronned")
            embedBuilder.addBlankField(false)
            embedBuilder.setImage("https://cdn.discordapp.com/attachments/799359211178819624/812140501901901824/gif-poutine-pandemic-bot.gif")
            embedBuilder.setThumbnail("https://cdn.discordapp.com/emojis/809604534078799882.png?v=1")
            val players = pandemicGame.getPlayers()
            val player = players[ThreadLocalRandom.current().nextInt(players.size)]
            if (pandemicGame.isUserCoronned(player.id)) {
                embedBuilder.addField("Corroned", BOLD_START + player.playerName + CORONNED_MESSAGE, false)
                jda.getTextChannelsByName(currentChannel!!, true)[0].sendMessage(embedBuilder.build()).queue()
            }
        }
    }

    private fun healPlayers(event: MessageReceivedEvent) {
        if (pandemicGame.gameStatus === GameStatus.IN_PROGRESS) {
            val author = event.author
            pandemicGame.healPlayers(author.id)
            if (pandemicGame.isHealAction) {
                //Save game at each step to avoid losses in case of crash
                pandemicGame.getPlayers().forEach { pandemicSpringBootApplication.savePlayer(it) }
                event.channel.sendMessage(EFFECTIVE_HEAL_MESSAGE).queue()
                event.channel.sendMessage(getScoresOrdered()).queue()
                pandemicGame.isHealAction = false
            }
        }
    }

    private fun getScoresOrdered(): MessageEmbed {
        var embedBuilder = EmbedBuilder()
        embedBuilder.setTitle("Scores #Chloroquine", null)
        embedBuilder.setColor(Color.GREEN)
        embedBuilder.setDescription("Top 10 scores oredered desc")
        embedBuilder.addBlankField(false)
        embedBuilder.setImage("https://cdn.discordapp.com/attachments/799359211178819624/812164915124109312/gif-raoult-et-pangolin_v2.gif")
        embedBuilder.setThumbnail("https://cdn.discordapp.com/emojis/809604534078799882.png?v=1")
        pandemicSpringBootApplication.getPlayersOrderedByScores().forEachIndexed{ index, element ->  embedBuilder.addField("" + (index+1), element.toString(), false) }
        return embedBuilder.build()
    }
}