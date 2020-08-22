package dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Code for initializing Tables in the database for Japan Lending Bot
 * Masaaki Kurosawa 2020-08-10
 */

public class DataInitialize {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitialize.class);


    private static final String JOB_NAME = "DB Initialization: ";

    public static String initializeTables() {
        if (!ConfigLoader.isInitialized) {
            return JOB_NAME + "Failed (Config not initialized)";
        }

//        Variables for Indexing Tables
        String counterPartyTableIndexName = "counterPartyTableIndices";
        String transactionTableIndexName = "transactionTableIndices";
        String counterPartyTableIndexColumn1 = "counterPartyName";
        String transactionTableIndexColumn1 = "borrowerName";
        String transactionTableIndexColumn2 = "lotNo";
        String transactionTableIndexColumn3 = "requestId";
        String transactionTableIndexColumn4 = "providerNo";
        String transactionTableIndexColumn5 = "lenderName";

        Connection connection = null;
        Statement statement = null;



        try {
            // sqliteのJDBCが存在するかチェック（存在しないとClassNotFoundException）
            Class.forName("org.sqlite.JDBC");


            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("drop index if exists " + counterPartyTableIndexName);
            statement.executeUpdate("drop index if exists " + transactionTableIndexName);
            statement.executeUpdate("drop table if exists " + ConfigLoader.transactionTable);
            statement.executeUpdate("drop table if exists " + ConfigLoader.counterPartyTable);
            statement.executeUpdate("create table " + ConfigLoader.counterPartyTable + " " + ConfigLoader.counterPartySchema);
            statement.executeUpdate("create table " + ConfigLoader.transactionTable + " " + ConfigLoader.transactionSchema);
//            System.out.println("create table " + ConfigLoader.counterPartyTable + " " + ConfigLoader.counterPartySchema);
//            System.out.println("create table " + ConfigLoader.transactionTable + " " + ConfigLoader.transactionSchema);

//             create index to the tables
            String uniqueIndexCounterPartySql = "create unique index counterPartyUniqueIndex on " + ConfigLoader.counterPartyTable + "("  + counterPartyTableIndexColumn1 + ")" ;
            statement.executeUpdate(uniqueIndexCounterPartySql);
            String indexTransactionSql = "create index transactionIndices on " + ConfigLoader.transactionTable + "(" +
                    transactionTableIndexColumn1 + ", " + transactionTableIndexColumn2 + ", " + transactionTableIndexColumn3 + ", " +
                    transactionTableIndexColumn4 + ", "+ transactionTableIndexColumn5 +")" ;
            statement.executeUpdate(indexTransactionSql);

            LOGGER.debug("DataInitialize.initializeTable Indices applied");


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataInitialize.initializeTable Error:", e);
            return JOB_NAME + "Failed (JDBC Class not exist)!";
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataInitialize.initializeTable Error:", e);
            return JOB_NAME + "Failed (SQL Error)!";
        } finally {
            // ステートメントとコネクションはクローズする
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

}
