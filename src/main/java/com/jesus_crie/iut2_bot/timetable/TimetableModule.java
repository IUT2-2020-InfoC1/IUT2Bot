package com.jesus_crie.iut2_bot.timetable;

import com.jesus_crie.modularbot.module.BaseModule;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TimetableModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TimetableModule");

    private static final ModuleInfo INFO = new ModuleInfo("Timetable", "Jesus_Crie", "", "1.0", 1);

    private static final String URL = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/direct_planning.jsp" +
            "?resources=$RESOURCE$" +
            "&weeks=1&showTree=false&showPianoDays=true&login=WebINFO&password=MPINFO&projectId=11&displayConfName=Vue_Web_INFO_Etudiant" +
            "&showOptions=false&showPianoWeeks=true&days=0,1,2,3,4,5&hash=5ac8ee5ea848397822695359c5188641";

    public static final int RESOURCE_A1 = 457;
    public static final int RESOURCE_A2 = 458;
    public static final int RESOURCE_B1 = 459;
    public static final int RESOURCE_B2 = 460;
    public static final int RESOURCE_C1 = 461;
    public static final int RESOURCE_C2 = 462;
    public static final int RESOURCE_D1 = 463;
    public static final int RESOURCE_D2 = 464;

    public TimetableModule() {
        super(INFO);
    }

    public void readPage(final int resource) {
        try {
            final Document mainDoc = Jsoup.connect(URL.replace("$RESOURCE$", String.valueOf(resource))).get();
            final Document tableDoc = Jsoup.connect(mainDoc.selectFirst("frame[name=et]").absUrl("src")).get();
            LOG.info(tableDoc.outerHtml());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
