package com.corona.virus

import com.corona.virus.game.GameStatus
import com.corona.virus.game.PandemicGame
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer
import javax.security.auth.login.LoginException

val TOKEN = System.getenv("PANDEMIC_TOKEN")
const val START_PANDEMIC = "!startPandemic"
const val START_PANDEMIC_MESSAGE =
    "Pandemic is there. Users in this server will be coronned. Fear my almighty power!!! (To visualize the scores, you can use the !score command)"
const val HEAL_MESSAGE = "!heal"
const val SCORE_MESSAGE = "!score"
const val CORONNED_MESSAGE =
    " is coronned. First player to heal him will increase his score. Use command !heal to do so."
const val EFFECTIVE_HEAL_MESSAGE = "Effective heal performed. Scores: \n"

class PandemicBotMain : ListenerAdapter() {

    private val pandemicGame = PandemicGame()
    private var currentChannel: String? = null

    companion object {
        private var jda: JDA? = null
        @Throws(LoginException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            jda = JDABuilder.createDefault(TOKEN)
                .addEventListeners(PandemicBotMain())
                .build()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val inputMessage = event.message.contentRaw
        if (event.author.isBot) {
            return
        } else if (inputMessage == START_PANDEMIC) {
            currentChannel = event.channel.name
            startGameEvent(event.channel, event.guild.members)
        } else if (inputMessage == HEAL_MESSAGE) {
            healPlayers(event)
        } else if (inputMessage == SCORE_MESSAGE) {
            event.channel.sendMessage(pandemicGame.gameScores).queue()
        }
    }

    private fun startGameEvent(channel: MessageChannel, users: List<Member>) {
        val gameStatus = pandemicGame.startGame()
        if (gameStatus === GameStatus.STARTED) {
            channel.sendMessage(START_PANDEMIC_MESSAGE).queue()
            createPlayers(users)
            pandemicGame.gameStatus = GameStatus.IN_PROGRESS
            val loopGame = Thread(gameLoopThread)
            loopGame.start()
        }
    }

    private fun createPlayers(users: List<Member>) {
        users.forEach(Consumer { member: Member ->
            val user = member.user
            if (!user.isBot) {
                pandemicGame.addPlayerInGame(user.name)
            }
        })
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
            val players = pandemicGame.getPlayers()
            val player = players[ThreadLocalRandom.current().nextInt(players.size)]
            if (pandemicGame.isUserCoronned(player.id)) {
                jda!!.getTextChannelsByName(currentChannel!!, true)[0].sendMessage(player.id + CORONNED_MESSAGE).queue()
            }
        }
    }

    private fun healPlayers(event: MessageReceivedEvent) {
        if (pandemicGame.gameStatus === GameStatus.IN_PROGRESS) {
            val author = event.author
            pandemicGame.healPlayers(author.name)
            if (pandemicGame.isHealAction) {
                event.channel.sendMessage(EFFECTIVE_HEAL_MESSAGE + pandemicGame.gameScores).queue()
                pandemicGame.isHealAction = false
            }
        }
    }
}