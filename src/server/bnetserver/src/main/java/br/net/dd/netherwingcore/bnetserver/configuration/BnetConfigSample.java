package br.net.dd.netherwingcore.bnetserver.configuration;

import br.net.dd.netherwingcore.common.configuration.Configuration;
import br.net.dd.netherwingcore.common.configuration.ConfigurationSample;
import br.net.dd.netherwingcore.common.configuration.fields.*;
import br.net.dd.netherwingcore.common.configuration.structs.Group;
import br.net.dd.netherwingcore.common.configuration.structs.Item;
import br.net.dd.netherwingcore.common.configuration.structs.Section;

import static br.net.dd.netherwingcore.common.configuration.fields.Type.*;

/**
 * This class represents a sample configuration for the NetherwingCore Auth Server, providing a structured example of how to define
 * configuration options for the server. It extends {@link ConfigurationSample} and initializes a {@link Configuration} object
 * with various sections, groups, and items that illustrate the expected format and content of the configuration file.
 *
 * <p>The {@code BnetConfigSample} class serves as a template for creating the actual configuration file used by the auth server,
 * demonstrating how to organize settings and provide detailed descriptions, examples, and default values for each configuration option.</p>
 */
public class BnetConfigSample extends ConfigurationSample {

    /**
     * Constructs a new instance of {@code BnetConfigSample}, initializing the configuration with a sample structure and setting the file name for the configuration file.
     */
    public BnetConfigSample() {
        super();
        setConfiguration(createSample());
        setFileName("bnetserver.conf");
    }

    /**
     * Creates a sample {@link Configuration} object with predefined sections, groups, and items that illustrate the expected format and content of the configuration file.
     *
     * @return A {@link Configuration} instance representing the sample configuration for the NetherwingCore Auth Server.
     */
    public Configuration makeSample() {

        Configuration configuration = new Configuration(
                new Description("NetherwingCore Auth Server configuration file"),
                "bnetserver"
        );

        Section exampleSection = new Section(new Description("EXAMPLE CONFIG"));
        Group exampleGroup = new Group();
        Item exampleItem = new Item(
                new Description("Brief description what the variable is doing."),
                new Detail("Details on how this variable is used."),
                new Format("Expected formatting for the value of this variable."),
                new ImportantNote("Annotation for important things about this variable."),
                new Example("Example, i.e. if the value is a string"),
                new Key("# Variable"),
                new Value("0", NUMBER),
                new DefaultValue("10 - (Enabled|Comment|Variable name in case of grouped config options)", "0  - (Disabled|Comment|Variable name in case of grouped config options)"),
                new DeveloperNote("Copy this example to keep the formatting.", "Line breaks should be at column 100."),
                new Observations("Additional notes")
        );
        exampleGroup.addItem(exampleItem);
        exampleSection.addGroup(exampleGroup);
        configuration.addSection(exampleSection);

        return configuration;
    }

