package com.corona.virus;

import com.corona.virus.game.GameStatus;
import com.corona.virus.game.PandemicGame;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;

public class PandemicBotMain extends ListenerAdapter {

    public static final String TOKEN = "NjkwMjEyNzk1Mzk1ODY2NjM0.XnSqXQ.5xGaT34LrGEe2xH-hnUluIcwuSY";
    public static final String START_PANDEMIC = "!startPandemic";
    public static final String START_PANDEMIC_MESSAGE = "Pandemic is there. Users in this server will be coronned. Fear my almighty power!!!";
    public static final String HEAL_MESSAGE = "!heal";
    public static final String CORONNED_MESSAGE = " is coronned. First player to heal him will increase his score. Use command !heal to do so.";

    private PandemicGame pandemicGame = new PandemicGame();

    public static void main(String[] args) throws LoginException {
        JDABuilder jdbaBuilder = new JDABuilder(AccountType.BOT);
        jdbaBuilder.setToken(TOKEN);
        jdbaBuilder.addEventListener(new PandemicBotMain());
        jdbaBuilder.buildAsync();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String inputMessage = event.getMessage().getContentRaw();
        System.out.println(inputMessage + " / author : " + event.getAuthor().getId());
        if (event.getAuthor().isBot()){
            return;
        }
        else if(inputMessage.equals(START_PANDEMIC)){
            System.out.println(event.getChannel());
            System.out.println(event.getJDA().getUsers());
            startGameEvent(event.getChannel(), event.getJDA().getUsers());
        }
        else if(inputMessage.equals(HEAL_MESSAGE)){
            healPlayers(event);
        }
        else{
            gameLoop(event);
        }
    }

    private void startGameEvent(MessageChannel channel, List<User> users) {
        GameStatus gameStatus = pandemicGame.startGame();
        if (gameStatus == GameStatus.STARTED){
            channel.sendMessage(START_PANDEMIC_MESSAGE).queue();
            createPlayers(users);
            pandemicGame.setGameStatus(GameStatus.IN_PROGRESS);
        }
    }

    private void createPlayers(List<User> users){
        users.forEach((user) -> pandemicGame.addPlayerInGame(user.getName()));
    }

    private void gameLoop(MessageReceivedEvent event) {
        if (pandemicGame.getGameStatus() == GameStatus.IN_PROGRESS){
            User author = event.getAuthor();
            if (pandemicGame.isUserCoronned(author.getName())){
                event.getChannel().sendMessage(author.getName() + CORONNED_MESSAGE).queue();
            }
        }
    }

    private void healPlayers(MessageReceivedEvent event){
        if (pandemicGame.getGameStatus() == GameStatus.IN_PROGRESS){
            User author = event.getAuthor();
            pandemicGame.healPlayers(author.getName());
            event.getChannel().sendMessage(pandemicGame.getGameScores()).queue();
        }
    }
}
