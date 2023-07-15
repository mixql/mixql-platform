package org.mixql.platform.demo.utils;

import org.beryx.textio.TextTerminal;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;

public class TerminalOps {

    public static boolean MultiLineMode = false;
    public static String MultiLineString = "";
    public static boolean registerAbort(TextTerminal<?> terminal, String keyStrokeAbort){
        return terminal.registerHandler(keyStrokeAbort,
                t -> new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT)
                        .withPayload(System.getProperty("user.name", "nobody")));
    }
    public static boolean registerReboot(TextTerminal<?> terminal, String keyStrokeReboot){
        return terminal.registerHandler(keyStrokeReboot, t -> {
            MultiLineMode = true;
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }

    public static boolean registerAutoValue(TextTerminal<?> terminal, String keyStrokeAutoValue){
        return terminal.registerHandler(keyStrokeAutoValue, t -> {
            terminal.println();
            MultiLineMode = false;
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RETURN)
                    .withReturnValueProvider(partialInput -> partialInput.isEmpty() ? ";" : partialInput);
        });
    }
}
