package br.net.dd.netherwingcore.shared.realm;

public class RealmBuildInfo {

    public static int build;
    public static int majorVersion;
    public static int minorVersion;
    public static int bugfixVersion;
    public static char[] hotfixVersion = new char[4];
    public static byte[] winAuthSeed = new byte[16];
    public static byte[] win64AuthSeed = new byte[16];
    public static byte[] mac64AuthSeed = new byte[16];

}
