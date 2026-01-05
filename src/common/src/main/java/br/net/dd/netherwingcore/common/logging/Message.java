package br.net.dd.netherwingcore.common.logging;

public class Message extends Exception implements Detail{
    Level level;
    public Message(String message) {
        super(message);
    }

    public Level getLevel() {
        return level;
    }
}
