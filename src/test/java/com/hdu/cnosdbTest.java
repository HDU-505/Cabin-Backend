package com.hdu;

import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Properties;

public class cnosdbTest {
    @Test
    public void test() {
        final Properties properties = new Properties();
        properties.put("user", "root");
        properties.put("password", "");
        properties.put("tenant", "cnosdb");
        properties.put("useEncryption", false);
        try (
                Connection connection = DriverManager.getConnection(
                        "jdbc:arrow-flight-sql://111.231.12.252:18904", properties
                );
                Statement statement = connection.createStatement())
        {
            ResultSet resultSet = statement.executeQuery("select * from air limit 1;");

            while (resultSet.next()) {
                Timestamp column1 = resultSet.getTimestamp(1);
                String column2 = resultSet.getString(2);
                Double column3 = resultSet.getDouble(3);
                Double column4 = resultSet.getDouble(4);
                Double column5 = resultSet.getDouble(5);

                System.out.printf("%s %s %f %f %f", column1, column2, column3, column4, column5);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}




