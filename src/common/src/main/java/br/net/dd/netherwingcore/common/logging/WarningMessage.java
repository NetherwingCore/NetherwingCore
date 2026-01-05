package br.net.dd.netherwingcore.common.logging;

public class WarningMessage extends Message {
    Level level;
    public WarningMessage(String message) {
        super(message);
        level = Level.WARNING;
    }
}
