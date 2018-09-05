package com.jesus_crie.iut2_bot;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot_command.CommandModule;

import javax.security.auth.login.LoginException;

public class IUT2Bot {

    public static void main(String[] args) {
        final ModularBotBuilder builder = new ModularBotBuilder(args[0])
                .autoLoadBaseModules();

        final ModularBot bot = builder.build();

        final CommandModule cmdModule = bot.getModuleManager().getModule(CommandModule.class);
        assert cmdModule != null; // No comment plz, i known.

        cmdModule.setCreatorId(182547138729869314L);

        cmdModule.registerCreatorQuickCommand("stop", e -> {
            e.fastReply("Shutting down...");
            bot.shutdown();
        });


        try {
            bot.login();
        } catch (LoginException e) {
            System.err.println("An error occurred while logging in ! " + e);
        }
    }
}
