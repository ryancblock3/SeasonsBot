package com.dialodds.seasonsbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JDAInitializer {

    private final CommandHandler commandHandler;

    public JDAInitializer(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void initJDA() throws Exception {
        String token = System.getProperty("DISCORD_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("DISCORD_BOT_TOKEN is not set");
        }

        JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .setLargeThreshold(50)
            .addEventListeners(commandHandler)
            .build();
    }
}