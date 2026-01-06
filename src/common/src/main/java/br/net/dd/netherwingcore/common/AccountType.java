package br.net.dd.netherwingcore.common;

public enum AccountType {

    SEC_PLAYER(0),
    SEC_MODERATOR(1),
    SEC_GAMEMASTER(2),
    SEC_ADMINISTRATOR(3),
    SEC_CONSOLE(4);

    private final int value;

    AccountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
