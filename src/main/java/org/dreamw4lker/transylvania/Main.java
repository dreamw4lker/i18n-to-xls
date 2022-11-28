package org.dreamw4lker.transylvania;

import lombok.extern.slf4j.Slf4j;
import org.dreamw4lker.transylvania.domain.Mode;
import org.dreamw4lker.transylvania.service.JsonToXlsService;
import org.dreamw4lker.transylvania.service.XlsToJsonService;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) throws IOException {
        log.info("Process started with args: {}", String.join(", ", args));
        Mode workMode = null;
        String workPath = null;
        String langFrom = null;
        String langTo = null;
        String xls = null;
        for (String arg : args) {
            if (arg.startsWith("mode=")) {
                workMode = Mode.fromString(arg.split("mode=")[1]);
            } else if (arg.startsWith("path=")) {
                workPath = arg.split("path=")[1];
            } else if (arg.startsWith("from=")) {
                langFrom = arg.split("from=")[1];
            } else if (arg.startsWith("to=")) {
                langTo = arg.split("to=")[1];
            } else if (arg.startsWith("xls=")) {
                xls = arg.split("xls=")[1];
            }
        }

        if (workMode == Mode.JSON_TO_XLS) {
            new JsonToXlsService(workPath, langFrom, langTo).createXls();
        } else if (workMode == Mode.XLS_TO_JSON) {
            new XlsToJsonService(workPath, xls, langTo).createJsonFiles();
        }
        log.info("Process finished");
    }
}
