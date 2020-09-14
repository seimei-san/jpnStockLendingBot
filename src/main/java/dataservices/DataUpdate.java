package dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DataUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataExports.class);


    public static boolean updateSelectionStatus (String userName, String timeStamp, String fromStatus, String toStatus) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");
    
            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
    
            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET status=?, timeStamp=?, updatedBy=? WHERE lenderNo!=0 AND status=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, toStatus);
            preStatementSelect.setString(2, timeStamp);
            preStatementSelect.setString(3, userName);
            preStatementSelect.setString(4, fromStatus);
            preStatementSelect.executeUpdate();
    
            LOGGER.debug("DataUpdate.updateSelectionStatus completed");
    
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateSelectionStatus.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateSelectionStatus.SQLException", e);
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


    public static boolean updateSelectionStatusByData (String userName, String toStatus, String selectionData) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            
            String timeStamp = Miscellaneous.getTimeStamp("transaction");
            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET status=?, timeStamp=?, updatedBy=? WHERE requestId=? AND  lineNo=? AND borrowerName=? AND lenderName=? AND lenderNo!=0";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);

            List<String> selectionLine;
            selectionLine = Arrays.asList(selectionData.split(",", 0));
            String[] tmpArray = new String[24];

            int noOfFields = 23;
            int countItem = 1;
            for (String item : selectionLine) {
                if (countItem <= noOfFields) {
                    tmpArray[countItem] = item;
                    countItem += 1;

                    if (countItem == noOfFields) {
//                        tmpArray[1] // type
//                        tmpArray[2] // lotNo
//                        tmpArray[3] // requestId
                        preStatementSelect.setString(4, tmpArray[3]); // requestId
//                        tmpArray[4] // versionNo
//                        tmpArray[5] // LineNo
                        preStatementSelect.setInt(5, Integer.parseInt(tmpArray[5])); // lineNo
//                        tmpArray[6] // stockCode
//                        tmpArray[7] // borrowerName
                        preStatementSelect.setString(6, tmpArray[7]); // borrowerName
//                        tmpArray[8] // borrowerQty
//                        tmpArray[9] // borrowerStart
//                        tmpArray[10] // borrowerEnd
//                        tmpArray[11] // borrowerRate
//                        tmpArray[12] // borrowerCondition
//                        tmpArray[13] // provideNo
//                        tmpArray[14] // lenderName
                        preStatementSelect.setString(7, tmpArray[14]); // lenderName
//                        tmpArray[15] // lenderQty
//                        tmpArray[16] // lenderStart
//                        tmpArray[17] // lenderEnd
//                        tmpArray[18] // lenderRate
//                        tmpArray[19] // lenderCondition
//                        tmpArray[20] // price
//                        tmpArray[21] // status
                        preStatementSelect.setString(1, toStatus); // status
//                        tmpArray[22] // timStamp
                        preStatementSelect.setString(2, timeStamp); // timeStamp
//                        tmpArray[23] // updateBy
                        preStatementSelect.setString(3, userName); // updateBy

                        preStatementSelect.executeUpdate();
                        countItem = 1;

                    }
                }
            }

            LOGGER.debug("DataUpdate.updateSelectionStatusByData completed");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateSelectionStatusByData.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateSelectionStatusByData.SQLException", e);
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

    public static void getUpdateSelectionStatus(String selectionData, String userName, String toStatus) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//           while (rs.next()) failed into Loop so that limit loop by the count of results

            String sqlUpdateQuoteWithNewStatus = "UPDATE " + ConfigLoader.transactionTable +
                    " SET status=?, timeStamp=?, updatedBy=? WHERE requestId=? AND lineNo=? AND borrowerName=? AND lenderName=? ";
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(sqlUpdateQuoteWithNewStatus);

            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            List<String> selectionLine;
            selectionLine = Arrays.asList(selectionData.split(",", 0));
            String[] tmpArray = new String[24];

            int noOfFields = 23;
            int countItem = 1;
            for (String item : selectionLine) {
                if (countItem<=noOfFields) {
                    tmpArray[countItem] = item;
                    countItem += 1;

                    if (countItem==noOfFields) {
//                        tmpArray[1] // type
//                        tmpArray[2] // lotNo
//                        tmpArray[3] // requestId
                        preparedStatementUpdate.setString(4, tmpArray[3]); // updateBy
//                        tmpArray[4] // versionNo
//                        tmpArray[5] // LineNo
                        preparedStatementUpdate.setInt(5, Integer.parseInt(tmpArray[5])); // updateBy
//                        tmpArray[6] // stockCode
//                        tmpArray[7] // borrowerName
                        preparedStatementUpdate.setString(6, tmpArray[7]); // borrowerName
//                        tmpArray[8] // borrowerQty
//                        tmpArray[9] // borrowerStart
//                        tmpArray[10] // borrowerEnd
//                        tmpArray[11] // borrowerRate
//                        tmpArray[12] // borrowerCondition
//                        tmpArray[13] // provideNo
//                        tmpArray[14] // lenderName
                        preparedStatementUpdate.setString(7, tmpArray[14]); // lenderName
//                        tmpArray[15] // lenderQty
//                        tmpArray[16] // lenderStart
//                        tmpArray[17] // lenderEnd
//                        tmpArray[18] // lenderRate
//                        tmpArray[19] // lenderCondition
//                        tmpArray[20] // price
//                        tmpArray[21] // status
                        preparedStatementUpdate.setString(1, toStatus); // status
//                        tmpArray[22] // timStamp
                        preparedStatementUpdate.setString(2, timeStamp); // timeStamp
//                        tmpArray[23] // updateBy
                        preparedStatementUpdate.setString(3, userName); // updateBy

                        preparedStatementUpdate.executeUpdate();
                        countItem = 1;

                    }
                }

            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getUpdateSelectionStatus.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getUpdateSelectionStatus.SQLException", e);
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
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        LOGGER.debug("DataServices.getUpdateSelectionStatus completed");
    }

    public static boolean updateWithNothing (String type, String requestId, String lenderName, String userName, String timeStamp, String status) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET lenderQty = 0, lenderRate = 0.0, updatedBy = '" +
                    userName + "', timeStamp = '" + timeStamp + "', status = '" + status + "' WHERE type=? AND requestId=? AND lenderName=?";
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

    public static boolean updateBorrowerTransactionStatusByRequestId(String type, String requestId, String lenderName, String fromStatus, String toStatus, String userName, String timeStamp) {
        boolean result = true;
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET updatedBy = '" + userName + "', " +
                    "timeStamp = '" + timeStamp + "', status = '" + toStatus + "' WHERE type=? AND requestId=? AND lenderName=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, type);
            preStatementSelect.setString(2, requestId);
            preStatementSelect.setString(3, lenderName);
            preStatementSelect.executeUpdate();

            LOGGER.debug("DataUpdate.updateBorrowerTransactionStatusByRequestId completed");

        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateBorrowerTransactionStatusByRequestId.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateBorrowerTransactionStatusByRequestId.SQLException", e);
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


    public static boolean updateLenderStatusAfterSentQuote (String borrowerName, String fromStatus, String toStatus) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET status = '" + toStatus + "' WHERE status=? AND borrowerName=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, fromStatus);
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


    public static boolean updateStatusAfterSentSelection (String lenderName, String fromStatus, String toStatus) {
        Connection connection = null;
        Statement statement = null;
        boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");
    
            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
    
            String sql = "UPDATE " + ConfigLoader.transactionTable + " SET status = '" + toStatus + "' WHERE lenderNo!=0 AND status=? AND lenderName=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, fromStatus);
            preStatementSelect.setString(2, lenderName);
    
            preStatementSelect.executeUpdate();
    
            LOGGER.debug("DataUpdate.updateStatusAfterSentSelection completed");
    
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateStatusAfterSentSelection.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateStatusAfterSentSelection.SQLException", e);
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


    public static boolean updateSelection (String userName, String lenderName, String requestId, int lineNo, String status) {
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
                    " SET status=?, timeStamp=?, updatedBy=? WHERE lenderName=? AND requestId=? AND lineNo=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setString(1, status);
            preStatementSelect.setString(2, timeStamp);
            preStatementSelect.setString(3, userName);
            preStatementSelect.setString(4, lenderName);
            preStatementSelect.setString(5, requestId);
            preStatementSelect.setInt(6, lineNo);
    
            preStatementSelect.executeUpdate();
    
            LOGGER.debug("DataUpdate.updateSelection completed");
    
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
            LOGGER.error("DataUpdate.updateSelection.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataUpdate.updateSelection.SQLException", e);
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


    public static boolean updateQuote (String userName, String fromStatus, String borrowerName, String lenderName, String requestId,
                                       int lineNo, int lenderQty, String lenderStart, String lenderEnd, double lenderRate,
                                       String lenderCondition, int price, String toStatus) {
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
                    " SET lenderQty=?, lenderStart=?, lenderEnd=?, lenderRate=?, lenderCondition=?, price=?, status=?, timeStamp=?, updatedBy=? " +
                    " WHERE status=? AND borrowName=? AND lenderName=? AND requestId=? AND lineNo=?";
            System.out.println("Four Digit Date = " + Miscellaneous.fourDigitDate(lenderStart.trim()) + "LEN=" + lenderStart.length());
            PreparedStatement preStatementSelect = connection.prepareStatement(sql);
            preStatementSelect.setInt(1, lenderQty);
            preStatementSelect.setString(2, Miscellaneous.fourDigitDate(lenderStart.trim()));
            preStatementSelect.setString(3, Miscellaneous.fourDigitDate(lenderEnd.trim()));
            preStatementSelect.setDouble(4, lenderRate);
            preStatementSelect.setString(5, lenderCondition);
            preStatementSelect.setInt(6, price);
            preStatementSelect.setString(7, toStatus);
            preStatementSelect.setString(8, timeStamp);
            preStatementSelect.setString(9, userName);
            preStatementSelect.setString(10, fromStatus);
            preStatementSelect.setString(11, borrowerName);
            preStatementSelect.setString(12, lenderName);
            preStatementSelect.setString(13, requestId);
            preStatementSelect.setInt(14, lineNo);

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
    
    
    public static void getUpdateQuoteStatus(String quoteData, String userName, String toStatus) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//           while (rs.next()) failed into Loop so that limit loop by the count of results

            String sqlUpdateQuoteWithNewStatus = "UPDATE " + ConfigLoader.transactionTable +
                    " SET status=?, timeStamp=?, updatedBy=? WHERE requestId=? AND lineNo=? AND borrowerName=? AND lenderName=? ";
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(sqlUpdateQuoteWithNewStatus);

            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            List<String> quoteLine;
            quoteLine = Arrays.asList(quoteData.split(",", 0));
            String[] tmpArray = new String[24];

            int noOfFields = 23;
            int countItem = 1;
            for (String item : quoteLine) {
                if (countItem<=noOfFields) {
                    tmpArray[countItem] = item;
                    countItem += 1;

                    if (countItem==noOfFields) {
//                        tmpArray[1] // type
//                        tmpArray[2] // lotNo
//                        tmpArray[3] // requestId
                        preparedStatementUpdate.setString(4, tmpArray[3]); // updateBy
//                        tmpArray[4] // versionNo
//                        tmpArray[5] // LineNo
                        preparedStatementUpdate.setInt(5, Integer.parseInt(tmpArray[5])); // updateBy
//                        tmpArray[6] // stockCode
//                        tmpArray[7] // borrowerName
                        preparedStatementUpdate.setString(6, tmpArray[7]); // borrowerName
//                        tmpArray[8] // borrowerQty
//                        tmpArray[9] // borrowerStart
//                        tmpArray[10] // borrowerEnd
//                        tmpArray[11] // borrowerRate
//                        tmpArray[12] // borrowerCondition
//                        tmpArray[13] // provideNo
//                        tmpArray[14] // lenderName
                        preparedStatementUpdate.setString(7, tmpArray[14]); // lenderName
//                        tmpArray[15] // lenderQty
//                        tmpArray[16] // lenderStart
//                        tmpArray[17] // lenderEnd
//                        tmpArray[18] // lenderRate
//                        tmpArray[19] // lenderCondition
//                        tmpArray[20] // price
//                        tmpArray[21] // status
                        preparedStatementUpdate.setString(1, toStatus); // status
//                        tmpArray[22] // timStamp
                        preparedStatementUpdate.setString(2, timeStamp); // timeStamp
//                        tmpArray[23] // updateBy
                        preparedStatementUpdate.setString(3, userName); // updateBy

                        preparedStatementUpdate.executeUpdate();
                        countItem = 1;

                    }
                }

            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getUpdateQuoteStatus.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getUpdateQuoteStatus.SQLException", e);
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
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        LOGGER.debug("DataServices.getUpdateQuoteStatus completed");
    }

    public static void getUpdateQuoteByLender(String quoteData, String userName, String toStatus) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//           while (rs.next()) failed into Loop so that limit loop by the count of results

            String sqlUpdateQuoteWithNewStatus = "UPDATE " + ConfigLoader.transactionTable +
                    " SET lenderQty=?, lenderStart=?, lenderEnd=?, lenderRate=?, lenderCondition=?, price=?, " +
                    "status=?, timeStamp=?, updatedBy=? WHERE requestId=? AND lineNo=? AND borrowerName=? AND lenderName=?";
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(sqlUpdateQuoteWithNewStatus);

            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            List<String> quoteLine;
            quoteLine = Arrays.asList(quoteData.split(",", 0));
            String[] tmpArray = new String[24];

            int noOfFields = 23;
            int countItem = 1;
            for (String item : quoteLine) {
                if (countItem<=noOfFields) {
                    tmpArray[countItem] = item;
                    countItem += 1;

                    if (countItem==noOfFields) {
//                        tmpArray[1] // type
//                        tmpArray[2] // lotNo
//                        tmpArray[3] // requestId
                        preparedStatementUpdate.setString(10, tmpArray[3]); // updateBy
//                        tmpArray[4] // versionNo
//                        tmpArray[5] // LineNo
                        preparedStatementUpdate.setInt(11, Integer.parseInt(tmpArray[5])); // updateBy
//                        tmpArray[6] // stockCode
//                        tmpArray[7] // borrowerName
                        preparedStatementUpdate.setString(12, tmpArray[7]); // borrowerName
//                        tmpArray[8] // borrowerQty
//                        tmpArray[9] // borrowerStart
//                        tmpArray[10] // borrowerEnd
//                        tmpArray[11] // borrowerRate
//                        tmpArray[12] // borrowerCondition
//                        tmpArray[13] // provideNo
//                        tmpArray[14] // lenderName
                        preparedStatementUpdate.setString(13, tmpArray[14]); // lenderName
//                        tmpArray[15] // lenderQty
                        preparedStatementUpdate.setInt(1, Integer.parseInt(tmpArray[15])); // lenderQty
//                        tmpArray[16] // lenderStart
                        preparedStatementUpdate.setString(2, tmpArray[16]); // lenderStart
//                        tmpArray[17] // lenderEnd
                        preparedStatementUpdate.setString(3, tmpArray[17]); // lenderEnd
//                        tmpArray[18] // lenderRate
                        preparedStatementUpdate.setDouble(4, Double.parseDouble(tmpArray[18])); // lenderRate
//                        tmpArray[19] // lenderCondition
                        preparedStatementUpdate.setString(5, tmpArray[19]); // lenderCondition
//                        tmpArray[20] // price
                        preparedStatementUpdate.setInt(6, Integer.parseInt(tmpArray[20])); // price
//                        tmpArray[21] // status
                        preparedStatementUpdate.setString(7, toStatus); // status
//                        tmpArray[22] // timStamp
                        preparedStatementUpdate.setString(8, timeStamp); // timeStamp
//                        tmpArray[23] // updateBy
                        preparedStatementUpdate.setString(9, userName); // updateBy

                        preparedStatementUpdate.executeUpdate();
                        countItem = 1;

                    }
                }

            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getUpdateQuoteStatus.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getUpdateQuoteStatus.SQLException", e);
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
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        LOGGER.debug("DataServices.getUpdateQuoteStatus completed");
    }


}
