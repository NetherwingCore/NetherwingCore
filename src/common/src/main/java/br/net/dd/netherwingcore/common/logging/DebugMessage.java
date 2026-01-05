package br.net.dd.netherwingcore.common.logging;

public class DebugMessage extends Message {
    public DebugMessage(String message) {
        super(message);
        level = Level.DEBUG;
    }
}
