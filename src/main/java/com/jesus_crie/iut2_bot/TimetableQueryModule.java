package com.jesus_crie.iut2_bot;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.module.BaseModule;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.Locale;

public class TimetableQueryModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("TimetableQueryModule");

    private static final ModuleInfo INFO = new ModuleInfo("Timetable Query", "Jesus_Crie", null, "1.0", 1);

    private static final String URL_INIT = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/direct_planning.jsp?resources=461&weeks=&showTree=true&showPianoDays=true&login=WebINFO&password=MPINFO&projectId=11&displayConfName=Vue_Web_INFO_Etudiant&showOptions=true&showPianoWeeks=true&days=0,1,2,3,4,5";
    private static final String URL_SELECT = "http://www-ade.iut2.upmf-grenoble.fr/ade/standard/gui/tree.jsp?selectBranchId=447&reset=false&forceLoad=false&scroll=0";
    private static final String URL_SETTINGS = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/direct_planning.jsp?keepSelection&showTree=true";
    private static final String URL_FINAL = "http://www-ade.iut2.upmf-grenoble.fr/ade/custom/modules/plannings/info.jsp?order=slot";

    private static final RequestBody FORM_DATA = RequestBody.create(MediaType.parse("application/x-www-form-encoded"),
            "showTab=true&showTabActivity=true&showTabDay=true&showTabDate=true&showTabHour=true&showTabDuration=true&aCx=true&displayConfId=48&showPianoWeeks=true&showPianoDays=true&display=true&x=&y=&isClickable=true&changeOptions=true&displayType=0&showLoad=false&showTabTrainees=true&sC=false&sTy=false&sUrl=false&sE=false&sM=false&sJ=false&sA1=false&sA2=false&sZp=false&sCi=false&sSt=false&sCt=false&sT=false&sF=false&sCx=false&sCy=false&sCz=false&sTz=false&showTabInstructors=true&iC=false&iTy=false&iUrl=false&iE=false&iM=false&iJ=false&iA1=false&iA2=false&iZp=false&iCi=false&iSt=false&iCt=false&iT=false&iF=false&iCx=false&iCy=false&iCz=false&iTz=false&showTabRooms=true&roC=false&roTy=false&roUrl=false&roE=false&roM=false&roJ=false&roA1=false&roA2=false&roZp=false&roCi=false&roSt=false&roCt=false&roT=false&roF=false&roCx=false&roCy=false&roCz=false&roTz=false&showTabResources=true&reC=false&reTy=false&reUrl=false&reE=false&reM=false&reJ=false&reA1=false&reA2=false&reZp=false&reCi=false&reSt=false&reCt=false&reT=false&reF=false&reCx=false&reCy=false&reCz=false&reTz=false&showTabCategory5=true&c5C=false&c5Ty=false&c5Url=false&c5E=false&c5M=false&c5J=false&c5A1=false&c5A2=false&c5Zp=false&c5Ci=false&c5St=false&c5Ct=false&c5T=false&c5F=false&c5Cx=false&c5Cy=false&c5Cz=false&c5Tz=false&showTabCategory6=true&c6C=false&c6Ty=false&c6Url=false&c6E=false&c6M=false&c6J=false&c6A1=false&c6A2=false&c6Zp=false&c6Ci=false&c6St=false&c6Ct=false&c6T=false&c6F=false&c6Cx=false&c6Cy=false&c6Cz=false&c6Tz=false&showTabCategory7=true&c7C=false&c7Ty=false&c7Url=false&c7E=false&c7M=false&c7J=false&c7A1=false&c7A2=false&c7Zp=false&c7Ci=false&c7St=false&c7Ct=false&c7T=false&c7F=false&c7Cx=false&c7Cy=false&c7Cz=false&c7Tz=false&showTabCategory8=true&c8C=false&c8Ty=false&c8Url=false&c8E=false&c8M=false&c8J=false&c8A1=false&c8A2=false&c8Zp=false&c8Ci=false&c8St=false&c8Ct=false&c8T=false&c8F=false&c8Cx=false&c8Cy=false&c8Cz=false&c8Tz=false");

    private final OkHttpClient client = new OkHttpClient.Builder().build();

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE);

    public TimetableQueryModule() {
        super(INFO);
    }

    public void queryAndSaveCurrentWeek() throws IOException {
        final Request requestInit = new Request.Builder()
                .url(URL_INIT)
                .get()
                .build();

        Response response = client.newCall(requestInit).execute();

        checkResponse(response);

        final String cookie = response.header("Set-Cookie");

        if (cookie != null)
            throw new IllegalArgumentException("No cookies found !");

        final Request requestSelect = new Request.Builder()
                .url(URL_SELECT)
                .get()
                .header("Cookie", cookie)
                .build();
        final Request requestChangeSettings = new Request.Builder()
                .url(URL_SETTINGS)
                .post(FORM_DATA)
                .header("Cookie", cookie)
                .build();
        final Request requestFinal = new Request.Builder()
                .url(URL_FINAL)
                .get()
                .header("Cookie", cookie)
                .build();

        response = client.newCall(requestSelect).execute();
        checkResponse(response);

        response = client.newCall(requestChangeSettings).execute();
        checkResponse(response);

        response = client.newCall(requestFinal).execute();
        checkResponse(response);

        LOG.info("Successfully queried the timetable !");

        parseAndSaveBodyToJson(response.body().string());
    }

    private void checkResponse(final Response res) {
        if (res.code() != 200)
            throw new IllegalStateException("Received non-valid status code " + res.code() + " !");
    }

    private void parseAndSaveBodyToJson(final String body) {
        final Config out = Config.inMemory();

        final Document document = Jsoup.parse(body);
        final Element tableBody = document.selectFirst("body > table > tbody");
        tableBody.childNodes().stream()
                .filter(node -> node.hasAttr("class"))
                .forEachOrdered(node -> {
                    final Config cl = Config.inMemory();

                    final String date = ((TextNode) node.childNode(0).childNode(0)).text();
                    final long timestamp = DATE_FORMATTER.parse(date, Instant::from).getEpochSecond();

                    cl.set("date", timestamp);
                    cl.set("class_node", ((TextNode) node.childNode(1).childNode(0)).text());
                    cl.set("day_name", ((TextNode) node.childNode(2)).text());
                    cl.set("hour", ((TextNode) node.childNode(3)).text());
                    // TODO 9/28/18 other fields
                });
    }
}
