package au.org.emii.ncdfgenerator;

import org.apache.commons.cli.*;
import java.io.*;
import java.sql.*;

public class Main {
    public static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ncdfgenerator", options);

        System.exit(3);
    }

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("u", "username", true, "Database user.");
        options.addOption("p", "password", true, "Database password.");
        options.addOption("d", "db", true, "Database connection string.");
        options.addOption("D", "driver", true, "Database driver class.");
        options.addOption("c", "cql", true, "CQL filter to apply.");
        options.addOption("P", "profile", true, "Profile to use.");
        options.addOption("o", "output", true, "Output file to use (output.zip as default).");
        options.addOption("t", "tmp-dir", true, "Set temporary directory for operation.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            usage(options);
        }

        String username = cmd.getOptionValue("u");
        String password = cmd.getOptionValue("p");
        String connectionString = cmd.getOptionValue("d");
        String databaseDriver = cmd.getOptionValue("D", "org.postgresql.Driver");
        String cql = cmd.getOptionValue("c");
        String profile = cmd.getOptionValue("P");
        String outputFile = cmd.getOptionValue("o", "output.zip");
        String tmpDir = cmd.getOptionValue("t", System.getProperty("java.io.tmpdir"));

        if (username == null) { usage(options); }
        if (password == null) { usage(options); }
        if (connectionString == null) { usage(options); }
        if (databaseDriver == null) { usage(options); }
        if (cql == null) { usage(options); }
        if (profile == null) { usage(options); }

        System.out.println("Cql for operation is:");
        System.out.println(cql);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
        }
        catch (Exception e) {
            System.out.println("File output stream exception");
            e.printStackTrace();
            System.exit(1);
        }

        Connection result = null;
        try {
            Class.forName(databaseDriver).newInstance();
        }
        catch (Exception e){
            System.out.printf("Check classpath. Cannot load db driver: '%s'%n", databaseDriver);
            e.printStackTrace();
            System.exit(1);
        }

        try {
            result = DriverManager.getConnection(connectionString, username, password);
        }
        catch (SQLException e){
            System.out.printf("Cannot connect to db: '%s'%n", connectionString);
            e.printStackTrace();
            System.exit(1);
        }

        try {
            NcdfGenerator generator = new NcdfGenerator("./src/test/resources", tmpDir);
            generator.write(profile, cql, result, outputStream);
        }
        catch (Exception e) {
            System.out.println("Write exception");
            e.printStackTrace();
        }
    }
}
