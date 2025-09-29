package com.hdu.utils.cnosdb;


import cfjd.org.apache.arrow.flight.FlightRuntimeException;
import com.hdu.config.CnosDBProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.Properties;

@Component
//@Scope("prototype")
public class CnosdbUtil {
    private static final Logger logger = LoggerFactory.getLogger(CnosdbUtil.class);

    private Connection connection;
    private Statement statementQuery;

    @PostConstruct
    public void init() {
//        try {
//            connection = getConnection();
//        } catch (SQLException e) {
//            logger.error("Error while getting CnosDB connection: " + e.getMessage(), e);
//        }
    }

    private Connection getConnection() throws SQLException {
        final Properties properties = new Properties();
        properties.put("user", CnosDBProperties.USERNAME);
        properties.put("password", CnosDBProperties.PASSWORD);
        properties.put("tenant", CnosDBProperties.TENANT);
        properties.put("db",CnosDBProperties.DATABASE);
        properties.put("useEncryption", CnosDBProperties.USE_ENCRYPTION);

        return DriverManager.getConnection(CnosDBProperties.URL, properties);
    }

    public void executeDel(String sql){
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error("Error while executing delte: " + e.getMessage(), e);
        } catch (FlightRuntimeException e){
            init();
            try {
                statement.execute(sql);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void executeUpdate(String sql) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error("Error while executing update: " + e.getMessage(), e);
        } catch (FlightRuntimeException e){
            init();
            try {
                statement.executeUpdate(sql);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public ResultSet executeQuery(String sql){
        try {
            statementQuery = connection.createStatement();
            ResultSet resultSet = statementQuery.executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            logger.error("Error while executing update: " + e.getMessage(), e);
        }
        return null;
    }

    public void closeResources() {
        if (connection != null) {
            try {

                connection.close();
            } catch (SQLException e) {
                logger.error("Error while closing connection: " + e.getMessage(), e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        closeResources();
    }
}