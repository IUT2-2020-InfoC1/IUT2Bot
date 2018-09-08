package com.jesus_crie.iut2_bot.timetable;

import com.jesus_crie.modularbot.module.BaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimetableModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TimetableModule");

    private static final ModuleInfo INFO = new ModuleInfo("Timetable", "Jesus_Crie", "", "1.0", 1);

    public TimetableModule() {
        super(INFO);
    }
}
