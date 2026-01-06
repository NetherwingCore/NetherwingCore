package br.net.dd.netherwingcore.common.discord;

public class DiscordMessage {

    private DiscordMessageChannel channel;
    private String message;
    private String characterName;
    private boolean isGm;

    public DiscordMessage(DiscordMessageChannel channel, String message, String characterName, boolean isGm) {
        this.channel = channel;
        this.message = message;
        this.characterName = characterName;
        this.isGm = isGm;
    }

    public DiscordMessageChannel getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public String getCharacterName() {
        return characterName;
    }

    public boolean isGm() {
        return isGm;
    }

}
