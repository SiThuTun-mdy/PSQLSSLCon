package com.psql.util;

import org.springframework.beans.factory.annotation.Value;
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
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.hikari.data-source-properties.sslcert}")
    private String sslCert;

    @Value("${spring.datasource.hikari.data-source-properties.sslkey}")
    private String sslKey;

    @Value("${spring.datasource.hikari.data-source-properties.sslrootcert}")
    private String sslRootCert;

    @Value("${spring.datasource.hikari.data-source-properties.sslmode}")
    private String sslMode;

    @Value("${spring.datasource.hikari.data-source-properties.ssl}")
    private String ssl;

    public static void main( String[] args )
    {
        SpringApplication.run(App.class);
    }

    @Override
    public void run(String... args) {

        Map<String, String> connectionProperties = Map.of(
                "sslcert", sslCert,
                "sslkey", sslKey,
                "sslrootcert", sslRootCert);

        System.out.println("Checking PostgreSQL SSL connection...");
        checkConnectionSsl(dbUrl, dbUsername, dbPassword, connectionProperties, sslMode, ssl);
    }

    public void checkConnectionSsl(String url, String username, String password, Map<String, String> extraProps, String sslMode, String ssl) {
        Properties props = new Properties();
        props.putAll(extraProps);
        props.put("user", username);
        props.put("password", password);
        props.put("sslmode", sslMode);
        props.put("ssl", ssl);

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
