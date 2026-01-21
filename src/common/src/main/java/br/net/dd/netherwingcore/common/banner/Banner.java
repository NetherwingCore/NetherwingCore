package br.net.dd.netherwingcore.common.banner;

import br.net.dd.netherwingcore.common.utilities.RevisionData;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * The {@code Banner} class is responsible for displaying a banner message with relevant information
 * and graphical decorations in the logs whenever an application or service is started.
 *
 * <p>This class outputs:
 * <ul>
 *   <li>The current version of the application or service, retrieved from {@code RevisionData}.</li>
 *   <li>A graphical banner artwork.</li>
 *   <li>A message including the service name and additional logging details.</li>
 * </ul>
 *
 * <p>This banner serves as a visual cue indicating that the service has initialized successfully
 * while also providing immediate access to version and logging details.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Banner.show("MyService", "logs/service.log", "Running in production mode");
 * }</pre>
 * This will output the banner in the logs using the provided service name and extra log information.
 *
 * <h3>Dependencies:</h3>
 * This class depends on:
 * <ul>
 *   <li>{@code RevisionData} for versioning information.</li>
 *   <li>{@code Log} for logging the banner output.</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * This class is not instantiable, and the method {@code show} is static. It is thread-safe
 * as it only writes to the logs using static methods.
 *
 * <h3>Limitations:</h3>
 * <ul>
 *   <li>The banner artwork is hardcoded and not customizable through parameters.</li>
 *   <li>No internationalization (i18n) support is provided for the logged messages.</li>
 * </ul>
 *
 * @author NetherwingCore Team
 * @version 1.0
 * @since 1.0
 */
public class Banner {

    /**
     * Displays the banner message with the application version, service name, and additional
     * information about logging.
     *
     * <p>The method logs the following in order:
     * <ul>
     *   <li>The full version of the application alongside the service name.</li>
     *   <li>A message indicating how to stop the application (`Ctrl-C`).</li>
     *   <li>A graphical ASCII representation of the banner.</li>
     *   <li>The GitHub repository URL for the project.</li>
     * </ul>
     *
     * @param serviceName  the name of the service to display in the banner.
     * @param logFileName  the name of the log file (currently unused in the method).
     * @param logExtraInfo additional information about logging (currently unused in the method).
     */
    public static void show(String serviceName, String logFileName, String logExtraInfo){
        log(RevisionData.getFullVersion() + " (" + serviceName + ")");
        log("(<Ctrl-C> to stop.)");
        log("");
        log(" __  __          __    __                                                         ");
        log("/\\ \\/\\ \\        /\\ \\__/\\ \\                                 __                     ");
        log("\\ \\ `\\\\ \\     __\\ \\ ,_\\ \\ \\___      __   _ __   __  __  __/\\_\\    ___      __     ");
        log(" \\ \\ , ` \\  /'__`\\ \\ \\/\\ \\  _ `\\  /'__`\\/\\`'__\\/\\ \\/\\ \\/\\ \\/\\ \\ /' _ `\\  /'_ `\\   ");
        log("  \\ \\ \\`\\ \\/\\  __/\\ \\ \\_\\ \\ \\ \\ \\/\\  __/\\ \\ \\/ \\ \\ \\_/ \\_/ \\ \\ \\/\\ \\/\\ \\/\\ \\L\\ \\  ");
        log("   \\ \\_\\ \\_\\ \\____\\\\ \\__\\\\ \\_\\ \\_\\ \\____\\\\ \\_\\  \\ \\___x___/'\\ \\_\\ \\_\\ \\_\\ \\____ \\ ");
        log("    \\/_/\\/_/\\/____/ \\/__/ \\/_/\\/_/\\/____/ \\/_/   \\/__//__/   \\/_/\\/_/\\/_/\\/___L\\ \\");
        log("                                                                           /\\____/");
        log("    https://github.com/NetherwingCore/NetherwingCore                CORE   \\_/__/ ");
        log(" ");
    }

}
