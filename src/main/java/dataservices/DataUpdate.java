package dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.sql.*;
import java.util.ArrayList;


public class DataUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataExports.class);

    public static boolean updateWithNothing (String type, String requestId, String lenderName, String userName, String timeStamp, String borrowerStatus, String lenderStatus) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET lenderQty = 0, lenderRate = 0.0, updatedBy = '" +
                    userName + "', timeStamp = '" + timeStamp + "', borrowerStatus = '" + borrowerStatus + "', lenderStatus = '" + lenderStatus + "' WHERE type=? AND requestId=? AND lenderName=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, type);
            preStatementSelect.setString(2, requestId);
            preStatementSelect.setString(3, lenderName);
            preStatementSelect.executeUpdate();

            LOGGER.debug("DataUpdate.updateWithNothing completed");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateWithNothing.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateWithNothing.SQLException", e);
            result = false;
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
        return result;

    }


    public ArrayList<CreateRfq> updateTargetRfqForAllNothing(String type, String requestId, String lenderName) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<CreateRfq> targetCreateRfqs = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "SELECT requestId, type, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo FROM "
                    + ConfigLoader.transactionTable + " WHERE type = '" + type + "' AND requestId = " + "'" + requestId + "'" + " AND lenderNo = " + lenderName + ";";
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
    public static boolean updateLenderStatusAfterSentQuote (String borrowerName, String fromLenderStatus, String toLenderStatus) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET lenderStatus = '" + toLenderStatus + "' WHERE lenderStatus=? AND borrowerName=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, fromLenderStatus);
            preStatementSelect.setString(2, borrowerName);

            preStatementSelect.executeUpdate();

            LOGGER.debug("DataUpdate.updateToExported completed");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateToExported.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateToExported.SQLException", e);
            result = false;
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
        return result;

    }

    public static boolean updateQuote (String userName, String fromLenderStatus, String lenderName, String requestId,
                                       int lineNo, int lenderQty, String lenderStart, String lenderEnd, double lenderRate,
                                       String lenderCondition, int price, String toLenderStatus) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String timeStamp = Miscellaneous.getTimeStamp("transaction");

            String sql = "UPDATE " + ConfigLoader.transactionTable +
                    " SET lenderQty=?, lenderStart=?, lenderEnd=?, lenderRate=?, lenderCondition=?, price=?, lenderStatus=?, timeStamp=?, updatedBy=? " +
                    " WHERE lenderStatus=? AND lenderName=? AND requestId=? AND lineNo=?";
            System.out.println("Four Digit Date = " + Miscellaneous.fourDigitDate(lenderStart.trim()) + "LEN=" + lenderStart.length());
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setInt(1, lenderQty);
            preStatementSelect.setString(2, Miscellaneous.fourDigitDate(lenderStart.trim()));
            preStatementSelect.setString(3, Miscellaneous.fourDigitDate(lenderEnd.trim()));
            preStatementSelect.setDouble(4, lenderRate);
            preStatementSelect.setString(5, lenderCondition);
            preStatementSelect.setInt(6, price);
            preStatementSelect.setString(7, toLenderStatus);
            preStatementSelect.setString(8, timeStamp);
            preStatementSelect.setString(9, userName);
            preStatementSelect.setString(10, fromLenderStatus);
            preStatementSelect.setString(11, lenderName);
            preStatementSelect.setString(12, requestId);
            preStatementSelect.setInt(13, lineNo);

            preStatementSelect.executeUpdate();

            LOGGER.debug("DataUpdate.updateQuote completed");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateQuote.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateQuote.SQLException", e);
            result = false;
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
        return result;

    }

}
