package com.corona.virus;

import com.corona.virus.game.GameStatus;
import com.corona.virus.game.PandemicGame;
import com.corona.virus.game.Player;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PandemicBotMain extends ListenerAdapter {

    public static final String TOKEN = "NjkwMjEyNzk1Mzk1ODY2NjM0.XnOIwQ.kcQ5RfttoF54bIyaD4mmTlfkiPc";
    public static final String START_PANDEMIC = "!startPandemic";
    public static final String START_PANDEMIC_MESSAGE = "Pandemic is there. Users in this server will be coronned. Fear my almighty power!!! (To visualize the scores, you can use the !score command)";
    public static final String HEAL_MESSAGE = "!heal";
    public static final String SCORE_MESSAGE = "!score";
    public static final String PANDEMIC_CHANNEL = "pandemic";
    public static final String CORONNED_MESSAGE = " is coronned. First player to heal him will increase his score. Use command !heal to do so.";
    public static final String EFFECTIVE_HEAL_MESSAGE = "Effective heal performed. Scores: \n";

    private PandemicGame pandemicGame = new PandemicGame();

    private static JDA jda;

    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(TOKEN)
                .addEventListeners(new PandemicBotMain())
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String inputMessage = event.getMessage().getContentRaw();
        if (event.getAuthor().isBot()){
            return;
        }
        else if(inputMessage.equals(START_PANDEMIC)){
            startGameEvent(event.getChannel(), jda.getUsers());
        }
        else if(inputMessage.equals(HEAL_MESSAGE)){
            healPlayers(event);
        }
        else if(inputMessage.equals(SCORE_MESSAGE)) {
            event.getChannel().sendMessage(pandemicGame.getGameScores()).queue();
        }
    }

    private void startGameEvent(MessageChannel channel, List<User> users) {
        GameStatus gameStatus = pandemicGame.startGame();
        if (gameStatus == GameStatus.STARTED){
            channel.sendMessage(START_PANDEMIC_MESSAGE).queue();
            createPlayers(users);
            pandemicGame.setGameStatus(GameStatus.IN_PROGRESS);
            Thread loopGame = new Thread(gameLoopThread);
            loopGame.start();
        }
    }

    private void createPlayers(List<User> users){
        users.forEach((user) -> {
            if (!user.isBot()) {
                pandemicGame.addPlayerInGame(user.getName());
            }
        });
    }

    private Runnable gameLoopThread = () -> {
        while(true){
            this.gameLoop();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void gameLoop() {
        if (pandemicGame.getGameStatus() == GameStatus.IN_PROGRESS){
            List<Player> players = pandemicGame.getPlayers();
            Player player = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            if (pandemicGame.isUserCoronned(player.getId())){
                jda.getTextChannelsByName(PANDEMIC_CHANNEL, true).get(0).sendMessage(player.getId() + CORONNED_MESSAGE).queue();
            }
        }
    }

    private void healPlayers(MessageReceivedEvent event){
        if (pandemicGame.getGameStatus() == GameStatus.IN_PROGRESS){
            User author = event.getAuthor();
            pandemicGame.healPlayers(author.getName());
            if (pandemicGame.isHealAction()) {
                event.getChannel().sendMessage(EFFECTIVE_HEAL_MESSAGE + pandemicGame.getGameScores()).queue();
                pandemicGame.setHealAction(false);
            }
        }
    }
}
