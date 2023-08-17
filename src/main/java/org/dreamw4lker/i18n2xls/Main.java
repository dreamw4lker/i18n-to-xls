package org.dreamw4lker.i18n2xls;

import lombok.extern.slf4j.Slf4j;
import org.dreamw4lker.i18n2xls.domain.Mode;
import org.dreamw4lker.i18n2xls.service.JsonToXlsService;
import org.dreamw4lker.i18n2xls.service.XlsToJsonService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Основной класс утилиты переноса переводов JSON -> XLS и XLS -> JSON
 *
 * @author Alexander Shkirkov
 */
@Slf4j
public class Main {

    private static Mode workMode;

    private static final String FALLBACK_PASSWORD = "12345678";
    private static final String FALLBACK_XLS_FILE_NAME = "translation_template.xls";

    /**
     * Чтение аргументов из строки запуска
     * @param args аргументы
     */
    private static Map<String, String> readArgs(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (String arg : args) {
            String[] keyValue = arg.split("=");
            arguments.put(keyValue[0], keyValue[1]);
        }
        if (!arguments.containsKey("password")) {
            log.warn("Argument 'password' was not found. Process will use fallback value");
            arguments.put("password", FALLBACK_PASSWORD);
        }
        if (!arguments.containsKey("resultFileName")) {
            log.warn("Argument 'resultFileName' was not found. Process will use fallback value '{}'", FALLBACK_XLS_FILE_NAME);
            arguments.put("resultFileName", FALLBACK_XLS_FILE_NAME);
        }
        return arguments;
    }

    /**
     * Проверка наличия необходимых аргументов для режима запуска
     * @param args key-value map аргументов
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

    public static void main(String[] args) {
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
