package dataservices;

import org.jose4j.http.Get;
import org.mozilla.javascript.ast.Loop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.sql.*;
import java.util.ArrayList;


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
    
    private ArrayList<CreateRfq> targetCreateRfqs;
    private ArrayList<SendRfq> targetSendRfqs;


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


    /**
     * Create a new RequestID for a transaction type
     */
    public static String[] createRequestId(String type) {
        Connection connection = null;
        Statement statement = null;
        String returnExceptionMsg = "ID-ERROR";
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
            newRequestId = Miscellaneous.getNewRequestId("RFQ", newLotNo);
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
                                int lineNo, String stockCode, int borrowerQty, String borrowerStart, String borrowerEnd, int providerNo,  String timeStamp) {
        Connection connection = null;
        Statement statement = null;


        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//            String timeStamp = Miscellaneous.getTimeStamp("transaction");

            String sqlReserveRequest = "insert into " + ConfigLoader.transactionTable + "(borrowerName, type, lotNo, requestId, " +
                    "versionNo, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, providerNo, timeStamp) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

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
            preStatement.setInt(11, providerNo);
            preStatement.setString(12,timeStamp);

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

    public ArrayList<CreateRfq> getTargetRfqs(String requestId, int providerNo) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<CreateRfq> targetCreateRfqs = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "SELECT requestId, type, lineNo, stockCode, borrowerQty, borrowerStart, borrowerEnd, providerNo FROM "
                    + ConfigLoader.transactionTable + " WHERE requestId = " + "'" + requestId + "'" + " AND providerNo = " + providerNo + ";";
            ResultSet resultSet = statement.executeQuery(sql);


            this.targetCreateRfqs = new ArrayList<>();

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

    public static boolean deleteRfqsByRequestId(String requestId) {
        boolean result = false;
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
            LOGGER.error("DataService.deleteRfqsByRequestId.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataService.deleteRfqsByRequestId.SQLException", e);
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
        LOGGER.debug("DataService.deleteRfqsByRequestId completed");
        return true;
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

    public static void insertRfqsForTargetCounterParty(String requestId, String lenderName, int providerNo) {
        Connection connection = null;
        Statement statement = null;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            connection.setAutoCommit(false);
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


//           while (rs.next()) failed into Loop so that limit loop by the count of results
            String sqlCountTargetRfqs = "select count(requestId) as cnt from " + ConfigLoader.transactionTable + " where providerNo=0 AND requestId=" + "'" + requestId + "'";
            ResultSet rsCount = statement.executeQuery(sqlCountTargetRfqs);
            int resultCount = rsCount.getInt(1);

            String sqlGetTargetRfqs = "select * from " + ConfigLoader.transactionTable + " where providerNo=0 AND requestId=?";
            PreparedStatement preStatementSelect = connection.prepareStatement(sqlGetTargetRfqs);
            preStatementSelect.setString(1, requestId);
            ResultSet resultSet = preStatementSelect.executeQuery();

            String sqlInsertRfqForTargetCounterParty = "insert into " + ConfigLoader.transactionTable +
                    " (type, lotNo, requestId, versionNo, lineNo, stockCode, borrowerName, borrowerQty, " +
                    "borrowerStart, borrowerEnd, providerNo, lenderName, timeStamp) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                preparedStatementInsert.setInt(11, providerNo); // providerNo
                preparedStatementInsert.setString(12, lenderName); // lenderName
                preparedStatementInsert.setString(13, timeStamp); // timeStamp


                preparedStatementInsert.executeUpdate();

            }
            connection.commit();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertRfqsForTargetCounterParty.ClassException", e);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataServices.insertRfqsForTargetCounterParty.SQLException", e);
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
        LOGGER.debug("DataServices.insertRfqsForTargetCounterParty completed");

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
                        resultSet.getString("borrowerStatus"),
                        resultSet.getInt("providerNo"),
                        resultSet.getString("lenderName"),
                        resultSet.getInt("lenderQty"),
                        resultSet.getString("lenderStart"),
                        resultSet.getString("lenderEnd"),
                        resultSet.getDouble("lenderRate"),
                        resultSet.getString("lenderCondition"),
                        resultSet.getString("lenderStatus"),
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
}
