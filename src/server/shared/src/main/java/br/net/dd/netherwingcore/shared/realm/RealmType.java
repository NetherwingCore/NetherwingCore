package br.net.dd.netherwingcore.shared.realm;

public enum RealmType {

    REALM_TYPE_NORMAL(0),
    REALM_TYPE_PVP(1),
    REALM_TYPE_NORMAL2(4),
    REALM_TYPE_RP(6),
    REALM_TYPE_RPPVP(8),

    MAX_CLIENT_REALM_TYPE(14),

    REALM_TYPE_FFA_PVP(16);

    private final int value;

    RealmType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
