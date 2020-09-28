package dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;


import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DataServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataServices.class);
    private static DataServices instance;

    public static DataServices getInstance() {
        if (instance == null) {
            instance = new DataServices();
        }
        return instance;
    }

    public static String[] tmpArray;
    public static String[] counterPartiesList;
    public static String[] extRoomIdList;

    private ArrayList<SendRfq> targetSendRfqs;
    private ArrayList<ReceivedRfq> targetReceivedRfqs;
    private ArrayList<SendQuote> targetSendingQuote;
    private ArrayList<ViewRfqQuote> targetViewingRfqs;
    private ArrayList<ViewIoi> targetViewingIois;



    public static String getCounterPartyList() {

//        Get Counter Party List into the public static counterPartiesList to list the options in Lender
        final String JOB_NAME = "Counter Party List Update: ";

        final String COLUMN_COUNTERPARTY_LIST = "counterPartyName";


        Connection connection = null;
        Statement statement = null;

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlCount = "select count(" + COLUMN_COUNTERPARTY_LIST + ") as cnt from " + ConfigLoader.counterPartyTable;
            String sql = "select " + COLUMN_COUNTERPARTY_LIST + " from " + ConfigLoader.counterPartyTable + " ORDER BY " + COLUMN_COUNTERPARTY_LIST + " asc";

            ResultSet rsCount = statement.executeQuery(sqlCount);
            int cnt = rsCount.getInt("cnt");
            if (cnt == 0) {
                return JOB_NAME + "Failed (No CounterParty Data)";
            }

            counterPartiesList = new String[cnt];
            int counter = 0;

            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                counterPartiesList[counter] = rs.getString(COLUMN_COUNTERPARTY_LIST);
                counter += 1;
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getCounterPartyList.ClassException",e);
            return JOB_NAME + "Failed (JDBC Class not exist)";
        } catch (SQLException e) {
            LOGGER.error("DataServices.getCounterPartyList.SQLException",e);
            e.printStackTrace();
            return JOB_NAME + "Failed (SQL Error)";
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
        LOGGER.debug("DataServices.getCounterPartyList completed");
        return JOB_NAME + "Successful";
    }

    public static String[] getCounterPartyInfo(String counterPartyName) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlGetCounterPartyInfo = "select * from " + ConfigLoader.counterPartyTable + " where isActive=1 AND counterPartyName=?";
            PreparedStatement preStatement = connection.prepareStatement(sqlGetCounterPartyInfo);
            preStatement.setString(1, counterPartyName);
            ResultSet resultSet = preStatement.executeQuery();
            tmpArray = new String[6];

            tmpArray[0] = resultSet.getString(1);  //counterPartyId
            tmpArray[1] = resultSet.getString(2);  //counterPartyName
            tmpArray[2] = resultSet.getString(3);  //counterPartyType
            tmpArray[3] = resultSet.getString(4);  //counterPartyBotName
            tmpArray[4] = Miscellaneous.convertRoomId(resultSet.getString(5));  //extChatRoomId
            tmpArray[5] = resultSet.getString(6);
