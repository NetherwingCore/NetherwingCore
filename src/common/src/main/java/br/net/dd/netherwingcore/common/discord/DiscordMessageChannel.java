package br.net.dd.netherwingcore.common.discord;

public enum DiscordMessageChannel {
    DISCORD_WORLD_A(1),
    DISCORD_WORLD_H(2),
    DISCORD_TICKET(3);

    private final int value;

    DiscordMessageChannel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
