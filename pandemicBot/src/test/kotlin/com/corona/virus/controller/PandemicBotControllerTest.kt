package com.corona.virus.controller

import com.corona.virus.config.PandemicBotProperties
import com.corona.virus.game.GameStatus
import com.corona.virus.game.PandemicGame
import com.corona.virus.game.Player
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PandemicBotControllerTest {

    @MockK
    lateinit var botProperties: PandemicBotProperties

    @MockK
    lateinit var playerController: PlayerController

    @InjectMockKs
    var pandemicBotController = PandemicBotController()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        pandemicBotController.pandemicGame = PandemicGame()
        pandemicBotController.currentChannel = "test"
        every { botProperties.getAddUserCommand() } returns "!addUser"
        every { botProperties.getStartPandemicMessage() } returns "!startPandemic"
        every { botProperties.getCreateUserMessage() } returns "OSEF"
        every { botProperties.getVirusAttackTitle() } returns "OSEF"
        every { botProperties.getPlayerCoronnedMessage() } returns "OSEF"
        every { botProperties.getEffectiveHealMessage() } returns "OSEF"
        every { botProperties.getScoreTitle() } returns "OSEF"
        every { botProperties.getScoreDescriptionMessage() } returns "OSEF"
        every { botProperties.getDiscoRaoultGifUrl() } returns "http://osef.osef"
        every { botProperties.getPandemicBotLogoUrl() } returns "http://osef.osef"
        every { playerController.savePlayer(any()) } just Runs
    }

    @Test
    fun `should add user bob with his id in pandemic game`() {
        pandemicBotController.pandemicGame.gameStatus = GameStatus.STARTED
        pandemicBotController.addUser("!addUser 123 bob")
        assertThat(pandemicBotController.pandemicGame.getPlayers()[0].id).isEqualTo("123")
        assertThat(pandemicBotController.pandemicGame.getPlayers()[0].playerName).isEqualTo("bob")
    }

    @Test
    fun `should not add user if player name is missing`() {
        pandemicBotController.pandemicGame.gameStatus = GameStatus.STARTED
        assertThrows<IllegalArgumentException> { pandemicBotController.addUser("!addUser 123") }
    }

    @Test
    fun `should not add user if arguments are missing`() {
        pandemicBotController.pandemicGame.gameStatus = GameStatus.STARTED
        assertThrows<IllegalArgumentException> { pandemicBotController.addUser("!addUser") }
    }

    @Test
    fun `should not add user if too many arguments are provided`() {
        pandemicBotController.pandemicGame.gameStatus = GameStatus.STARTED
        assertThrows<IllegalArgumentException> { pandemicBotController.addUser("!addUser 123 bob notExpected") }
    }

    @Test
    fun `should start the game`(){
        mockkConstructor(MessageAction::class)
        mockkConstructor(Thread::class)
        val messageChannel = mockk<MessageChannel>()
        val players = ArrayList<Player>()
        players.add(Player("123"))
        every { messageChannel.sendMessage("!startPandemic").queue() } just Runs
        pandemicBotController.startGameEvent(messageChannel, players)
        assertThat(pandemicBotController.pandemicGame.getPlayers()[0].id).isEqualTo("123")
        assertThat(pandemicBotController.pandemicGame.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
    }

    @Test
    fun `should keep the game in progress`(){
        pandemicBotController.pandemicGame.gameStatus = GameStatus.STARTED
        mockkConstructor(MessageAction::class)
        mockkConstructor(Thread::class)
        val messageChannel = mockk<MessageChannel>()
        val players = ArrayList<Player>()
        players.add(Player("123"))
        every { messageChannel.sendMessage("!startPandemic").queue() } just Runs
        pandemicBotController.startGameEvent(messageChannel, players)
        assertThat(pandemicBotController.pandemicGame.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
    }

    @Test
    fun `should heal infected players`(){
        pandemicBotController.pandemicGame.gameStatus = GameStatus.IN_PROGRESS
        val event = mockk< MessageReceivedEvent>()
        val coronnedPlayer = Player("123")
        coronnedPlayer.isCoronned = true
        pandemicBotController.pandemicGame.getPlayers().add(coronnedPlayer)
        every { event.author } returns User.fromId("123")
        every { event.channel.sendMessage("OSEF").queue() } just Runs
        every { playerController.getPlayersOrderedByScores() } returns ArrayList()
        every { event.channel.sendMessage(any<MessageEmbed>()).queue() } just Runs
        pandemicBotController.healPlayers(event)
        assertThat(pandemicBotController.pandemicGame.getPlayers()[0].isCoronned).isFalse
    }
}