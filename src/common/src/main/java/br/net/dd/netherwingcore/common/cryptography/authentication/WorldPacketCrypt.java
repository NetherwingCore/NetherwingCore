package br.net.dd.netherwingcore.common.cryptography.authentication;

import br.net.dd.netherwingcore.common.cryptography.AES;

public class WorldPacketCrypt {

    private AES clientDecrypt;
    private AES serverEncrypt;

    private Integer clientCounter;
    private Integer serverCounter;
    private boolean initialized;

    public WorldPacketCrypt() throws Exception {
        clientDecrypt = new AES(false, 256);
        serverEncrypt = new AES(true, 256);
        clientCounter = 0;
        serverCounter = 0;
        initialized = false;
    }

    // TODO: Implement initialization with session key

}
