package dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;

import java.sql.*;
import java.util.ArrayList;

public class DataUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataExports.class);

    public ArrayList<CreateRfq> updateTargetRfqForAllNothing(String requestId, String lenderName) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<CreateRfq> targetCreateRfqs = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "SELECT requestId, type, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo FROM "
                    + ConfigLoader.transactionTable + " WHERE requestId = " + "'" + requestId + "'" + " AND providerNo = " + lenderName + ";";
            ResultSet resultSet = statement.executeQuery(sql);


            while (resultSet.next()) {
                CreateRfq newCreateRfq = new CreateRfq(resultSet.getString("requestId"),resultSet.getString("type"),
                        resultSet.getInt("lineNo"), resultSet.getString("stockCode"),
                        resultSet.getInt("borrowerQty"), resultSet.getString("borrowerStart"), resultSet.getString("borrowerEnd"));
                targetCreateRfqs.add(newCreateRfq);

            }
            LOGGER.debug("DataUpdate.updateTargetRfqForAllNothing completed");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataUpdate.updateTargetRfqForAllNothing.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateTargetRfqForAllNothing.SQLException", e);
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();

            }
        }
        return targetCreateRfqs;

    }

}
