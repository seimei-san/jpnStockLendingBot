package dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataImport {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataImport.class);

    private static final String JOB_NAME = "CSV import: ";

    public static String importCsv(String tableName) {

        if (!ConfigLoader.isInitialized) {
            return JOB_NAME + "Failed (config is not initialized)";
        }
        Connection connection = null;
        BufferedReader br = null;
        String line;

        try {
//            Check the target CSV file exists or not
            String fileName = ConfigLoader.importCsvPath + File.separator + tableName + ".csv";
            File file = new File(fileName);
            boolean fileExists = file.exists();
            if (!fileExists) {
                return tableName + ".csv import: Failed (the CSV file not exist)";
            }
//            connect database
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
            PreparedStatement stmt = connection.prepareStatement("insert into " + tableName + "(" + ConfigLoader.counterPartyTableSql +") values (?, ?, ?, ?, ?, ?, ?)");
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                String[] table = line.split(",");
                stmt.setString(1, table[0]);
                stmt.setString(2, table[1]);
                stmt.setString(3, table[2]);
                stmt.setString(4, table[3]);
                stmt.setString(5, table[4]);
                stmt.setString(6, table[5]);
                stmt.setString(7, table[6]);
                stmt.executeUpdate();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return tableName + ".csv import: Failed (JDBC Class not exist)";
        } catch (SQLException e) {
            e.printStackTrace();
            return tableName + ".csv import: Failed (SQL Error)";
        } catch (IOException e) {
            e.printStackTrace();
            return tableName + ".csv import: Failed (IO Error)";

        } finally {
            // ステートメントとコネクションはクローズする
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
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

        return tableName + ".csv import: Successful";
    }
}