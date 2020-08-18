package dataservices;

import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class DataServices {
    private static DataServices instance;
    public static DataServices getInstance() {
        if (instance == null) {
            instance = new DataServices();
        }
        return instance;
    }
    public static String[] counterPartiesList;

    private ArrayList<Rfq> targetRfqs;


    public static String getCounterPartyList() {
        /**
         * Get Counter Party List into the public static counterParties to list the options in Provider
         * @return
         */
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
            return JOB_NAME + "Failed (JDBC Class not exist)";
        } catch (SQLException e) {
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
        return JOB_NAME + "Successful";
    }

    /**
     * Create a new RequestID for a transaction type
     */
    public static String[] createRequestId(String type) {
        Connection connection = null;
        Statement statement = null;
        String returnExceptionMsg = "ID-ERROR";
        String requestIdAndLotNo[] = new String[2];

        int lastLotNo = 0;
        int newLotNo = 0;
        String newRequestId = "";

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
//            String timeStamp = Miscellaneous.getTimeStamp("transaction");

//            String sqlReserveRequest = "insert into transactions(type,lotNo,requestId,updatedBy,timeStamp) VALUES(?,?,?,?,?)";
//
//            PreparedStatement preStatement = connection.prepareStatement(sqlReserveRequest);
//            preStatement.setString(1,type);
//            preStatement.setInt(2,newLotNo);
//            preStatement.setString(3,newRequestId);
//            preStatement.setString(4,user);
//            preStatement.setString(5,timeStamp);
//            preStatement.executeUpdate();


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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

        return requestIdAndLotNo;
    }

    public static void insertRfq(String counterParty, String type, int lotNo, String requestId, int noOfBlast, int versionNo,
                                 String timeStamp, int lineNo, String stock, int qty, String start, String end, int providerNo) {
        Connection connection = null;
        Statement statement = null;


        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//            String timeStamp = Miscellaneous.getTimeStamp("transaction");

            String sqlReserveRequest = "insert into " + ConfigLoader.transactionTable + "(counterPartyNameRequester, type, lotNo, requestId, noOfBlast, " +
                    "versionNo, timeStamp, lineNo, stockCode, requesterQty, requesterStart, requesterEnd, providerNo) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement preStatement = connection.prepareStatement(sqlReserveRequest);
            preStatement.setString(1,counterParty);
            preStatement.setString(2,type);
            preStatement.setInt(3,lotNo);
            preStatement.setString(4,requestId);
            preStatement.setInt(5,noOfBlast);
            preStatement.setInt(6,versionNo);
            preStatement.setString(7,timeStamp);
            preStatement.setInt(8,lineNo);
            preStatement.setString(9,stock);
            preStatement.setInt(10,qty);
            preStatement.setString(11,Miscellaneous.getInstance().fourDigitDate(start));
            preStatement.setString(12,Miscellaneous.getInstance().fourDigitDate(end));
            preStatement.setInt(13, providerNo);

            preStatement.executeUpdate();


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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

    }

    public ArrayList<Rfq> getTargetRfqs(String requestId, int providerNo) {

        Connection connection = null;
        Statement statement = null;
        ArrayList<Rfq> targetRfqs = new ArrayList<Rfq>();
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sql = "SELECT requestId, type, lineNo, stockCode, requesterQty, requesterStart, requesterEnd, providerNo FROM "
                    + ConfigLoader.transactionTable + " WHERE requestId = " + "'" + requestId + "'" + " AND providerNo = " + providerNo + ";";
            ResultSet resultSet = statement.executeQuery(sql);

            String targetRfq[];
            targetRfq = new String[8];

            this.targetRfqs = new ArrayList<>();

            while (resultSet.next()) {
                Rfq newRfq = new Rfq(resultSet.getString("requestId"),resultSet.getString("type"),
                        resultSet.getInt("lineNo"), resultSet.getString("stockCode"),
                        resultSet.getInt("requesterQty"), resultSet.getString("requesterStart"), resultSet.getString("requesterEnd"));
                targetRfqs.add(newRfq);

            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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
            return targetRfqs;
        }
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

            String sqlDeleteByRequestId = "delete from " + ConfigLoader.transactionTable + " where requestId =  ?";

            PreparedStatement preStatement = connection.prepareStatement(sqlDeleteByRequestId);
            preStatement.setString(1,requestId);

            preStatement.executeUpdate();


        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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
            return true;
        }
    }
}
