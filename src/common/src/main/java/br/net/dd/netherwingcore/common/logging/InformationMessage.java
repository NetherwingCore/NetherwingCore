package br.net.dd.netherwingcore.common.logging;

public class InformationMessage extends Message {
    Level level;
    public InformationMessage(String message) {
        super(message);
        level = Level.INFORMATION;
    }
}
