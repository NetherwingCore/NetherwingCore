package br.net.dd.netherwingcore.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RevisionData {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = RevisionData.class.getResourceAsStream("/revision_data.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load revision data.", e);
        }
    }

    public static String getCommitHash() {
        return props.getProperty("git.commit.hash");
    }

    public static String getCommitDate() {
        return props.getProperty("git.commit.date");
    }

    public static String getGitCommitBranch() {
        return props.getProperty("git.commit.branch");
    }

    public static String getFullDatabaseRevision() {
        return props.getProperty("database.full_database");
    }

    public static String getHotfixDatabaseRevision() {
        return props.getProperty("database.hotfixes_database");
    }

    public static String getCompanyName() {
        return props.getProperty("company.name");
    }

    public static String getLegalCopyright() {
        return props.getProperty("legal.copyright");
    }

    public static String getFileVersion() {
        return props.getProperty("file.version");
    }

    public static String getFullVersion() {
        return getProductName() + " rev. " + getFileVersion()
                + " (" + detectPlatform() + ", " + detectLinkageType() + ")";
    }

    public static String getProductName() {
        return props.getProperty("product.name");
    }

    public static String getProductDescription() {
        return props.getProperty("product.description");
    }

    // --- Platform detection helper ---
    private static String detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return System.getProperty("os.arch").contains("64") ? "Win64" : "Win32";
        } else if (os.contains("mac")) {
            return "MacOSX";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "Unix";
        }
        return "Unknown";
    }

    // --- Linkage detection helper ---
    private static String detectLinkageType() {
        // In Java, linkage type is usually 'Dynamic', but you can customize this if needed.
        return "Dynamic";
    }

}
