package com.corona.virus.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Component

@Component
class PandemicBotProperties {

    @Autowired private lateinit var env: Environment
    private var languageCode = null

    //Load language
    fun getLanguage() = if (languageCode == null) env.getProperty("bot.language") else languageCode

    //Commands
    fun getStartPandemicCommand() = env.getProperty("bot.commands.startPandemic")
    fun getScoreCommand() = env.getProperty("bot.commands.score")
    fun getAddUserCommand() = env.getProperty("bot.commands.addUser")
    fun getHealCommand() = env.getProperty("bot.commands.heal")
    fun getGetUsersCommand() = env.getProperty("bot.commands.getUsers")
    fun getLoadUsersFromServerCommand() = env.getProperty("bot.commands.loadUsersFromServer")
    fun getMyScoreCommand() = env.getProperty("bot.commands.myScore")

    //Messages
    fun getStartPandemicMessage() = env.getProperty("bot.messages.${getLanguage()}.startPandemic")
    fun getBoldMarkdown() = env.getProperty("bot.messages.${getLanguage()}.boldMarkdown")
    fun getCoronnedTitle() = env.getProperty("bot.messages.${getLanguage()}.coronnedTitle")
    fun getCoronnedMessage() = env.getProperty("bot.messages.${getLanguage()}.coronned")
    fun getEffectiveHealMessage() = env.getProperty("bot.messages.${getLanguage()}.effectiveHeal")
    fun getLoadUsersFromServerMessage() = env.getProperty("bot.messages.${getLanguage()}.loadUsersFromServer")
    fun getCreateUserMessage() = env.getProperty("bot.messages.${getLanguage()}.createUser")
    fun getVirusAttackTitle() = env.getProperty("bot.messages.${getLanguage()}.virusAttackTitle")
    fun getPlayerCoronnedMessage() = env.getProperty("bot.messages.${getLanguage()}.playerCoronned")
    fun getScoreTitle() = env.getProperty("bot.messages.${getLanguage()}.scoreTitle")
    fun getScoreDescriptionMessage() = env.getProperty("bot.messages.${getLanguage()}.scoreDescription")
    fun getOnePlayerScoreDescriptionMessage() = env.getProperty("bot.messages.${getLanguage()}.onePlayerScoreDescription")

    //Images URLs
    fun getPandemicPoutineGifUrl() = env.getProperty("bot.imagesUrl.pandemicPoutine")
    fun getPandemicBotLogoUrl() = env.getProperty("bot.imagesUrl.pandemicBotLogo")
    fun getDiscoRaoultGifUrl() = env.getProperty("bot.imagesUrl.discoRaoult")
    
}