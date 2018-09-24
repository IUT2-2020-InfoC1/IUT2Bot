package com.jesus_crie.iut2_bot;

import com.jesus_crie.iut2_bot.command.*;
import com.jesus_crie.iut2_bot.timetable.TimetableModule;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.utils.IStateProvider;
import com.jesus_crie.modularbot_command.CommandModule;
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
                return i -> Game.listening("Use +help");
            }

            @Override
            public IntFunction<OnlineStatus> getOnlineStatusProvider() {
                return i -> OnlineStatus.ONLINE;
            }
        });


        final ModularBot bot = builder.build();

        final TimetableModule timetableModule = bot.getModuleManager().getModule(TimetableModule.class);
        assert timetableModule != null; // Don't look at me like this.

        final CommandModule cmdModule = bot.getModuleManager().getModule(CommandModule.class);
        assert cmdModule != null; // No comment plz, i known.

        cmdModule.registerCommands(
                new HelpCommand(),
                new LinksCommand(),
                new GroupCommand(),
                new TimetableCommand(timetableModule),

                new StopCommand(),
                new TestCommand()
        );

        cmdModule.registerQuickCommand("jesus_crie", e -> e.fastReply("...STEEEEEEEEEEUH !!!!!!!!!!!!!!"));

        cmdModule.addListener(new CommandListener());

        try {
            bot.login();
        } catch (LoginException e) {
            System.err.println("An error occurred while logging in ! " + e);
        }
    }
}
