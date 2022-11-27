package org.dreamw4lker.transylvania;

import org.dreamw4lker.transylvania.domain.Mode;
import org.dreamw4lker.transylvania.service.JsonToXlsService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Mode workMode = null;
        String workPath = null;
        String langFrom = null;
        String langTo = null;
        for (String arg : args) {
            if (arg.startsWith("mode=")) {
                workMode = Mode.fromString(arg.split("mode=")[1]);
            } else if (arg.startsWith("path=")) {
                workPath = arg.split("path=")[1];
            } else if (arg.startsWith("from=")) {
                langFrom = arg.split("from=")[1];
            } else if (arg.startsWith("to=")) {
                langTo = arg.split("to=")[1];
            }
        }

        if (workMode == Mode.JSON_TO_XLS) {
            new JsonToXlsService(workPath, langFrom, langTo).createXls();
        }
    }
}
