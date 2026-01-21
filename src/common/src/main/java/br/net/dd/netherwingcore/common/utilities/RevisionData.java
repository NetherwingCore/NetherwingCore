package br.net.dd.netherwingcore.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The {@code RevisionData} class provides a utility for retrieving various metadata
 * about the software, including version information, commit details, and database revisions.
 * This information is loaded from a properties file, typically named {@code revision_data.properties},
 * which is expected to be available in the classpath.
 * <p>
 * Example use cases include displaying version information in "About" dialogs,
 * embedding software revision details in logs, and tracking database schema versions.
 * </p>
 */
public class RevisionData {

    // Holds properties loaded from '/revision_data.properties'
    private static final Properties props = new Properties();

    static {
        try (InputStream in = RevisionData.class.getResourceAsStream("/revision_data.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load revision data.", e);
        }
    }

    /**
     * @return the Git commit hash of the current build.
     */
    public static String getCommitHash() {
        return props.getProperty("git.commit.hash");
    }

    /**
     * @return the date of the Git commit for the current build.
     */
    public static String getCommitDate() {
        return props.getProperty("git.commit.date");
    }

    /**
     * @return the name of the Git branch used for the current build.
     */
    public static String getGitCommitBranch() {
        return props.getProperty("git.commit.branch");
    }

    /**
     * @return the full database revision identifier.
     */
    public static String getFullDatabaseRevision() {
        return props.getProperty("database.full_database");
    }

    /**
     * @return the database revision identifier for hotfixes.
     */
    public static String getHotfixDatabaseRevision() {
        return props.getProperty("database.hotfixes_database");
    }

    /**
     * @return the name of the company responsible for this software.
     */
    public static String getCompanyName() {
        return props.getProperty("company.name");
    }

    /**
     * @return the legal copyright notice for the software.
     */
    public static String getLegalCopyright() {
        return props.getProperty("legal.copyright");
    }

    /**
     * @return the version of the file associated with this software build.
     */
    public static String getFileVersion() {
        return props.getProperty("file.version");
    }

    /**
     * @return the full version string for the product, including the product name,
     *         version, platform, and linkage type.
     */
    public static String getFullVersion() {
        return getProductName() + " rev. " + getFileVersion()
                + " (" + detectPlatform() + ", " + detectLinkageType() + ")";
    }

    /**
     * @return the name of the product.
     */
    public static String getProductName() {
        return props.getProperty("product.name");
    }

    /**
     * @return the description of the product.
     */
    public static String getProductDescription() {
        return props.getProperty("product.description");
    }

    /**
     * Detects the underlying operating system and determines the most suitable
     * human-readable platform name.
     *
     * @return a string representation of the platform, such as "Win64", "MacOSX",
     *         "Unix", or "Unknown" if the detection fails.
     */
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

    /**
     * Detects the type of linkage used for the application. In Java,
     * this is usually "Dynamic" because of the JVM's runtime linkage model.
     *
     * @return the linkage type.
     */
    private static String detectLinkageType() {
        return "Dynamic";
    }

}
