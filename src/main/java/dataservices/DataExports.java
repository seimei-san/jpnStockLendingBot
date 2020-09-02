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

public class DataExports {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataExports.class);


    public static String exportRfqsForTargetlender(String requestId, String borrowerName, String lenderName) {
        String fileFullPath = "";
        Connection connection = null;
        Statement statement = null;
        boolean isError = false;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlCollectRfqsForTargetlender =
                    "SELECT type as 種別, borrowerName as 依頼元, stockCode as 銘柄, borrowerQty as 依頼数,  borrowerStart as 開始日, borrowerEnd as 終了期間, " +
                            "lenderName as 依頼先_C, requestId as 依頼番号_C, lineNo as 行番_C, lenderQty as 可能数_C, lenderStart as 可能開始_C, " +
                            "lenderEnd as 可能終了期間_C, lenderRate as 利率_C, lenderCondition as 条件_C FROM " +
                            ConfigLoader.transactionTable + " WHERE requestId=? AND borrowerName=? AND lenderName=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCollectRfqsForTargetlender);
            preparedStatement.setString(1, requestId);
            preparedStatement.setString(2, borrowerName);
            preparedStatement.setString(3, lenderName);

            File exportDir = new File(ConfigLoader.sendRfqCsvPath);

            if (!exportDir.exists()) exportDir.mkdirs();

            String exportCsvName = borrowerName + "_" + requestId + "_" + lenderName + ".csv";
            fileFullPath = exportDir.toString() + File.separator + exportCsvName;

            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileFullPath));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(resultSet.getMetaData()).withQuoteMode(QuoteMode.ALL));

                while (resultSet.next()) {
                    csvPrinter.printRecord(
                            resultSet.getString(1), //type 種別
                            resultSet.getString(2), //borrower 依頼元
                            resultSet.getString(3), //stockCode 銘柄
                            resultSet.getString(4), //borrowerQty 依頼数
                            resultSet.getString(5), //borrowerStart 開始日
                            resultSet.getString(6), //borrowerEnd 終了・期間
                            resultSet.getString(7), //lenderName  依頼先
                            resultSet.getString(8), //requestId 依頼番号
                            resultSet.getString(9), //lineNo 行番
                            resultSet.getString(10), //lenderQty 見積株数
                            resultSet.getString(11), //lenderStart 見積開始
                            resultSet.getString(12), //lenderEnd 見積返却
                            resultSet.getString(13), //lenderRate 見積利率
                            resultSet.getString(14) //lenderCondition 見積条件

                    );
                }
                csvPrinter.flush();
                csvPrinter.close();
                LOGGER.debug("DataExports.exportRfqsForTargetlender completed");

            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.error("DataExports.exportRfqsForTargetlender.SQLException",e);
                isError = true;
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("DataExports.exportRfqsForTargetlender.IOException",e);
                isError = true;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataExports.exportRfqsForTargetlender.ClassException",e);
            isError = true;

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataExports.exportRfqsForTargetlender.SQLException",e);
            isError = true;

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
        if (isError) {
            return "";
        } else {
            return fileFullPath;
        }
    }


    public static String exportRfqsTolender(String lenderName, String lenderStatus) {
        String fileFullPath = "";
        Connection connection = null;
        Statement statement = null;
        boolean isError = false;
        try {

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


            String sqlCollectRfqsTolender =
                    "SELECT type as 種別, borrowerName as 依頼元, stockCode as 銘柄, borrowerQty as 依頼数,  borrowerStart as 開始日, borrowerEnd as 終了期間, " +
                            "lenderName as 依頼先_C, requestId as 依頼番号_c, lineNo as 行番_c, lenderQty as 可能数_c, lenderStart as 可能開始_c, " +
                            "lenderEnd as 可能終了_c, lenderRate as 利率_c, lenderCondition as 条件_c, price as 価格_c FROM " +
                            ConfigLoader.transactionTable + " WHERE type='QUO' AND lenderStatus=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCollectRfqsTolender);
            preparedStatement.setString(1, lenderStatus);


            File exportDir = new File(ConfigLoader.sendRfqCsvPath);

            if (!exportDir.exists()) exportDir.mkdirs();

            String exportCsvName = lenderName + "_" + Miscellaneous.getTimeStamp("fileName") + ".csv";
            fileFullPath = exportDir.toString() + File.separator + exportCsvName;

            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileFullPath));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(resultSet.getMetaData()).withQuoteMode(QuoteMode.ALL));

                while (resultSet.next()) {
                    csvPrinter.printRecord(
                            resultSet.getString(1), //type 種別
                            resultSet.getString(2), //borrower 依頼元
                            resultSet.getString(3), //stockCode 銘柄
                            resultSet.getString(4), //borrowerQty 依頼数
                            resultSet.getString(5), //borrowerStart 開始日
                            resultSet.getString(6), //borrowerEnd 終了・期間
                            resultSet.getString(7), //lenderName  依頼先
                            resultSet.getString(8), //requestId 依頼番号
                            resultSet.getString(9), //lineNo 行番
                            resultSet.getString(10), //lenderQty 可能数
                            resultSet.getString(11), //lenderStart 可能開始
                            resultSet.getString(12), //lenderEnd 可能終了・期間
                            resultSet.getString(13), //lenderRate 利率
                            resultSet.getString(14) //lenderCondition 条件

                    );
                }
                csvPrinter.flush();
                csvPrinter.close();

            } catch (SQLException e) {
                e.printStackTrace();
                LOGGER.error("DataExports.exportRfqsTolender.SQLException",e);
                isError = true;
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("DataExports.exportRfqsTolender.IOException",e);
                isError = true;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("DataExports.exportRfqsTolender.ClassException",e);
            isError = true;

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("DataExports.exportRfqsTolender.SQLException",e);
            isError = true;

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
        if (isError) {
            LOGGER.debug("DataExports.exportRfqsTolender Error");
            return "";
        } else {
            LOGGER.debug("DataExports.exportRfqsTolender completed");
            return fileFullPath;
        }
    }


}
