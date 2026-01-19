package br.net.dd.netherwingcore.database;

import br.net.dd.netherwingcore.database.impl.auth.StatementName;

import java.util.HashMap;
import java.util.Map;

public abstract class Database {

    private final Map<StatementName, StatementValue> statements;

    protected Database() {
        this.statements = new HashMap<>();
        loadStatements();
    }

    protected void prepareStatement(StatementName name, String query, ConnectionFlag connectionFlag){
        this.statements.put(name, new StatementValue(query, connectionFlag));
    };

    public StatementValue get(StatementName name) {
        return this.statements.get(name);
    }

    public abstract void loadStatements();

}
