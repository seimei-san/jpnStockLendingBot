package dataservices;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;


public class DbBackup {
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
            String fileName = exportDir.toString() + "/" + tableName + "-" + Miscellaneous.getTimeStamp("backupFileName") + ".csv";

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
                ResultSet results = statement.executeQuery(sqlSelect);
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(results.getMetaData()).withQuoteMode(QuoteMode.ALL));

                if (tableName.equals(COUNTERPARTY_TABLE)) {
                    while (results.next()) {
                        csvPrinter.printRecord(
                                results.getString(1),
                                results.getString(2),
                                results.getString(3),
                                results.getString(4),
                                results.getString(5),
                                results.getInt(6)
                        );
                    }

                } else if (tableName.equals(TRANSACTION_TABLE)) {
                    while (results.next()) {
                        csvPrinter.printRecord(
                                results.getString(1),
                                results.getString(2),
                                results.getInt(3),
                                results.getString(4),
                                results.getInt(5),
                                results.getInt(6),
                                results.getString(7),
                                results.getInt(8),
                                results.getString(9),
                                results.getInt(10),
                                results.getString(11),
                                results.getString(12),
                                results.getInt(13),
                                results.getString(14),
                                results.getInt(15),
                                results.getString(16),
                                results.getString(17),
                                results.getDouble(18),
                                results.getString(19),
                                results.getString(20)
                        );
                    }

                }
                csvPrinter.flush();
                csvPrinter.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return tableName + " Table Backup: Failed (SQL Error)";
            } catch (IOException e) {
                e.printStackTrace();
                return tableName + " Table Backup: Failed (IO Error)";
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return tableName + " Table Backup: Failed (JDBC Class not exist)";
        } catch (SQLException e) {
            e.printStackTrace();
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
        return tableName + " Table Backup: Successful";

    }
}