//            tmpArray[5] = rs.getString(6);  //isActive

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataService.getCounterPartyInfo.ClassException",e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataService.getCounterPartyInfo.SQLException", e);
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
        LOGGER.debug("DataService.getCounterPartyInfo completed");
        return tmpArray;
    }

    public static ArrayList<String> getRequestIdList(String type) {

//        Get Request ID List into the public static RequestID to list the options in Lender
        final String JOB_NAME = "Request ID List Update: ";
        final String COLUMN_REQUESTID_LIST = "requestId";

        Connection connection = null;
        Statement statement = null;

        ArrayList<String> requestIdList = new ArrayList<String>();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "select " + COLUMN_REQUESTID_LIST + " from " + ConfigLoader.transactionTable + " WHERE type='" + type + "' GROUP BY " + COLUMN_REQUESTID_LIST;

            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                requestIdList.add(rs.getString(COLUMN_REQUESTID_LIST));
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getRequestIdList.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataServices.getRequestIdList.SQLException", e);
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
        LOGGER.debug("DataServices.getRequestIdList completed");
        return requestIdList;
    }

    public static String getExtRoomIdList() {

//       Get External Room ID list into the public static extRoomIdList in order to avoid createRFQ in External Chat Rooms
        final String JOB_NAME = "External Room ID List Update: ";

        final String COLUMN_COUNTERPARTY_LIST = "counterPartyName";
        final String COLUMN_EXT_ROOMID_LIST = "extChatRoomId";

        Connection connection = null;
        Statement statement = null;

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlCount = "select count(" + COLUMN_COUNTERPARTY_LIST + ") as cnt from " + ConfigLoader.counterPartyTable;
            String sql = "select " + COLUMN_COUNTERPARTY_LIST + ", " + COLUMN_EXT_ROOMID_LIST + " from " + ConfigLoader.counterPartyTable + " ORDER BY " + COLUMN_COUNTERPARTY_LIST + " asc";

            ResultSet rsCount = statement.executeQuery(sqlCount);
            int cnt = rsCount.getInt("cnt");
            if (cnt == 0) {
                return JOB_NAME + "Failed (No CounterParty Data)";
            }

            extRoomIdList = new String[cnt];
            int counter = 0;

            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                extRoomIdList[counter] = Miscellaneous.convertRoomId(resultSet.getString(COLUMN_EXT_ROOMID_LIST));
                counter += 1;
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getExtRoomIdList.ClassException",e);
            return JOB_NAME + "Failed (JDBC Class not exist)";
        } catch (SQLException e) {
            LOGGER.error("DataServices.getExtRoomIdList.SQLException",e);
            e.printStackTrace();
            return JOB_NAME + "Failed (SQL Error)";
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
        LOGGER.debug("DataServices.getExtRoomIdList completed");
        return JOB_NAME + "Successful";
    }

    public static String[] createRequestId(String type) {
        Connection connection = null;
        Statement statement = null;
        String[] requestIdAndLotNo = new String[2];

        int lastLotNo;
        int newLotNo;
        String newRequestId;

        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlFindMaxLotNo = "select max(lotNo) from " + ConfigLoader.transactionTable + " where type='" + type + "'";
            ResultSet rsLastLotNo = statement.executeQuery(sqlFindMaxLotNo);
            lastLotNo = rsLastLotNo.getInt(1);
            newLotNo = lastLotNo + 1;
            requestIdAndLotNo[1] = String.valueOf(newLotNo);
            newRequestId = Miscellaneous.getNewRequestId(type, newLotNo);
            requestIdAndLotNo[0] = newRequestId;


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.createRequestId.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.createRequestId.SQLException", e);
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
        LOGGER.debug("DataServices.createRequestId completed");
        return requestIdAndLotNo;

    }

    public static void insertRfq(String borrowerName, String type, int lotNo, String requestId, int versionNo,
                                 int lineNo, String stockCode, int borrowerQty, String borrowerStart, String borrowerEnd, int lenderNo,  String timeStamp, String userName) {
        Connection connection = null;
        Statement statement = null;


        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//            String timeStamp = Miscellaneous.getTimeStamp("transaction");

            String sqlReserveRequest = "insert into " + ConfigLoader.transactionTable + "(borrowerName, type, lotNo, requestId, " +
                    "versionNo, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo, status, timeStamp, updatedBy) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement preStatement = connection.prepareStatement(sqlReserveRequest);
            preStatement.setString(1,borrowerName);
            preStatement.setString(2,type);
            preStatement.setInt(3,lotNo);
            preStatement.setString(4,requestId);
            preStatement.setInt(5,versionNo);
            preStatement.setInt(6,lineNo);
            preStatement.setString(7,stockCode);
            preStatement.setInt(8,borrowerQty);
            preStatement.setString(9,Miscellaneous.fourDigitDate(borrowerStart));
            preStatement.setString(10,Miscellaneous.fourDigitDate(borrowerEnd));
            preStatement.setInt(11, lenderNo);
            preStatement.setString(12, "");
            preStatement.setString(13,timeStamp);
            preStatement.setString(14,userName);

            preStatement.executeUpdate();


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertRfq.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataServices.insertRfq.SQLException", e);
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
        LOGGER.debug("DataServices.insertRfq completed");

    }

    public static void insertIoi(String lenderName, String type, int lotNo, String requestId, int versionNo,
                                 int lineNo, String stockCode, int lenderQty, String lenderStart, String lenderEnd,
                                 double lenderRate, String lenderCondition, int price, int lenderNo,  String timeStamp, String userName) {
        Connection connection = null;
        Statement statement = null;


        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//            String timeStamp = Miscellaneous.getTimeStamp("transaction");

            String sqlReserveRequest = "insert into " + ConfigLoader.transactionTable + "(lenderName, type, lotNo, requestId, " +
                    "versionNo, lineNo, stockCode, lenderQty, lenderStart, lenderEnd, lenderRate, lenderCondition, price, lenderNo, status, timeStamp, updatedBy) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement preStatement = connection.prepareStatement(sqlReserveRequest);
            preStatement.setString(1,lenderName);
            preStatement.setString(2,type);
            preStatement.setInt(3,lotNo);
            preStatement.setString(4,requestId);
            preStatement.setInt(5,versionNo);
            preStatement.setInt(6,lineNo);
            preStatement.setString(7,stockCode);
            preStatement.setInt(8,lenderQty);
            preStatement.setString(9,Miscellaneous.fourDigitDate(lenderStart));
            preStatement.setString(10,Miscellaneous.fourDigitDate(lenderEnd));
            preStatement.setDouble(11,lenderRate);
            preStatement.setString(12,lenderCondition);
            preStatement.setInt(13,price);
            preStatement.setInt(14, lenderNo);
            preStatement.setString(15, "");
            preStatement.setString(16,timeStamp);
            preStatement.setString(17, userName);

            preStatement.executeUpdate();


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertIoi.ClassException", e);
        } catch (SQLException e) {
            LOGGER.error("DataServices.insertIoi.SQLException", e);
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
        LOGGER.debug("DataServices.insertIoi completed");

    }


    public ArrayList<CreateRfq> getTargetRfqs(String requestId, int lenderNo) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<CreateRfq> targetCreateRfqs = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "SELECT requestId, type, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo FROM "
                    + ConfigLoader.transactionTable + " WHERE requestId = " + "'" + requestId + "'" + " AND lenderNo = " + lenderNo + ";";
            ResultSet resultSet = statement.executeQuery(sql);



            while (resultSet.next()) {
                CreateRfq newCreateRfq = new CreateRfq(
                        resultSet.getString("requestId"),
                        resultSet.getString("type"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"));
                targetCreateRfqs.add(newCreateRfq);

            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getTargetRfqs.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getTargetRfqs.SQLException", e);
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
        LOGGER.debug("DataServices.getTargetRfqs Completed");
        return targetCreateRfqs;
    }

    public ArrayList<ViewRfq> viewTargetRfqs(String requestId) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<ViewRfq> targetViewRfqs = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSuffix = "";
            if (requestId!=null && !requestId.equals("")) {
                sqlSuffix = " AND requestId='" + requestId + "'";
            }

            String sql = "SELECT requestId, type, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo FROM "
                    + ConfigLoader.transactionTable + " WHERE type='RFQ' AND lenderNo=0" + sqlSuffix;
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                ViewRfq newViewRfq = new ViewRfq(
                        resultSet.getString("requestId"),
                        resultSet.getString("type"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"));
                targetViewRfqs.add(newViewRfq);

            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getTargetRfqs.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getTargetRfqs.SQLException", e);
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
        LOGGER.debug("DataServices.viewTargetRfqs Completed");
        return targetViewRfqs;
    }

    public ArrayList<ViewIoi> viewTargetIois(String requestId) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<ViewIoi> targetViewIois = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSuffix = "";
            if (requestId!=null && !requestId.equals("")) {
                sqlSuffix = " AND requestId='" + requestId + "'";
            }

            String sql = "SELECT * FROM " + ConfigLoader.transactionTable + " WHERE type='IOI' AND lenderNo=0" + sqlSuffix;
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy"));
                targetViewIois.add(newViewIoi);

            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewTargetIois.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewTargetIois.SQLException", e);
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
        LOGGER.debug("DataServices.viewTargetIois Completed");
        return targetViewIois;
    }

    public static boolean deleteTransactionsByRequestId(String requestId) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlDeleteByRequestId = "delete from " + ConfigLoader.transactionTable + " where requestId = ?";

            PreparedStatement preStatement = connection.prepareStatement(sqlDeleteByRequestId);
            preStatement.setString(1,requestId);

            preStatement.executeUpdate();


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataService.deleteTransactionsByRequestId.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataService.deleteTransactionsByRequestId.SQLException", e);
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
        LOGGER.debug("DataService.deleteTransactionsByRequestId completed");
        return true;
    }

    public static String insertGetRfqsForTargetCounterParty(String type, String userName, String requestId, String lenderName, int lenderNo) {
        Connection connection = null;
        Statement statement = null;
        String rfqsData = "";
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


//           while (rs.next()) failed into Loop so that limit loop by the count of results
            String sqlCountTargetRfqs = "select count(requestId) as cnt from " + ConfigLoader.transactionTable + " where lenderNo=0 AND type='" + type + "' AND requestId=" + "'" + requestId + "'";
            ResultSet rsCount = statement.executeQuery(sqlCountTargetRfqs);
            int resultCount = rsCount.getInt(1);

            String sqlGetTargetRfqs = "select * from " + ConfigLoader.transactionTable + " where lenderNo=0 AND type='" + type + "' AND requestId=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlGetTargetRfqs);
            preStatementSelect.setString(1, requestId);
            ResultSet resultSet = preStatementSelect.executeQuery();

            String sqlInsertRfqForTargetCounterParty = "insert into " + ConfigLoader.transactionTable +
                    " (type, lotNo, requestId, versionNo, lineNo, stockCode, borrowerName, borrowerQty, " +
                    "borrowerStart, borrowerEnd, borrowerRate, borrowerCondition, lenderNo, lenderName, timeStamp, price, updatedBy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatementInsert = connection.prepareStatement(sqlInsertRfqForTargetCounterParty);

            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            for (int i=1; i<=resultCount; i++) {
                resultSet.next();
                tmpArray = new String[15];
                tmpArray[1] = resultSet.getString(1); // type
                tmpArray[2] = resultSet.getString(2); // lotNo
                tmpArray[3] = resultSet.getString(3); // requestId
                tmpArray[4] = resultSet.getString(4); // versionNo
                tmpArray[5] = resultSet.getString(5); // lineNo
                tmpArray[6] = resultSet.getString(6); // stockCode
                tmpArray[7] = resultSet.getString(7); // borrowerName
                tmpArray[8] = resultSet.getString(8); // borrowerQty
                tmpArray[9] = resultSet.getString(9); // borrowerStart
                tmpArray[10] = resultSet.getString(10); // borrowerEnd
                tmpArray[11] = resultSet.getString(11); // borrowerRate
                tmpArray[12] = resultSet.getString(12); // borrowerCondition

                preparedStatementInsert.setString(1,  tmpArray[1]); // type
                preparedStatementInsert.setInt(2, Integer.parseInt( tmpArray[2])); // lotNo
                preparedStatementInsert.setString(3,  tmpArray[3]); // requestId
                preparedStatementInsert.setInt(4, Integer.parseInt( tmpArray[4])); // versionNo
                preparedStatementInsert.setInt(5, Integer.parseInt( tmpArray[5])); // lineNo
                preparedStatementInsert.setString(6,  tmpArray[6]); // stockCode
                preparedStatementInsert.setString(7,  tmpArray[7]); // borrowerName
                preparedStatementInsert.setInt(8, Integer.parseInt( tmpArray[8])); // borrowerQty
                preparedStatementInsert.setString(9,  tmpArray[9]); // borrowerStart
                preparedStatementInsert.setString(10,  tmpArray[10]); // borrowerEnd
//                preparedStatementInsert.setDouble(11,  tmpArray[11]); // borrowerRate
                preparedStatementInsert.setDouble(11,  0.0); // borrowerRate
                preparedStatementInsert.setString(12,  tmpArray[12]); // borrowerCondition
                preparedStatementInsert.setInt(13, lenderNo); // lenderNo
                preparedStatementInsert.setString(14, lenderName); // lenderName
                preparedStatementInsert.setString(15, timeStamp); // timeStamp
                preparedStatementInsert.setInt(16, 0); // price
                preparedStatementInsert.setString(17, userName); // userName

                preparedStatementInsert.executeUpdate();

                rfqsData += tmpArray[1] + ","; // type
                rfqsData += tmpArray[2] + ","; // lotNo
                rfqsData += tmpArray[3] + ","; // requestId
                rfqsData += Integer.parseInt( tmpArray[4]) + ","; // versionNo
                rfqsData += Integer.parseInt( tmpArray[5]) + ","; // lineNo
                rfqsData += tmpArray[6] + ","; // stockCode
                rfqsData += tmpArray[7] + ","; // borrowerName
                rfqsData += Integer.parseInt( tmpArray[8]) + ","; // borrowerQty
                rfqsData += tmpArray[9] + ","; // borrowerStart
                rfqsData += tmpArray[10] + ","; // borrowerEnd
//                rfqsData += tmpArray[11] + ","; // borrowerRate
                rfqsData += "0.0,"; // borrowerRate
                rfqsData += tmpArray[12] + ","; // borrowerCondition
                rfqsData += lenderNo + ","; // lenderNo
                rfqsData += lenderName + ","; // lenderName
                rfqsData += timeStamp + "\r"; // timeStamp


            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertGetRfqsForTargetCounterParty.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertGetRfqsForTargetCounterParty.SQLException", e);
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
        LOGGER.debug("DataServices.insertGetRfqsForTargetCounterParty completed");
        return rfqsData;

    }

    public static String insertGetIoisForTargetCounterParty(String type, String userName, String requestId, String borrowerName, int lenderNo) {
        Connection connection = null;
        Statement statement = null;
        String ioisData = "";
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


//           while (rs.next()) failed into Loop so that limit loop by the count of results
            String sqlCountTargetIois = "select count(requestId) as cnt from " + ConfigLoader.transactionTable + " where lenderNo=0 AND type='" + type + "' AND requestId=" + "'" + requestId + "'";
            ResultSet rsCount = statement.executeQuery(sqlCountTargetIois);
            int resultCount = rsCount.getInt(1);

            String sqlGetTargetIois = "select * from " + ConfigLoader.transactionTable + " where lenderNo=0 AND type='" + type + "' AND requestId=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlGetTargetIois);
            preStatementSelect.setString(1, requestId);
            ResultSet resultSet = preStatementSelect.executeQuery();

            String sqlInsertIoiForTargetCounterParty = "insert into " + ConfigLoader.transactionTable +
                    " (type, lotNo, requestId, versionNo, lineNo, stockCode, borrowerName, borrowerQty, " +
                    "borrowerStart, borrowerEnd, borrowerRate, borrowerCondition, lenderNo, lenderName, lenderQty, lenderStart, " +
                    "lenderEnd, lenderRate, lenderCondition, price, status, timeStamp, updatedBy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatementInsert = connection.prepareStatement(sqlInsertIoiForTargetCounterParty);

            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            for (int i=1; i<=resultCount; i++) {
                resultSet.next();
                tmpArray = new String[24];
                tmpArray[1] = resultSet.getString(1); // type
                tmpArray[2] = resultSet.getString(2); // lotNo
                tmpArray[3] = resultSet.getString(3); // requestId
                tmpArray[4] = resultSet.getString(4); // versionNo
                tmpArray[5] = resultSet.getString(5); // lineNo
                tmpArray[6] = resultSet.getString(6); // stockCode
                tmpArray[7] = resultSet.getString(7); // borrowerName
                tmpArray[8] = resultSet.getString(8); // borrowerQty
                tmpArray[9] = resultSet.getString(9); // borrowerStart
                tmpArray[10] = resultSet.getString(10); // borrowerEnd
                tmpArray[11] = resultSet.getString(11); // borrowerRate
                tmpArray[12] = resultSet.getString(12); // borrowerCondition
                tmpArray[13] = resultSet.getString(13); // lenderNo
                tmpArray[14] = resultSet.getString(14); // lenderName
                tmpArray[15] = resultSet.getString(15); // lenderQty
                tmpArray[16] = resultSet.getString(16); // lenderStart
                tmpArray[17] = resultSet.getString(17); // lenderEnd
                tmpArray[18] = resultSet.getString(18); // lenderRate
                tmpArray[19] = resultSet.getString(19); // lenderCondition
                tmpArray[20] = resultSet.getString(20); // price
                tmpArray[21] = resultSet.getString(21); // status
                tmpArray[22] = resultSet.getString(22); // timestamp
                tmpArray[23] = resultSet.getString(23); // updatedBy

                preparedStatementInsert.setString(1,  tmpArray[1]); // type
                preparedStatementInsert.setInt(2, Integer.parseInt( tmpArray[2])); // lotNo
                preparedStatementInsert.setString(3,  tmpArray[3]); // requestId
                preparedStatementInsert.setInt(4, Integer.parseInt( tmpArray[4])); // versionNo
                preparedStatementInsert.setInt(5, Integer.parseInt( tmpArray[5])); // lineNo
                preparedStatementInsert.setString(6,  tmpArray[6]); // stockCode
                preparedStatementInsert.setString(7,  borrowerName); // borrowerName
                preparedStatementInsert.setInt(8, Integer.parseInt(tmpArray[8])); // borrowerQty
                preparedStatementInsert.setString(9,  tmpArray[9]); // borrowerStart
                preparedStatementInsert.setString(10,  tmpArray[10]); // borrowerEnd
                preparedStatementInsert.setDouble(11,  Double.parseDouble(tmpArray[11])); // borrowerRate
                preparedStatementInsert.setString(12,  tmpArray[12]); // borrowerCondition
                preparedStatementInsert.setInt(13, lenderNo); // lenderNo
                preparedStatementInsert.setString(14, tmpArray[14]); // lenderName
                preparedStatementInsert.setInt(15, Integer.parseInt(tmpArray[15])); // lenderQty
                preparedStatementInsert.setString(16, tmpArray[16]); // lenderStart
                preparedStatementInsert.setString(17, tmpArray[17]); // lenderEnd
                preparedStatementInsert.setDouble(18, Double.parseDouble(tmpArray[18])); // lenderRate
                preparedStatementInsert.setString(19, tmpArray[19]); // lenderCondition
                preparedStatementInsert.setInt(20, Integer.parseInt(tmpArray[20])); // price
                preparedStatementInsert.setString(21, tmpArray[21]); // status
                preparedStatementInsert.setString(22, timeStamp); // timestamp
                preparedStatementInsert.setString(23, userName); // updatedBy

                preparedStatementInsert.executeUpdate();

                ioisData += tmpArray[1] + ","; // type 1
                ioisData += tmpArray[2] + ","; // lotNo 2
                ioisData += tmpArray[3] + ","; // requestId 3
                ioisData += Integer.parseInt( tmpArray[4]) + ","; // versionNo 4
                ioisData += Integer.parseInt( tmpArray[5]) + ","; // lineNo 5
                ioisData += tmpArray[6] + ","; // stockCode 6
                ioisData += borrowerName + ","; // borrowerName 7
                ioisData += Integer.parseInt( tmpArray[8]) + ","; // borrowerQty 8
                ioisData += tmpArray[9] + ","; // borrowerStart 9
                ioisData += tmpArray[10] + ","; // borrowerEnd 10
                ioisData += Double.parseDouble(tmpArray[11]) + ","; // borrowerRate 11
                ioisData += tmpArray[12] + ","; // borrowerCondition 12
                ioisData += lenderNo + ","; // lenderNo 13
                ioisData += tmpArray[14] + ","; // lenderName 14
                ioisData += Integer.parseInt(tmpArray[15]) + ","; // lenderQty 15
                ioisData += tmpArray[16] + ","; // lenderStart 16
                ioisData += tmpArray[17] + ","; // lenderEnd 17
                ioisData += Double.parseDouble(tmpArray[18]) + ","; // lenderRate 18
                ioisData += tmpArray[19] + ","; // lenderCondition 19
                ioisData += Integer.parseInt(tmpArray[20]) + ","; // price 20
//                ioisData += tmpArray[21] + ","; // status 21
                ioisData += "NEW" + ","; // status 21
                ioisData += timeStamp + ","; // timeStamp 22
                ioisData += userName + "\r"; // updatedBy 23
//                ioisData += userName + ","; // updatedBy 23


            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertGetIoisForTargetCounterParty.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertGetIoisForTargetCounterParty.SQLException", e);
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
        LOGGER.debug("DataServices.insertGetIoisForTargetCounterParty completed");
        return ioisData;

    }

    public static void getInsertRfqsIntoTargetCounterParty(String rfqData, String userName) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


//           while (rs.next()) failed into Loop so that limit loop by the count of results

            String sqlInsertRfqForTargetCounterParty = "insert into " + ConfigLoader.transactionTable +
                    " (type, lotNo, requestId, versionNo, lineNo, stockCode, borrowerName, borrowerQty, " +
                    "borrowerStart, borrowerEnd, borrowerRate, borrowerCondition, lenderNo, lenderName, " +
                    "timeStamp, price, status, updatedBy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatementInsert = connection.prepareStatement(sqlInsertRfqForTargetCounterParty);

            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            List<String> rfqLine;
            rfqLine = Arrays.asList(rfqData.split(",", 0));
            tmpArray = new String[16];

            int noOfFields = 15;
            int countItem = 1;
            for (String item : rfqLine) {
                if (countItem<=noOfFields) {
                    tmpArray[countItem] = item;
                    countItem += 1;

                    if (countItem==noOfFields) {
//                preparedStatementInsert.setString(1,  tmpArray[1]); // type
                        preparedStatementInsert.setString(1,  "RFQ"); // type
                        preparedStatementInsert.setInt(2, Integer.parseInt( tmpArray[2])); // lotNo
                        preparedStatementInsert.setString(3,  tmpArray[3]); // requestId
                        preparedStatementInsert.setInt(4, Integer.parseInt( tmpArray[4])); // versionNo
                        preparedStatementInsert.setInt(5, Integer.parseInt( tmpArray[5])); // lineNo
                        preparedStatementInsert.setString(6,  tmpArray[6]); // stockCode
                        preparedStatementInsert.setString(7,  tmpArray[7]); // borrowerName
                        preparedStatementInsert.setInt(8, Integer.parseInt( tmpArray[8])); // borrowerQty
                        preparedStatementInsert.setString(9,  tmpArray[9]); // borrowerStart
                        preparedStatementInsert.setString(10,  tmpArray[10]); // borrowerEnd
//                        preparedStatementInsert.setDouble(11,  tmpArray[11]); // borrowerRate
                        preparedStatementInsert.setDouble(11,  0.0); // borrowerRate
                        preparedStatementInsert.setString(12,  tmpArray[12]); // borrowerCondition
                        preparedStatementInsert.setInt(13, Integer.parseInt(tmpArray[13])); // lenderNo
                        preparedStatementInsert.setString(14, tmpArray[14]); // lenderName
                        preparedStatementInsert.setString(15, timeStamp); // timeStamp
                        preparedStatementInsert.setInt(16, 0); // price
                        preparedStatementInsert.setString(17, "YET"); // status
                        preparedStatementInsert.setString(18, userName); // userName
                        preparedStatementInsert.executeUpdate();
                        countItem = 1;

                    }
                }



            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getInsertRfqsIntoTargetCounterParty.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getInsertRfqsIntoTargetCounterParty.SQLException", e);
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
        LOGGER.debug("DataServices.getInsertRfqsIntoTargetCounterParty completed");

    }

    public static void getInsertIoisIntoTargetCounterParty(String ioiData, String userName) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


//           while (rs.next()) failed into Loop so that limit loop by the count of results

            String sqlInsertIoiForTargetCounterParty = "insert into " + ConfigLoader.transactionTable +
                    " (type, lotNo, requestId, versionNo, lineNo, stockCode, borrowerName, borrowerQty, " +
                    "borrowerStart, borrowerEnd, borrowerRate, borrowerCondition, lenderNo, lenderName, lenderQty, lenderStart, " +
                    "lenderEnd, lenderRate, lenderCondition, price, status, timeStamp, updatedBy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatementInsert = connection.prepareStatement(sqlInsertIoiForTargetCounterParty);
            String timeStamp = Miscellaneous.getTimeStamp("transaction");


            List<String> ioiLine;
            ioiLine = Arrays.asList(ioiData.split(",", 0));
            tmpArray = new String[24];

            int noOfFields = 23;
            int countItem = 1;
            for (String item : ioiLine) {
                if (countItem<=noOfFields) {
                    tmpArray[countItem] = item;
                    countItem += 1;

                    if (countItem==noOfFields) {
                        preparedStatementInsert.setString(1,  "IOI"); // type
                        preparedStatementInsert.setInt(2, Integer.parseInt( tmpArray[2])); // lotNo
                        preparedStatementInsert.setString(3,  tmpArray[3]); // requestId
                        preparedStatementInsert.setInt(4, Integer.parseInt( tmpArray[4])); // versionNo
                        preparedStatementInsert.setInt(5, Integer.parseInt( tmpArray[5])); // lineNo
                        preparedStatementInsert.setString(6,  tmpArray[6]); // stockCode
                        preparedStatementInsert.setString(7,  tmpArray[7]); // borrowerName
//                        preparedStatementInsert.setInt(8, Integer.parseInt(tmpArray[8])); // borrowerQty
                        preparedStatementInsert.setInt(8, Integer.parseInt(tmpArray[15])); // borrowerQty <- lenderQty
//                        preparedStatementInsert.setString(9,  tmpArray[9]); // borrowerStart
                        preparedStatementInsert.setString(9,  tmpArray[16]); // borrowerStart <- lenderStart
//                        preparedStatementInsert.setString(10,  tmpArray[10]); // borrowerEnd
                        preparedStatementInsert.setString(10,  tmpArray[17]); // borrowerEnd <- lenderEnd
//                        preparedStatementInsert.setDouble(11,  Double.parseDouble(tmpArray[11])); // borrowerRate
                        preparedStatementInsert.setDouble(11,  Double.parseDouble(tmpArray[18])); // borrowerRate <- lenderRate
//                        preparedStatementInsert.setString(12,  tmpArray[12]); // borrowerCondition
                        preparedStatementInsert.setString(12,  tmpArray[19]); // borrowerCondition <- lenderCondition
                        preparedStatementInsert.setInt(13, Integer.parseInt(tmpArray[13])); // lenderNo
                        preparedStatementInsert.setString(14, tmpArray[14]); // lenderName
                        preparedStatementInsert.setInt(15, Integer.parseInt(tmpArray[15])); // lenderQty
                        preparedStatementInsert.setString(16, tmpArray[16]); // lenderStart
                        preparedStatementInsert.setString(17, tmpArray[17]); // lenderEnd
                        preparedStatementInsert.setDouble(18, Double.parseDouble(tmpArray[18])); // lenderRate
                        preparedStatementInsert.setString(19, tmpArray[19]); // lenderCondition
                        preparedStatementInsert.setInt(20, Integer.parseInt(tmpArray[20])); // price
                        preparedStatementInsert.setString(21, tmpArray[21]); // status
                        preparedStatementInsert.setString(22, timeStamp); // timestamp
                        preparedStatementInsert.setString(23, userName); // updatedBy
                        preparedStatementInsert.executeUpdate();
                        countItem = 1;
                        tmpArray = new String[24];

                    }
                }



            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getInsertIoisIntoTargetCounterParty.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getInsertIoisIntoTargetCounterParty.SQLException", e);
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
        LOGGER.debug("DataServices.getInsertIoisIntoTargetCounterParty completed");

    }

    public ArrayList<SendRfq> getSendRfqs(String requestId, String lenderName) {

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectSendRfqs = "SELECT * FROM " + ConfigLoader.transactionTable +
                    " WHERE requestId=? AND lenderName=?";
//            ResultSet resultSet = statement.executeQuery(sql);
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlSelectSendRfqs);
            preStatementSelect.setString(1, requestId);
            preStatementSelect.setString(2, lenderName);
            ResultSet resultSet = preStatementSelect.executeQuery();

            this.targetSendRfqs = new ArrayList<>();

            while (resultSet.next()) {
                SendRfq newSendRfq = new SendRfq(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")

                );
                targetSendRfqs.add(newSendRfq);
            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSendRfqs.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSendRfqs.SQLException", e);
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
        LOGGER.debug("DataServices.getSendRfqs completed");
        return targetSendRfqs;
    }

    public ArrayList<ViewIoi> getSendIois(String requestId, String borrowerName) {

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectSendRfqs = "SELECT * FROM " + ConfigLoader.transactionTable +
                    " WHERE requestId=? AND borrowerName=?";
//            ResultSet resultSet = statement.executeQuery(sql);
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlSelectSendRfqs);
            preStatementSelect.setString(1, requestId);
            preStatementSelect.setString(2, borrowerName);
            ResultSet resultSet = preStatementSelect.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (resultSet.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")

                );
                targetViewingIois.add(newViewIoi);
            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSendIois.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSendIois.SQLException", e);
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
        LOGGER.debug("DataServices.getSendIois completed");
        return targetViewingIois;
    }

    public ArrayList<ReceivedRfq> getReceivedRfqs(String status) {

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectReceivedRfqs = "SELECT * FROM " + ConfigLoader.transactionTable + " WHERE type=? AND status=?";
//            ResultSet resultSet = statement.executeQuery(sql);
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlSelectReceivedRfqs);
            preStatementSelect.setString(1, "RFQ");
            preStatementSelect.setString(2, status);
//            preStatementSelect.setString(2, requestId);
            ResultSet resultSet = preStatementSelect.executeQuery();

            this.targetReceivedRfqs = new ArrayList<>();

            while (resultSet.next()) {
                ReceivedRfq newReceivedRfq = new ReceivedRfq(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")

                );
                targetReceivedRfqs.add(newReceivedRfq);
            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getReceivedRfq.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getReceivedRfq.SQLException", e);
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
        LOGGER.debug("DataServices.getReceivedRfq completed");
        return targetReceivedRfqs;
    }

    public ArrayList<ViewIoi> getReceivedIois(String status) {

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectReceivedIois = "SELECT * FROM " + ConfigLoader.transactionTable + " WHERE type=? AND status=?";
//            ResultSet resultSet = statement.executeQuery(sql);
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlSelectReceivedIois);
            preStatementSelect.setString(1, "IOI");
            preStatementSelect.setString(2, status);
//            preStatementSelect.setString(2, requestId);
            ResultSet resultSet = preStatementSelect.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (resultSet.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")

                );
                targetViewingIois.add(newViewIoi);
            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getReceivedIois.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getReceivedIois.SQLException", e);
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
        LOGGER.debug("DataServices.getReceivedIois completed");
        return targetViewingIois;
    }

    public ArrayList<ViewIoi> viewImportIoi(String requestId, int lenderNo) {

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectReceivedRfqs = "SELECT * FROM " + ConfigLoader.transactionTable + " WHERE type=? AND requestId=? AND lenderNo=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlSelectReceivedRfqs);
            preStatementSelect.setString(1, "IOI");
            preStatementSelect.setString(2, requestId);
            preStatementSelect.setInt(3, 0);
            ResultSet resultSet = preStatementSelect.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (resultSet.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")

                );
                targetViewingIois.add(newViewIoi);
            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewImportIoi.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewImportIoi.SQLException", e);
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
        LOGGER.debug("DataServices.viewImportIoi completed");
        return targetViewingIois;
    }

    public ArrayList<String> listBorrowerNames(String type, String status) {
        Connection connection = null;
        Statement statement = null;
        ArrayList<String> listBorrowerNames = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            String sqlListTargetBorrowers = "select borrowerName from " + ConfigLoader.transactionTable + " where type='" + type + "' AND status='" + status + "' GROUP By borrowerName";
            ResultSet borrowerList = statement.executeQuery(sqlListTargetBorrowers);
            while (borrowerList.next()) {
                String listBorrowerName = borrowerList.getString("borrowerName");
                listBorrowerNames.add(listBorrowerName);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.listBorrowerNames.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.listBorrowerNames.SQLException", e);
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
        LOGGER.debug("DataServices.listBorrowerNames completed");
        return listBorrowerNames;

    }

    public ArrayList<String> listLenderNames(String type, String status) {
        Connection connection = null;
        Statement statement = null;
        ArrayList<String> listLenderNames = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            String sqlListTargetLenders = "select lenderName from " + ConfigLoader.transactionTable + " where lenderNo!=0 AND type='" + type + "' AND status='" + status + "' GROUP By lenderName";

            ResultSet lenderList = statement.executeQuery(sqlListTargetLenders);
            while (lenderList.next()) {
                String listLenderName = lenderList.getString("lenderName");
                listLenderNames.add(listLenderName);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.listLenderNames.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.listLenderNames.SQLException", e);
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
        LOGGER.debug("DataServices.listLenderNames completed");
        return listLenderNames;

    }

    public ArrayList<SendQuote> getQuoteForBorrower(String borrowerName, String toStatus) {

        Connection connection = null;
        Statement statement = null;

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectQuoteToBorrower = "select * from " + ConfigLoader.transactionTable + " where type='RFQ' AND (status='NEW' OR status='UPDATE') AND borrowerName=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectQuoteToBorrower);
            preparedStatement.setString(1, borrowerName);
            ResultSet quotesForBorrower = preparedStatement.executeQuery();

            this.targetSendingQuote = new ArrayList<>();

            while (quotesForBorrower.next()) {
                SendQuote newSendQuote = new SendQuote(
                        quotesForBorrower.getString("type"),
                        quotesForBorrower.getInt("lotNo"),
                        quotesForBorrower.getString("requestId"),
                        quotesForBorrower.getInt("versionNo"),
                        quotesForBorrower.getInt("lineNo"),
                        quotesForBorrower.getString("stockCode"),
                        quotesForBorrower.getString("borrowerName"),
                        quotesForBorrower.getInt("borrowerQty"),
                        quotesForBorrower.getString("borrowerStart"),
                        quotesForBorrower.getString("borrowerEnd"),
                        quotesForBorrower.getDouble("borrowerRate"),
                        quotesForBorrower.getString("borrowerCondition"),
                        quotesForBorrower.getInt("lenderNo"),
                        quotesForBorrower.getString("lenderName"),
                        quotesForBorrower.getInt("lenderQty"),
                        quotesForBorrower.getString("lenderStart"),
                        quotesForBorrower.getString("lenderEnd"),
                        quotesForBorrower.getDouble("lenderRate"),
                        quotesForBorrower.getString("lenderCondition"),
                        quotesForBorrower.getInt("price"),
//                        quotesForBorrower.getString("status"),
                        toStatus,
                        quotesForBorrower.getString("timeStamp"),
                        quotesForBorrower.getString("updatedBy")
                );
                targetSendingQuote.add(newSendQuote);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getQuoteForBorrower.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getQuoteForBorrower.SQLException", e);
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
        LOGGER.debug("DataServices.getQuoteForBorrower completed");
        System.out.println("Size of Array=" + targetSendingQuote.size());
        return targetSendingQuote;
    }

    public ArrayList<ViewIoi> getIoiForBorrower(String borrowerName, String toStatus) {

        Connection connection = null;
        Statement statement = null;

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectIoiToBorrower = "select * from " + ConfigLoader.transactionTable + " where type='IOI' AND (status='NEW' OR status='UPDATE') AND borrowerName=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectIoiToBorrower);
            preparedStatement.setString(1, borrowerName);
            ResultSet IoisForBorrower = preparedStatement.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (IoisForBorrower.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        IoisForBorrower.getString("type"),
                        IoisForBorrower.getInt("lotNo"),
                        IoisForBorrower.getString("requestId"),
                        IoisForBorrower.getInt("versionNo"),
                        IoisForBorrower.getInt("lineNo"),
                        IoisForBorrower.getString("stockCode"),
                        IoisForBorrower.getString("borrowerName"),
                        IoisForBorrower.getInt("borrowerQty"),
                        IoisForBorrower.getString("borrowerStart"),
                        IoisForBorrower.getString("borrowerEnd"),
                        IoisForBorrower.getDouble("borrowerRate"),
                        IoisForBorrower.getString("borrowerCondition"),
                        IoisForBorrower.getInt("lenderNo"),
                        IoisForBorrower.getString("lenderName"),
                        IoisForBorrower.getInt("lenderQty"),
                        IoisForBorrower.getString("lenderStart"),
                        IoisForBorrower.getString("lenderEnd"),
                        IoisForBorrower.getDouble("lenderRate"),
                        IoisForBorrower.getString("lenderCondition"),
                        IoisForBorrower.getInt("price"),
//                        IoisForBorrower.getString("status"),
                        toStatus,
                        IoisForBorrower.getString("timeStamp"),
                        IoisForBorrower.getString("updatedBy")
                );
                targetViewingIois.add(newViewIoi);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getIoiForBorrower.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getIoiForBorrower.SQLException", e);
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
        LOGGER.debug("DataServices.getIoiForBorrower completed");
        System.out.println("Size of Array=" + targetSendingQuote.size());
        return targetViewingIois;
    }


    public String getQuoteForBorrowerToTextarea(String borrowerName, String fromStatus, String toStatus) {

        Connection connection = null;
        Statement statement = null;
        String quoteData = "";

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectQuoteToBorrower = "select * from " + ConfigLoader.transactionTable + " where type='RFQ' AND (status=? OR status='UPDATE') AND borrowerName=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectQuoteToBorrower);
            preparedStatement.setString(1, fromStatus);
            preparedStatement.setString(2, borrowerName);
            ResultSet quotesForBorrower = preparedStatement.executeQuery();

            while (quotesForBorrower.next()) {
                quoteData += quotesForBorrower.getString("type") + ",";
                quoteData += quotesForBorrower.getInt("lotNo") + ",";
                quoteData += quotesForBorrower.getString("requestId") + ",";
                quoteData += quotesForBorrower.getInt("versionNo") + ",";
                quoteData += quotesForBorrower.getInt("lineNo") + ",";
                quoteData += quotesForBorrower.getString("stockCode") + ",";
                quoteData += quotesForBorrower.getString("borrowerName") + ",";
                quoteData += quotesForBorrower.getInt("borrowerQty") + ",";
                quoteData += quotesForBorrower.getString("borrowerStart") + ",";
                quoteData += quotesForBorrower.getString("borrowerEnd") + ",";
                quoteData += quotesForBorrower.getDouble("borrowerRate") + ",";
                quoteData += quotesForBorrower.getString("borrowerCondition") + ",";
                quoteData += quotesForBorrower.getInt("lenderNo") + ",";
                quoteData += quotesForBorrower.getString("lenderName") + ",";
                quoteData += quotesForBorrower.getInt("lenderQty") + ",";
                quoteData += quotesForBorrower.getString("lenderStart") + ",";
                quoteData += quotesForBorrower.getString("lenderEnd") + ",";
                quoteData += quotesForBorrower.getDouble("lenderRate") + ",";
                quoteData += quotesForBorrower.getString("lenderCondition") + ",";
//                quoteData += quotesForBorrower.getString("status") + ",";
                quoteData += quotesForBorrower.getInt("price") + ",";
                quoteData += toStatus + ",";
                quoteData += quotesForBorrower.getString("timeStamp") + ",";
                quoteData += quotesForBorrower.getString("updatedBy");
                quoteData += "\r";

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getQuoteForBorrowerToTextarea.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getQuoteForBorrowerToTextarea.SQLException", e);
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
        LOGGER.debug("DataServices.getQuoteForBorrowerToTextarea completed");
        return quoteData;
    }

    public String getSelectionForLenderToTextarea(String type, String lenderName, String fromStatus, String toStatus) {

        Connection connection = null;
        Statement statement = null;
        String selectionData = "";

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectQuoteToLender = "select * from " + ConfigLoader.transactionTable + " WHERE type='" + type + "' AND lenderName=? AND lenderNo!=0 AND status=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectQuoteToLender);
            preparedStatement.setString(1, lenderName);
            preparedStatement.setString(2, fromStatus);
            ResultSet quotesForLender = preparedStatement.executeQuery();

            while (quotesForLender.next()) {
                selectionData += quotesForLender.getString("type") + ",";
                selectionData += quotesForLender.getInt("lotNo") + ",";
                selectionData += quotesForLender.getString("requestId") + ",";
                selectionData += quotesForLender.getInt("versionNo") + ",";
                selectionData += quotesForLender.getInt("lineNo") + ",";
                selectionData += quotesForLender.getString("stockCode") + ",";
                selectionData += quotesForLender.getString("borrowerName") + ",";
                selectionData += quotesForLender.getInt("borrowerQty") + ",";
                selectionData += quotesForLender.getString("borrowerStart") + ",";
                selectionData += quotesForLender.getString("borrowerEnd") + ",";
                selectionData += quotesForLender.getDouble("borrowerRate") + ",";
                selectionData += quotesForLender.getString("borrowerCondition") + ",";
                selectionData += quotesForLender.getInt("lenderNo") + ",";
                selectionData += quotesForLender.getString("lenderName") + ",";
                selectionData += quotesForLender.getInt("lenderQty") + ",";
                selectionData += quotesForLender.getString("lenderStart") + ",";
                selectionData += quotesForLender.getString("lenderEnd") + ",";
                selectionData += quotesForLender.getDouble("lenderRate") + ",";
                selectionData += quotesForLender.getString("lenderCondition") + ",";
                selectionData += quotesForLender.getInt("price") + ",";
                selectionData += toStatus + ",";
                selectionData += quotesForLender.getString("timeStamp") + ",";
                selectionData += quotesForLender.getString("updatedBy");
                selectionData += "\r";

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSelectionForLenderToTextarea.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSelectionForLenderToTextarea.SQLException", e);
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
        LOGGER.debug("DataServices.getSelectionForLenderToTextarea completed");
        return selectionData;
    }

    public String getSelectionForBorrowerToTextarea(String type, String borrowerName, String fromStatus, String toStatus) {

        Connection connection = null;
        Statement statement = null;
        String selectionData = "";

        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectQuoteToLender = "select * from " + ConfigLoader.transactionTable + " WHERE type='" + type + "' AND borrowerName=? AND lenderNo!=0 AND status=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectQuoteToLender);
            preparedStatement.setString(1, borrowerName);
            preparedStatement.setString(2, fromStatus);
            ResultSet quotesForLender = preparedStatement.executeQuery();

            while (quotesForLender.next()) {
                selectionData += quotesForLender.getString("type") + ",";
                selectionData += quotesForLender.getInt("lotNo") + ",";
                selectionData += quotesForLender.getString("requestId") + ",";
                selectionData += quotesForLender.getInt("versionNo") + ",";
                selectionData += quotesForLender.getInt("lineNo") + ",";
                selectionData += quotesForLender.getString("stockCode") + ",";
                selectionData += quotesForLender.getString("borrowerName") + ",";
                selectionData += quotesForLender.getInt("borrowerQty") + ",";
                selectionData += quotesForLender.getString("borrowerStart") + ",";
                selectionData += quotesForLender.getString("borrowerEnd") + ",";
                selectionData += quotesForLender.getDouble("borrowerRate") + ",";
                selectionData += quotesForLender.getString("borrowerCondition") + ",";
                selectionData += quotesForLender.getInt("lenderNo") + ",";
                selectionData += quotesForLender.getString("lenderName") + ",";
                selectionData += quotesForLender.getInt("lenderQty") + ",";
                selectionData += quotesForLender.getString("lenderStart") + ",";
                selectionData += quotesForLender.getString("lenderEnd") + ",";
                selectionData += quotesForLender.getDouble("lenderRate") + ",";
                selectionData += quotesForLender.getString("lenderCondition") + ",";
                selectionData += quotesForLender.getInt("price") + ",";
                selectionData += toStatus + ",";
                selectionData += quotesForLender.getString("timeStamp") + ",";
                selectionData += quotesForLender.getString("updatedBy");
                selectionData += "\r";

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSelectionForLenderToTextarea.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.getSelectionForLenderToTextarea.SQLException", e);
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
        LOGGER.debug("DataServices.getSelectionForLenderToTextarea completed");
        return selectionData;
    }

    public ArrayList<ViewRfqQuote> viewRfqUpdatedByLender(String requestId, String lenderName, String status) {

        Connection connection = null;
        Statement statement = null;


        try {
            Class.forName("org.sqlite.JDBC");

            String sqlSuffix = "";
            if (requestId!=null && !requestId.equals("")) {
                sqlSuffix = " AND requestId='" + requestId + "'";
            }
            if (lenderName!=null && !lenderName.equals("")) {
                sqlSuffix += " AND lenderName='" + lenderName + "'";
            }
            if (status!=null && !status.equals("")) {
                sqlSuffix += " AND status='" + status + "'";
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectRfqUpdatedByLender = "select * from " + ConfigLoader.transactionTable + " where type='RFQ' AND lenderNo!=0" + sqlSuffix;
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectRfqUpdatedByLender);
            ResultSet rfqsUpdatedByLender = preparedStatement.executeQuery();

            this.targetViewingRfqs = new ArrayList<>();

            while (rfqsUpdatedByLender.next()) {
                ViewRfqQuote newViewRfqQuote = new ViewRfqQuote(
                        rfqsUpdatedByLender.getString("type"),
                        rfqsUpdatedByLender.getInt("lotNo"),
                        rfqsUpdatedByLender.getString("requestId"),
                        rfqsUpdatedByLender.getInt("versionNo"),
                        rfqsUpdatedByLender.getInt("lineNo"),
                        rfqsUpdatedByLender.getString("stockCode"),
                        rfqsUpdatedByLender.getString("borrowerName"),
                        rfqsUpdatedByLender.getInt("borrowerQty"),
                        rfqsUpdatedByLender.getString("borrowerStart"),
                        rfqsUpdatedByLender.getString("borrowerEnd"),
                        rfqsUpdatedByLender.getDouble("borrowerRate"),
                        rfqsUpdatedByLender.getString("borrowerCondition"),
                        rfqsUpdatedByLender.getInt("lenderNo"),
                        rfqsUpdatedByLender.getString("lenderName"),
                        rfqsUpdatedByLender.getInt("lenderQty"),
                        rfqsUpdatedByLender.getString("lenderStart"),
                        rfqsUpdatedByLender.getString("lenderEnd"),
                        rfqsUpdatedByLender.getDouble("lenderRate"),
                        rfqsUpdatedByLender.getString("lenderCondition"),
                        rfqsUpdatedByLender.getInt("price"),
                        rfqsUpdatedByLender.getString("status"),
                        rfqsUpdatedByLender.getString("timeStamp"),
                        rfqsUpdatedByLender.getString("updatedBy")
                );
                targetViewingRfqs.add(newViewRfqQuote);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewRfqUpdatedByLender.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewRfqUpdatedByLender.SQLException", e);
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
        LOGGER.debug("DataServices.viewRfqUpdatedByLender completed");
//        System.out.println("Size of Array=" + targetSendingQuote.size());
        return targetViewingRfqs;
    }

    public ArrayList<ViewIoi> viewIoiUpdatedByBorrower(String requestId, String lenderName, String status) {

        Connection connection = null;
        Statement statement = null;


        try {
            Class.forName("org.sqlite.JDBC");

            String sqlSuffix = "";
            if (requestId!=null && !requestId.equals("")) {
                sqlSuffix = " AND requestId='" + requestId + "'";
            }
            if (lenderName!=null && !lenderName.equals("")) {
                sqlSuffix += " AND lenderName='" + lenderName + "'";
            }
            if (status!=null && !status.equals("")) {
                sqlSuffix += " AND status='" + status + "'";
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectIoiUpdatedByBorrower = "select * from " + ConfigLoader.transactionTable + " where type='IOI' AND lenderNo!=0" + sqlSuffix;
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectIoiUpdatedByBorrower);
            ResultSet ioisUpdatedByBorrower = preparedStatement.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (ioisUpdatedByBorrower.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        ioisUpdatedByBorrower.getString("type"),
                        ioisUpdatedByBorrower.getInt("lotNo"),
                        ioisUpdatedByBorrower.getString("requestId"),
                        ioisUpdatedByBorrower.getInt("versionNo"),
                        ioisUpdatedByBorrower.getInt("lineNo"),
                        ioisUpdatedByBorrower.getString("stockCode"),
                        ioisUpdatedByBorrower.getString("borrowerName"),
                        ioisUpdatedByBorrower.getInt("borrowerQty"),
                        ioisUpdatedByBorrower.getString("borrowerStart"),
                        ioisUpdatedByBorrower.getString("borrowerEnd"),
                        ioisUpdatedByBorrower.getDouble("borrowerRate"),
                        ioisUpdatedByBorrower.getString("borrowerCondition"),
                        ioisUpdatedByBorrower.getInt("lenderNo"),
                        ioisUpdatedByBorrower.getString("lenderName"),
                        ioisUpdatedByBorrower.getInt("lenderQty"),
                        ioisUpdatedByBorrower.getString("lenderStart"),
                        ioisUpdatedByBorrower.getString("lenderEnd"),
                        ioisUpdatedByBorrower.getDouble("lenderRate"),
                        ioisUpdatedByBorrower.getString("lenderCondition"),
                        ioisUpdatedByBorrower.getInt("price"),
                        ioisUpdatedByBorrower.getString("status"),
                        ioisUpdatedByBorrower.getString("timeStamp"),
                        ioisUpdatedByBorrower.getString("updatedBy")
                );
                targetViewingIois.add(newViewIoi);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewIoiUpdatedByBorrower.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewIoiUpdatedByBorrower.SQLException", e);
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
        LOGGER.debug("DataServices.viewIoiUpdatedByBorrower completed");
//        System.out.println("Size of Array=" + targetSendingQuote.size());
        return targetViewingIois;
    }

    public ArrayList<ViewIoi> viewAllocIois(String requestId, String lenderName, String status) {

        Connection connection = null;
        Statement statement = null;


        try {
            Class.forName("org.sqlite.JDBC");

            String sqlSuffix = "";
            if (requestId!=null && !requestId.equals("")) {
                sqlSuffix = " AND requestId='" + requestId + "'";
            }
            if (lenderName!=null && !lenderName.equals("")) {
                sqlSuffix += " AND lenderName='" + lenderName + "'";
            }
            if (status!=null && !status.equals("")) {
                sqlSuffix += " AND status='" + status + "'";
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "select * from " + ConfigLoader.transactionTable + " where type='IOI' AND lenderNo!=0" + sqlSuffix;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet viewingIois = preparedStatement.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (viewingIois.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        viewingIois.getString("type"),
                        viewingIois.getInt("lotNo"),
                        viewingIois.getString("requestId"),
                        viewingIois.getInt("versionNo"),
                        viewingIois.getInt("lineNo"),
                        viewingIois.getString("stockCode"),
                        viewingIois.getString("borrowerName"),
                        viewingIois.getInt("borrowerQty"),
                        viewingIois.getString("borrowerStart"),
                        viewingIois.getString("borrowerEnd"),
                        viewingIois.getDouble("borrowerRate"),
                        viewingIois.getString("borrowerCondition"),
                        viewingIois.getInt("lenderNo"),
                        viewingIois.getString("lenderName"),
                        viewingIois.getInt("lenderQty"),
                        viewingIois.getString("lenderStart"),
                        viewingIois.getString("lenderEnd"),
                        viewingIois.getDouble("lenderRate"),
                        viewingIois.getString("lenderCondition"),
                        viewingIois.getInt("price"),
                        viewingIois.getString("status"),
                        viewingIois.getString("timeStamp"),
                        viewingIois.getString("updatedBy")
                );
                targetViewingIois.add(newViewIoi);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewAllocIois.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewAllocIois.SQLException", e);
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
        LOGGER.debug("DataServices.viewAllocIois completed");
        return targetViewingIois;
    }

    public ArrayList<ViewRfqQuote> viewReceivedRfqs(String requestId, String borrowerName, String status) {

        Connection connection = null;
        Statement statement = null;


        try {
            Class.forName("org.sqlite.JDBC");

            String sqlSuffix = "";
            if (requestId!=null && !requestId.equals("")) {
                sqlSuffix = " AND requestId='" + requestId + "'";
            }
            if (borrowerName!=null && !borrowerName.equals("")) {
                sqlSuffix += " AND borrowerName='" + borrowerName + "'";
            }
            if (status!=null && !status.equals("")) {
                sqlSuffix += " AND status='" + status + "'";
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlViewingRfqs = "select * from " + ConfigLoader.transactionTable + " where type='RFQ' AND lenderNo!=0" + sqlSuffix;
            PreparedStatement preparedStatement = connection.prepareStatement(sqlViewingRfqs);
            ResultSet viewingRfqs = preparedStatement.executeQuery();

            this.targetViewingRfqs = new ArrayList<>();

            while (viewingRfqs.next()) {
                ViewRfqQuote newViewRfqQuote = new ViewRfqQuote(
                        viewingRfqs.getString("type"),
                        viewingRfqs.getInt("lotNo"),
                        viewingRfqs.getString("requestId"),
                        viewingRfqs.getInt("versionNo"),
                        viewingRfqs.getInt("lineNo"),
                        viewingRfqs.getString("stockCode"),
                        viewingRfqs.getString("borrowerName"),
                        viewingRfqs.getInt("borrowerQty"),
                        viewingRfqs.getString("borrowerStart"),
                        viewingRfqs.getString("borrowerEnd"),
                        viewingRfqs.getDouble("borrowerRate"),
                        viewingRfqs.getString("borrowerCondition"),
                        viewingRfqs.getInt("lenderNo"),
                        viewingRfqs.getString("lenderName"),
                        viewingRfqs.getInt("lenderQty"),
                        viewingRfqs.getString("lenderStart"),
                        viewingRfqs.getString("lenderEnd"),
                        viewingRfqs.getDouble("lenderRate"),
                        viewingRfqs.getString("lenderCondition"),
                        viewingRfqs.getInt("price"),
                        viewingRfqs.getString("status"),
                        viewingRfqs.getString("timeStamp"),
                        viewingRfqs.getString("updatedBy")
                );
                targetViewingRfqs.add(newViewRfqQuote);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewReceivedRfqs.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewReceivedRfqs.SQLException", e);
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
        LOGGER.debug("DataServices.viewReceivedRfqs completed");
        return targetViewingRfqs;
    }
    
    public ArrayList<ViewRfqQuote> viewSendSelectionFromBorrowerToLender(String lenderName, String status) {

        Connection connection = null;
        Statement statement = null;


        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlSelectionFromBorrowerToLender = "select * from " + ConfigLoader.transactionTable + " where type='RFQ' AND lenderNo!=0 AND lenderName=? AND status=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectionFromBorrowerToLender);
            preparedStatement.setString(1, lenderName);
            preparedStatement.setString(2, status);
            ResultSet resultSet = preparedStatement.executeQuery();

            this.targetViewingRfqs = new ArrayList<>();

            while (resultSet.next()) {
                ViewRfqQuote newViewRfqQuote = new ViewRfqQuote(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")
                );
                targetViewingRfqs.add(newViewRfqQuote);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewSendSelectionFromBorrowerToLender.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewSendSelectionFromBorrowerToLender.SQLException", e);
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
        LOGGER.debug("DataServices.viewSendSelectionFromBorrowerToLender completed");
        return targetViewingRfqs;
    }

    public ArrayList<ViewIoi> viewSendSelectedIoiFromBorrowerToLender(String lenderName, String status) {

        Connection connection = null;
        Statement statement = null;


        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "select * from " + ConfigLoader.transactionTable + " where type='IOI' AND lenderNo!=0 AND lenderName=? AND status=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, lenderName);
            preparedStatement.setString(2, status);
            ResultSet resultSet = preparedStatement.executeQuery();

            this.targetViewingIois = new ArrayList<>();

            while (resultSet.next()) {
                ViewIoi newViewIoi = new ViewIoi(
                        resultSet.getString("type"),
                        resultSet.getInt("lotNo"),
                        resultSet.getString("requestId"),
                        resultSet.getInt("versionNo"),
                        resultSet.getInt("lineNo"),
                        resultSet.getString("stockCode"),
                        resultSet.getString("borrowerName"),
                        resultSet.getInt("borrowerQty"),
                        resultSet.getString("borrowerStart"),
                        resultSet.getString("borrowerEnd"),
                        resultSet.getDouble("borrowerRate"),
                        resultSet.getString("borrowerCondition"),
                        resultSet.getInt("lenderNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getInt("price"),
                        resultSet.getString("status"),
                        resultSet.getString("timeStamp"),
                        resultSet.getString("updatedBy")
                );
                targetViewingIois.add(newViewIoi);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewSendSelectedIoiFromBorrowerToLender.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.viewSendSelectedIoiFromBorrowerToLender.SQLException", e);
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
        LOGGER.debug("DataServices.viewSendSelectedIoiFromBorrowerToLender completed");
        return targetViewingIois;
    }

}
