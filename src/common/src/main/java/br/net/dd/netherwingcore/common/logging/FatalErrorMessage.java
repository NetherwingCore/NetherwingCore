package br.net.dd.netherwingcore.common.logging;

public class FatalErrorMessage extends Message {
    Level level;
    public FatalErrorMessage(String message) {
        super(message);
        level = Level.FATAL_ERROR;
    }
}
