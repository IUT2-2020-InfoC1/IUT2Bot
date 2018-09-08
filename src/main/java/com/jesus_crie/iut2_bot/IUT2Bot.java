package com.jesus_crie.iut2_bot;

import com.jesus_crie.iut2_bot.command.*;
import com.jesus_crie.iut2_bot.timetable.TimetableModule;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot_command.CommandModule;

import javax.security.auth.login.LoginException;

public class IUT2Bot {

    public static void main(String[] args) {
        final ModularBotBuilder builder = new ModularBotBuilder(args[0])
                .autoLoadBaseModules()
                .registerModules(
                        new TimetableModule()
                );

        final ModularBot bot = builder.build();

        final CommandModule cmdModule = bot.getModuleManager().getModule(CommandModule.class);
        assert cmdModule != null; // No comment plz, i known.

        cmdModule.setCreatorId(182547138729869314L);
        cmdModule.addListener(new CommandListener());

        cmdModule.registerCommands(
                new HelpCommand(),
                new LinksCommand(),
                new GroupCommand(),

                new StopCommand(),
                new TestCommand()
        );


        try {
            bot.login();
        } catch (LoginException e) {
            System.err.println("An error occurred while logging in ! " + e);
        }
    }
}
