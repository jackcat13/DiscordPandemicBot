package com.corona.virus.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Component

@Component
class PandemicBotProperties {

    @Autowired private lateinit var env: Environment

    //Commands
    fun getStartPandemicCommand() = env.getProperty("bot.commands.startPandemic")
    fun getScoreCommand() = env.getProperty("bot.commands.score")
    fun getAddUserCommand() = env.getProperty("bot.commands.addUser")
    fun getHealCommand() = env.getProperty("bot.commands.heal")
    fun getGetUsersCommand() = env.getProperty("bot.commands.getUsers")
    fun getLoadUsersFromServerCommand() = env.getProperty("bot.commands.loadUsersFromServer")

    //Messages
    fun getStartPandemicMessage() = env.getProperty("bot.messages.startPandemic")
    fun getBoldMarkdown() = env.getProperty("bot.messages.boldMarkdown")
    fun getCoronnedTitle() = env.getProperty("bot.messages.coronnedTitle")
    fun getCoronnedMessage() = env.getProperty("bot.messages.coronned")
    fun getEffectiveHealMessage() = env.getProperty("bot.messages.effectiveHeal")
    fun getLoadUsersFromServerMessage() = env.getProperty("bot.messages.loadUsersFromServer")
    fun getCreateUserMessage() = env.getProperty("bot.messages.createUser")
    fun getVirusAttackTitle() = env.getProperty("bot.messages.virusAttackTitle")
    fun getPlayerCoronnedMessage() = env.getProperty("bot.messages.playerCoronned")
    fun getScoreTitle() = env.getProperty("bot.messages.scoreTitle")
    fun getScoreDescriptionMessage() = env.getProperty("bot.messages.scoreDescription")

    //Images URLs
    fun getPandemicPoutineGifUrl() = env.getProperty("bot.imagesUrl.pandemicPoutine")
    fun getPandemicBotLogoUrl() = env.getProperty("bot.imagesUrl.pandemicBotLogo")
    fun getDiscoRaoultGifUrl() = env.getProperty("bot.imagesUrl.discoRaoult")

}