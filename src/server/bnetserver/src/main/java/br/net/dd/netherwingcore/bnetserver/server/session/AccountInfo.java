package br.net.dd.netherwingcore.bnetserver.server.session;

import java.util.HashMap;
import java.util.Map;

public class AccountInfo {
    public Integer id;
    public String login;
    public boolean isLockedToIP;
    public String lockCountry;
    public String lastIP;
    public long loginTicketExpiry;
    public boolean isBanned;
    public boolean isPermanentlyBanned;

    public Map<Integer, GameAccountInfo> gameAccounts = new HashMap<>();

    public void loadResult(Object preparedQueryResult) {
        // Implementation to load data from a hypothetical query result
    }
}
