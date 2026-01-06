package br.net.dd.netherwingcore.shared.realm;

public enum RealmFlags {
    REALM_FLAG_NONE(0x00),
    REALM_FLAG_VERSION_MISMATCH(0x01),
    REALM_FLAG_OFFLINE(0x02),
    REALM_FLAG_SPECIFYBUILD(0x04),
    REALM_FLAG_UNK1(0x08),
    REALM_FLAG_UNK2(0x10),
    REALM_FLAG_RECOMMENDED(0x20),
    REALM_FLAG_NEW(0x40),
    REALM_FLAG_FULL(0x80);

    private final int value;

    RealmFlags(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