    /**
     * Creates a sample {@link Configuration} object with predefined sections, groups, and items that illustrate the expected format and content of the configuration file.
     *
     * @return A {@link Configuration} instance representing the sample configuration for the NetherwingCore Auth Server.
     */
    public Configuration createSample() {

        return new Configuration(new Description("NetherwingCore Auth Server configuration file"), "bnetserver")
                .addSection(new Section(new Description("EXAMPLE CONFIG"))
                        .addGroup(new Group()
                                .addItem(new Item(
                                                new Description("Brief description what the variable is doing."),
                                                new Detail("Details on how this variable is used."),
                                                new Format("Expected formatting for the value of this variable."),
                                                new ImportantNote("Annotation for important things about this variable."),
                                                new Example("Example, i.e. if the value is a string"),
                                                new Key("# Variable"),
                                                new Value("0", NUMBER),
                                                new DefaultValue("10 - (Enabled|Comment|Variable name in case of grouped config options)", "0  - (Disabled|Comment|Variable name in case of grouped config options)"),
                                                new DeveloperNote("Copy this example to keep the formatting.", "Line breaks should be at column 100."),
                                                new Observations("Additional notes")
                                        )
                                )
                        )
                ).addSection(new Section(new Description("AUTH SERVER SETTINGS"))
                        .addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Logs directory setting."),
                                        new ImportantNote("LogsDir needs to be quoted, as the string might contain space characters.",
                                                "Logs directory must exists, or log file creation will be disabled."),
                                        new Example("\"\" - (Log files will be stored in the current path)"),
                                        new Key("LogsDir"),
                                        new Value("\"\"", TEXT),
                                        new DefaultValue("\"\"")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Time (in minutes) between database pings."),
                                        new Key("MaxPingTime"),
                                        new Value("30", NUMBER),
                                        new DefaultValue("30")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("TCP port to reach the auth server for battle.net connections."),
                                        new Key("BattlenetPort"),
                                        new Value("1119", NUMBER),
                                        new DefaultValue("1119")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("TCP port to reach the REST login method."),
                                        new Key("LoginREST.Port"),
                                        new Value("8081", NUMBER),
                                        new DefaultValue("8081")
                                ))
                                .addItem(new Item(
                                        new Description("IP address sent to clients connecting from outside the network where bnetserver runs",
                                                "Set it to your external IP address"),
                                        new Key("LoginREST.ExternalAddress"),
                                        new Value("\"127.0.0.1\"", TEXT),
                                        new DefaultValue("\"127.0.0.1\"")
                                ))
                                .addItem(new Item(
                                        new Description("IP address sent to clients connecting from inside the network where bnetserver runs",
                                                "Set it to your local IP address (common 192.168.x.x network)",
                                                "or leave it at default value 127.0.0.1 if connecting directly to the internet without a router"),
                                        new Key("LoginREST.LocalAddress"),
                                        new Value("\"127.0.0.1\"", TEXT),
                                        new DefaultValue("\"127.0.0.1\"")
                                ))
                                .addItem(new Item(
                                        new Description("Determines how long the login ticket is valid (in seconds)",
                                                "When using client -launcherlogin feature it is recommended to set it to a high value (like a week)"),
                                        new Key("LoginREST.TicketDuration"),
                                        new Value("3600", NUMBER),
                                        new DefaultValue("3600")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Bind auth server to IP/hostname",
                                                "Using IPv6 address (such as \"::\") will enable both IPv4 and IPv6 connections"),
                                        new Key("BindIP"),
                                        new Value("\"0.0.0.0\"", TEXT),
                                        new DefaultValue("\"0.0.0.0\" - (Bind to all IPs on the system)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Certificates file. Both PEM (.crt) and PKCS#12 (.pfx) formats are supported"),
                                        new Example("\"/etc/ssl/certs/bnetserver.cert.pem\""),
                                        new Key("CertificatesFile"),
                                        new Value("\"./bnetserver.cert.pem\"", TEXT),
                                        new DefaultValue("\"./bnetserver.cert.pem\"")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Private key file."),
                                        new Example("\"/etc/ssl/private/bnetserver.key.pem\"",
                                                "Leave empty if you have a certificate in PKCS#12 format"),
                                        new Key("PrivateKeyFile"),
                                        new Value("\"./bnetserver.key.pem\"", TEXT),
                                        new DefaultValue("\"./bnetserver.key.pem\"")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Password used to encrypt private key."),
                                        new Key("PrivateKeyPassword"),
                                        new Value("\"\"", TEXT),
                                        new DefaultValue("\"\"")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Processors mask for Windows and Linux based multi-processor systems."),
                                        new Example("A computer with 2 CPUs:",
                                                "1 - 1st CPU only, 2 - 2nd CPU only, 3 - 1st and 2nd CPU, because 1 | 2 is 3"),
                                        new Key("UseProcessors"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0  - (Selected by OS)", "1+ - (Bit mask value of selected processors)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Process priority setting for Windows and Linux based systems."),
                                        new Detail("On Linux, a nice value of -15 is used. (requires superuser). On Windows, process is set to HIGH class."),
                                        new Key("ProcessPriority"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0 - (Normal)", "1 - (High)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Time (in seconds) between realm list updates."),
                                        new Key("RealmsStateUpdateDelay"),
                                        new Value("10", NUMBER),
                                        new DefaultValue("10", "0  - (Disabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Number of login attempts with wrong password before the account or IP will be banned."),
                                        new Key("WrongPass.MaxCount"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0  - (Disabled)", "1+ - (Enabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Time (in seconds) for banning account or IP for invalid login attempts."),
                                        new Key("WrongPass.BanTime"),
                                        new Value("600", NUMBER),
                                        new DefaultValue("600 - (10 minutes)", "0   - (Permanent ban)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Ban type for invalid login attempts."),
                                        new Key("WrongPass.BanType"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0 - (Ban IP)", "1 - (Ban Account)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Additionally log attempted wrong password logging"),
                                        new Key("WrongPass.Logging"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0 - (Disabled)", "1 - (Enabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Time (in seconds) between checks for expired bans"),
                                        new Key("BanExpiryCheckInterval"),
                                        new Value("60", NUMBER),
                                        new DefaultValue("60")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("The path to your TrinityCore source directory.",
                                                "If the path is left empty, the built-in CMAKE_SOURCE_DIR is used."),
                                        new Example("\"../TrinityCore\""),
                                        new Key("SourceDirectory"),
                                        new Value("\"\"", TEXT),
                                        new DefaultValue("\"\"")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("The path to your MySQL CLI binary.",
                                                "If the path is left empty, built-in path from cmake is used."),
                                        new Example("\"C:/Program Files/MySQL/MySQL Server 5.6/bin/mysql.exe\"", "\"mysql.exe\"", "\"/usr/bin/mysql\""),
                                        new Key("MySQLExecutable"),
                                        new Value("\"\"", TEXT),
                                        new DefaultValue("\"\"")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("The path to your IP2Location database CSV file."),
                                        new Example("\"C:/Trinity/IP2LOCATION-LITE-DB1.CSV\"",
                                                "\"/home/trinity/IP2LOCATION-LITE-DB1.CSV\""),
                                        new Key("IPLocationFile"),
                                        new Value("\"\"", TEXT),
                                        new DefaultValue("\"\"  - (Disabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Specifies if IP addresses can be logged to the database"),
                                        new Key("AllowLoggingIPAddressesInDatabase"),
                                        new Value("1", NUMBER),
                                        new DefaultValue("1 - (Enabled)", "0 - (Disabled)")
                                ))
                        )
                ).addSection(new Section(new Description("MYSQL SETTINGS"))
                        .addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Database connection settings for the realm server."),
                                        new Example("\"hostname;port;username;password;database;ssl\"",
                                                "\".;some_number;username;password;database\" ",
                                                " - (Use named pipes on Windows \"enable-named-pipe\" to [mysqld] section my.ini)",
                                                "\".;/path/to/unix_socket;username;password;database\" ",
                                                " - (use Unix sockets on Unix/Linux)"),
                                        new Key("LoginDatabaseInfo"),
                                        new Value("\"127.0.0.1;3306;trinity;trinity;auth\"", TEXT),
                                        new DefaultValue("\"127.0.0.1;3306;trinity;trinity;auth\""),
                                        new Observations("The SSL option will enable TLS when connecting to the specified database. If not provided or",
                                                "any value other than 'ssl' is set, TLS will not be used.")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("The amount of worker threads spawned to handle asynchronous (delayed) MySQL",
                                                "statements. Each worker thread is mirrored with its own connection to the",
                                                "MySQL server and their own thread on the MySQL server."),
                                        new Key("LoginDatabase.WorkerThreads"),
                                        new Value("1", NUMBER),
                                        new DefaultValue("1")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("The amount of MySQL connections spawned to handle."),
                                        new Key("LoginDatabase.SynchThreads"),
                                        new Value("1", NUMBER),
                                        new DefaultValue("1 - (LoginDatabase.SynchThreads)")
                                ))
                        )
                ).addSection(new Section(new Description("CRYPTOGRAPHY"))
                        .addGroup(new Group()
                                .addItem(new Item(
                                        new Description("The master key used to encrypt TOTP secrets for database storage.",
                                                "If you want to change this, uncomment TOTPOldMasterSecret, then copy",
                                                "your old secret there and startup authserver once. Afterwards, you can re-",
                                                "comment that line and get rid of your old secret."),
                                        new Example("000102030405060708090A0B0C0D0E0F"),
                                        new Key("TOTPMasterSecret"),
                                        new Value("", TEXT),
                                        new DefaultValue("<blank> - (Store TOTP secrets unencrypted)")
                                ))
                        )
                ).addSection(new Section(new Description("UPDATE SETTINGS"))
                        .addGroup(new Group()
                                .addItem(new Item(
                                        new Description("A mask that describes which databases shall be updated."),
                                        new Key("Updates.EnableDatabases"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0  - (All Disabled)", "1  - (All Enabled)"),
                                        new Observations("Following flags are available",
                                                "   DATABASE_LOGIN     = 1, // Auth database")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Auto populate empty databases."),
                                        new Key("Updates.AutoSetup"),
                                        new Value("1", NUMBER),
                                        new DefaultValue("1 - (Enabled)", "0 - (Disabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Perform data redundancy checks through hashing",
                                                "to detect changes on sql updates and reapply it."),
                                        new Key("Updates.Redundancy"),
                                        new Value("1", NUMBER),
                                        new DefaultValue("1 - (Enabled)", "0 - (Disabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Check hashes of archived updates (slows down startup)."),
                                        new Key("Updates.ArchivedRedundancy"),
                                        new Value("0", NUMBER),
                                        new DefaultValue("0 - (Disabled)", "1 - (Enabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Inserts the current file hash in the database if it is left empty.",
                                                "Useful if you want to mark a file as applied but you don't know its hash."),
                                        new Key("Updates.AllowRehash"),
                                        new Value("1", NUMBER),
                                        new DefaultValue("1 - (Enabled)", "0 - (Disabled)")
                                ))
                        ).addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Cleans dead/ orphaned references that occur if an update was removed",
                                                " or renamed and edited in one step.",
                                                "It only starts the clean up if the count of the missing updates is below or",
                                                " equal the Updates.CleanDeadRefMaxCount value.",
                                                "This way prevents erasing of the update history due to wrong source directory",
                                                " state (maybe wrong branch or bad revision).",
                                                "Disable this if you want to know if the database is in a possible \"dirty state\"."),
                                        new Key("Updates.CleanDeadRefMaxCount"),
                                        new Value("3", NUMBER),
                                        new DefaultValue("3 - (Enabled)", "0 - (Disabled)", "-1 - (Enabled - unlimited)")
                                ))
                        )
                ).addSection(new Section(new Description("LOGGING SYSTEM SETTINGS"))
                        .addGroup(new Group()
                                .addItem(new Item(
                                        new Description("Defines 'where to log'"),
                                        new Key("Appender.Console"),
                                        new Value("1,2,0", TEXT),
                                        new DefaultValue("1,2,0"),
                                        new Format("Type,LogLevel,Flags,optional1,optional2,optional3",
                                                " ",
                                                "Type",
                                                "    0 - (None)",
                                                "    1 - (Console)",
                                                "    2 - (File)",
                                                "    3 - (DB)",
                                                " ",
                                                "LogLevel",
                                                "    0 - (Disabled)",
                                                "    1 - (Trace)",
                                                "    2 - (Debug)",
                                                "    3 - (Info)",
                                                "    4 - (Warn)",
                                                "    5 - (Error)",
                                                "    6 - (Fatal)",
                                                " ",
                                                "Flags",
                                                "    0 - None",
                                                "    1 - Prefix Timestamp to the text",
                                                "    2 - Prefix Log Level to the text",
                                                "    4 - Prefix Log Filter type to the text",
                                                "    8 - Append timestamp to the log file name. Format: YYYY-MM-DD_HH-MM-SS (Only used with Type = 2)",
                                                "   16 - Make a backup of existing file before overwrite (Only used with Mode = w)"
                                        )
                                ))
                                .addItem(new Item(
                                        new Key("Appender.Bnet"),
                                        new Value("2,2,0,Bnet.log,w", TEXT),
                                        new DefaultValue("2,2,0,Bnet.log,w")
                                ))
                        )
                );
    }

}
