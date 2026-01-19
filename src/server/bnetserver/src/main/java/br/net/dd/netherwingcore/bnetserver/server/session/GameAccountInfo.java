package br.net.dd.netherwingcore.bnetserver.server.session;

import java.util.HashMap;
import java.util.Map;

public class GameAccountInfo {
    public Integer id;
    public String name;
    public String displayName;
    public long unbanDate;
    public boolean isBanned;
    public boolean isPermanentlyBanned;
    public String securityLevel;

    public Map<Integer, Byte> characterCounts = new HashMap<>();
    public Map<String, LastPlayedCharacterInfo> lastPlayedCharacters = new HashMap<>();

    public void loadResult(Object... fields) {
        // Implementation to load data from the fields object,
        // equivalent to working with ResultSet in Java JDBC.
    }
}
