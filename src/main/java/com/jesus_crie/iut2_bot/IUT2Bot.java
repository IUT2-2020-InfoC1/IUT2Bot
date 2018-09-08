package com.jesus_crie.iut2_bot;

import com.jesus_crie.iut2_bot.command.*;
import com.jesus_crie.iut2_bot.timetable.TimetableModule;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.utils.IStateProvider;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import javax.security.auth.login.LoginException;
import java.util.function.IntFunction;

public class IUT2Bot {

    public static void main(String[] args) {
        final ModularBotBuilder builder = new ModularBotBuilder(args[0])
                .autoLoadBaseModules()
                .registerModules(
                        new TimetableModule()
                );

        builder.setStateProvider(new IStateProvider() {
            @Override
            public IntFunction<Boolean> getIdleProvider() {
                return i -> false;
            }

            @Override
            public IntFunction<Game> getGameProvider() {
                return i -> Game.watching("over you. Use +help");
            }

            @Override
            public IntFunction<OnlineStatus> getOnlineStatusProvider() {
                return i -> OnlineStatus.ONLINE;
            }
        });

        final ModularBot bot = builder.build();

        final CommandModule cmdModule = bot.getModuleManager().getModule(CommandModule.class);
        assert cmdModule != null; // No comment plz, i known.

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
