package dataservices;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;


public class DataBackup {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBackup.class);

    private static final String TRANSACTION_TABLE = "transactions";
    private static final String COUNTERPARTY_TABLE = "counterParties";
    private static final String JOB_NAME = "DB backup: ";

    public static String sqlSelect = "";

    public static String backupTables(String tableName) {


        if (!ConfigLoader.isInitialized) {
            return JOB_NAME + "Failed (Config is not initialized)";
        }

        Connection connection = null;
        Statement statement = null;

        try {
            // sqliteのJDBCが存在するかチェック（存在しないとClassNotFoundException）
            Class.forName("org.sqlite.JDBC");


            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            File exportDir = new File(ConfigLoader.tableBackUpPath);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            String fileName = exportDir.toString() + File.separator + tableName + "-" + Miscellaneous.getTimeStamp("fileName") + ".csv";

            if (tableName.equals(COUNTERPARTY_TABLE)) {
                 sqlSelect = "SELECT " + ConfigLoader.counterPartyTableSql + " FROM " + tableName + ";";
            } else if (tableName.equals(TRANSACTION_TABLE)) {
                 sqlSelect = "SELECT " + ConfigLoader.transactionTableSql + " FROM " + tableName + ";";
            } else {
                System.out.println("IF ERROR");
            }


            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();

            try {
                ResultSet resultSet = statement.executeQuery(sqlSelect);
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(resultSet.getMetaData()).withQuoteMode(QuoteMode.ALL));

                if (tableName.equals(COUNTERPARTY_TABLE)) {
                    while (resultSet.next()) {
                        csvPrinter.printRecord(
                                resultSet.getString("counterPartyId"),
                                resultSet.getString("counterPartyName"),
                                resultSet.getString("counterPartyType"),
                                resultSet.getString("counterPartyBotName"),
                                resultSet.getString("extChatRoomId"),
                                resultSet.getInt("isActive")
                        );
                    }

                } else if (tableName.equals(TRANSACTION_TABLE)) {
                    while (resultSet.next()) {
                        csvPrinter.printRecord(
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
                                resultSet.getString("borrowerRate"),
                                resultSet.getString("borrowerCondition"),
                                resultSet.getString("borrowerStatus"),
                                resultSet.getString("providerNo"),
                                resultSet.getString("lenderName"),
                                resultSet.getInt("lenderQty"),
                                resultSet.getString("lenderStart"),
                                resultSet.getString("lenderEnd"),
                                resultSet.getDouble("lenderRate"),
                                resultSet.getString("lenderCondition"),
                                resultSet.getString("lenderStatus"),
                                resultSet.getString("price"),
                                resultSet.getString("timeStamp"),
                                resultSet.getString("updatedBy")

                        );
                    }

                }
                csvPrinter.flush();
                csvPrinter.close();


            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.error("DataBackup.backupTables.SQLException, e");
                return tableName + " Table Backup: Failed (SQL Error)";
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("DataBackup.backupTables.IOException, e");
                return tableName + " Table Backup: Failed (IO Error)";
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataBackup.backupTables.ClassException, e");
            return tableName + " Table Backup: Failed (JDBC Class not exist)";
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataBackup.backupTables.SQLException, e");
            return tableName +  " Table Backup: Failed (SQL Error)";
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
        LOGGER.debug("DataBackup.backupTables completed");

        return tableName + " Table Backup: Successful";

    }
}
