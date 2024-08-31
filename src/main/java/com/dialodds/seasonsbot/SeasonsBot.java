package com.dialodds.seasonsbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;

@Component
public class SeasonsBot {

    @Value("${discord.bot.token}")
    private String token;

    private final CommandHandler commandHandler;

    @Autowired
    public SeasonsBot(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void start() throws Exception {
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("NFL Betting Seasons"))
                .addEventListeners(commandHandler)
                .build();

        jda.awaitReady();
        System.out.println("SeasonsBot is ready!");
    }
}