package br.net.dd.netherwingcore.common.logging;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Log {
    public static void log(String message) {

    }

    public static void log(Detail... details) {
        AtomicReference<Level> level = new AtomicReference<>(Level.INFORMATION);
        AtomicReference<String> message = new AtomicReference<>("No message provided.");
        List<Path> logFiles = new ArrayList<>();

        Arrays.stream(details).forEach(detail -> {
            if (detail instanceof Message){
                level.set(((Message) detail).getLevel());
                message.set(((Message) detail).getMessage());
            }
            if (detail instanceof LogFile){
                Arrays.stream(((LogFile) detail).paths()).iterator().forEachRemaining(logFiles::add);
            }
        });

        // Here would be the logic to actually log the message to the specified log files with the given level.
        System.out.println(message.get());

        logFiles.forEach(path -> {
            System.out.println("Logging to file: " + path.toString());
        });

    }
}
