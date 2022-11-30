package org.dreamw4lker.transylvania;

import lombok.extern.slf4j.Slf4j;
import org.dreamw4lker.transylvania.domain.Mode;
import org.dreamw4lker.transylvania.service.JsonToXlsService;
import org.dreamw4lker.transylvania.service.XlsToJsonService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private static Mode workMode;

    /**
     * Reads arguments from command prompt
     */
    private static Map<String, String> readArgs(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (String arg : args) {
            String[] keyValue = arg.split("=");
            arguments.put(keyValue[0], keyValue[1]);
        }
        return arguments;
    }

    /**
     * Checks required arguments for specific mode
     */
    private static boolean checkArgs(Map<String, String> args) {
        workMode = Mode.fromString(args.get("mode"));
        String[] requiredArgs;
        if (workMode == Mode.JSON_TO_XLS) {
            requiredArgs = new String[]{"path", "from", "to"};
        } else if (workMode == Mode.XLS_TO_JSON) {
            requiredArgs = new String[]{"path", "to", "xls"};
        } else {
            log.error("Unknown 'mode' argument value: {}. Should be one of: {}",
                    args.get("mode"),
                    Arrays.stream(Mode.values()).map(Mode::getText).collect(Collectors.toList())
            );
            return false;
        }

        return Arrays.stream(requiredArgs).allMatch(key -> {
            boolean result = args.containsKey(key);
            if (!result) {
                log.error("Please, specify '{}' argument", key);
            }
            return result;
        });
    }

    public static void main(String[] args) throws IOException {
        log.info("Process started with args: {}", String.join(", ", args));

        Map<String, String> arguments = readArgs(args);
        if (checkArgs(arguments)) {
            if (workMode == Mode.JSON_TO_XLS) {
                new JsonToXlsService(arguments).createXls();
            } else if (workMode == Mode.XLS_TO_JSON) {
                new XlsToJsonService(arguments).createJsonFiles();
            }
        }

        log.info("Process finished");
    }
}
