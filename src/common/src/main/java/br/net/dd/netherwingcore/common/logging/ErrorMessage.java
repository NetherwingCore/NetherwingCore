package br.net.dd.netherwingcore.common.logging;

public class ErrorMessage extends Message {
    Level level;
    public ErrorMessage(String message) {
        super(message);
        level = Level.ERROR;
    }
}
