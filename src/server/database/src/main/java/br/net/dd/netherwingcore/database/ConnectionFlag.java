package br.net.dd.netherwingcore.database;

/**
 * Represents different modes of connection handling in the system.
 * This enum provides flags for asynchronous, synchronous, and combined connection modes.
 * Each mode is represented by an integer flag, enabling bitwise operations for configuration.
 */
public enum ConnectionFlag {
    /**
     * Asynchronous connection mode.
     * This flag represents the connections that are handled asynchronously, allowing non-blocking operations.
     */
    CONNECTION_ASYNC(0x1),

    /**
     * Synchronous connection mode.
     * This flag represents the connections that are handled synchronously, ensuring operations complete in a blocking manner.
     */
    CONNECTION_SYNC(0x2),

    /**
     * Combined connection mode.
     * This flag represents a combination of asynchronous and synchronous handling,
     * and is defined by performing a bitwise OR operation on CONNECTION_ASYNC and CONNECTION_SYNC flags.
     */
    CONNECTION_BOTH(CONNECTION_ASYNC.flag | CONNECTION_SYNC.flag);

    /**
     * The integer value of the flag.
     * This field stores the specific bitwise value representing the connection handling mode.
     */
    final int flag;

    /**
     * Constructor to associate an integer flag with a connection handling mode.
     *
     * @param i the integer value of the connection flag
     */
    ConnectionFlag(int i) {
        this.flag = i;
    }
}
