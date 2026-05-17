package com.psql.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App implements CommandLineRunner
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class);
    }

    @Override
    public void run(String... args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "";
        String BASE_PATH = Paths.get("certs")
                .toAbsolutePath()
                .toString();

        Map<String, String> connectionProperties = Map.of(
                "sslcert", BASE_PATH.concat("/client.crt"),
                "sslkey", BASE_PATH.concat("/client.pk8"),
                "sslrootcert", BASE_PATH.concat("/rootCA.crt"));

        System.out.println("Checking PostgreSQL SSL connection...");
        checkConnectionSsl(url, username, password, connectionProperties);
    }

    public void checkConnectionSsl(String url, String username, String password, Map<String, String> extraProps) {
        Properties props = new Properties();
        props.putAll(extraProps);
        props.put("user", username);
        props.put("password", password);
        props.put("sslmode", "verify-ca");
        props.put("ssl", "true");

        try (Connection connection = DriverManager.getConnection(url, props)) {
            // Verify that this backend is using SSL at the server level.
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("select ssl from pg_stat_ssl where pid = pg_backend_pid()")) {
                if (rs.next() && rs.getBoolean(1)) {
                    System.out.println("Connection was successful and SSL is active");
                    return;
                }
            }
            System.out.println("Connection was successful");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
