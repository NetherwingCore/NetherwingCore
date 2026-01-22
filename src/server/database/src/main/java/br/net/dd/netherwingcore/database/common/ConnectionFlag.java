package br.net.dd.netherwingcore.database.common;

/**
 * Flags used to specify the type of connection for database operations.
 */
public enum ConnectionFlag {

    CONNECTION_ASYNC(0x1),
    CONNECTION_SYNC(0x2),
    CONNECTION_BOTH(CONNECTION_ASYNC.flag | CONNECTION_SYNC.flag);

    final int flag;

    ConnectionFlag(int flag) {
        this.flag = flag;
    }

}
