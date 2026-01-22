package br.net.dd.netherwingcore.database;

public record Statement(String name, String query, MySQLConnection.ConnectionFlags connectionFlag) {
}
