package com.jesus_crie.iut2_bot.command;

import com.jesus_crie.iut2_bot.timetable.TimetableModule;
import com.jesus_crie.modularbot_command.AccessLevel;
import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;

import javax.annotation.Nonnull;

@CommandInfo(
        name = {"test"}
)
public class TestCommand extends Command {

    public TestCommand() {
        super(AccessLevel.CREATOR);
    }

    @RegisterPattern
    public void exec(@Nonnull final CommandEvent event) {
        final TimetableModule module = event.getModule().getBot().getModuleManager().getModule(TimetableModule.class);
        assert module != null; // F u

        event.getModule().getBot().getMainPool().execute(() -> {
            module.readPage(TimetableModule.RESOURCE_C1);
            event.fastReply("ok");
        });
    }
}
