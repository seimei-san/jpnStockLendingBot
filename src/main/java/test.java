import clients.SymBotClient;
import com.google.common.base.Splitter;
import dataservices.*;
import model.OutboundMessage;
import org.glassfish.jersey.internal.guava.Lists;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.glassfish.jersey.internal.guava.Lists.*;

public class test {
//    private Arraylist<> rfqs;
    private SymBotClient botClient;
    public static void main(String[] args) {

        System.out.println(ConfigLoader.loadConfig());
//        System.out.println(ConfigLoader.transactionSchema);
//        System.out.println(ConfigLoader.isInitialized);

//        System.out.println(DataBackup.backupTables(ConfigLoader.counterPartyTable));
//        System.out.println(DataBackup.backupTables(ConfigLoader.transactionTable));
        System.out.println(DataInitialize.initializeTables());
//        System.out.println(DataImport.importCsv(ConfigLoader.counterPartyTable));
//        System.out.println(DataServices.getCounterPartyList());
//
//        for (int i=0; i <= 12; i++) {
//            System.out.println(DataServices.counterPartiesList[i]);
//    }
//        System.out.println(Miscellaneous.getTimeStamp());

//        System.out.println(Miscellaneous.convertRoomId("yzFT8r8xvhzp8Ogy1L5AK3///ox+6WB1dA=="));
//        System.out.println(GenerateElementsForm.generateReqElementsForm());
//        System.out.println(DataServices.getLastLotNo("RFQ"));
//        System.out.println(GenerateElementsForm.confirmCreatedRfq("1234 1000 0829 0930", "TEST"));
//        System.out.println(DataServices.reserveId("RFQ", "masa"));
//        System.out.println(DataServices.getLastLotNo("RFQ"));
//        System.out.println(DataServices.deleteRfqsByRequestId("B20081806"));
//        String info[] = DataServices.getCounterPartyInfo("SYPNY");
//        for (String inf : info) {
//            System.out.println(inf);
////        }
//        DataServices.insertRfqsForTargetCounterParty("B20082202","MTBJ",1);
//        DataServices dataServices = DataServices.getInstance();
//        int i = 0;
//
//        for (SendRfq sendRfq : dataServices.getSendRfqs("B20082202", "MTBJ")) {
//            System.out.println(sendRfq.getBorrowerName());
//            System.out.println(sendRfq.getType());
//            System.out.println(sendRfq.getLotNo());
//            System.out.println(sendRfq.getRequestId());
//            System.out.println(sendRfq.getVersionNo());
//            System.out.println(sendRfq.getTimeStamp());
//            System.out.println(sendRfq.getLineNo());
//            System.out.println(sendRfq.getStockCode());
//            System.out.println(sendRfq.getBorrowerQty());
//            System.out.println(sendRfq.getBorrowerStart());
//            System.out.println(sendRfq.getBorrowerEnd());
//            System.out.println(sendRfq.getlenderNo());
//            System.out.println(sendRfq.getLenderName());
//            System.out.println(sendRfq.getLenderQty());
//            System.out.println(sendRfq.getLenderStart());
//            System.out.println(sendRfq.getLenderEnd());
//            System.out.println(sendRfq.getLenderRate());
//            System.out.println(sendRfq.getLenderStart());
//            i += 1;
//
//        }
//////        System.out.println(i);
//        String csvName = DataExports.exportRfqsForTargetlender("B20082103", "SYMPNY", "NKKO");
//        System.out.println(csvName);
//
// 

//        if (DataUpdate.updateWithNothing("B20082306","ABC", "ABC", Miscellaneous.getTimeStamp("transaction"),"NONE")) {
//            System.out.println("OK");
//        } else {
//            System.out.printf("NG");
//        }
//    }

//    public static void testFileAttachment() {
//        SymBotClient botClient = SymBotClient.getBotClient();
//        OutboundMessage messageOut = new OutboundMessage();
//        messageOut.setAttachment(new File("/home/seimeisama/projects/bots/jpnStockLendingBot/src/main/resources/csvrfqs/SYMPNY_B20082103_NKKO.csv"));
//        messageOut.setMessage("test");
//
////        MessageSender.getInstance().sendMessage("yzFT8r8xvhzp8Ogy1L5AK3___ox+6WB1dA", messageOut);
//        botClient.getMessagesClient().sendMessage("yzFT8r8xvhzp8Ogy1L5AK3___ox+6WB1dA", messageOut);
//
//
//
//    }
//        String text = "asdfasdfas adsfasdf";
//
//        String[] items = new String[2];
//        String[] vals = new String[2];
//
//        vals = text.split(" ",0);
//
//        if (vals.length<=1) {
//            items[0] = vals[0];
//            items[1] = "";
//        } else {
//            items[0] = vals[0];
//            items[1] = vals[1];
//        }

//        System.out.println(DataServices.getCounterPartyInfo("MTBJ")[3]);
//
//        System.out.println(items[0].trim());
//        System.out.println(items[1].trim());
//////        System.out.println(DataServices.rfqsInTextForTargetCounterParty("B20082307","ABC",1));
//
//        String quoteData = "QUO\tNASU\t1234\t100\t901\t901\tBANANA\tB20082601\t1\t100\t901\t901\t0.01\t\t1200\n" +
//                "QUO\tNASU\t5847\t12300\t200829\t2w\tBANANA\tB20082601\t2\t12300\t200829\t2w\t0.4\toncall\t3400\n" +
//                "QUO\tNASU\t1412\t45300\t829\t3d\tBANANA\tB20082601\t3\t45300\t829\t3d\t0.03\t\t2340\n" +
//                "QUO\tNASU\t9084\t2300\t829\topen\tBANANA\tB20082601\t4\t2300\t829\topen\t0.12\t\t1410\n" +
//                "QUO\tNASU\t2356\t1800\t829\t200930\tBANANA\tB20082601\t5\t1800\t829\t200930\t0.5\toncall\t6999\n" +
//                "QUO\tNASU\t4582\t10000\t829\t1012\tBANANA\tB20082601\t6\t10000\t829\t1012\t1\t\t2310\n" +
//                "QUO\tNASU\t7685\t54300\t829\t1230\tBANANA\tB20082601\t7\t54300\t829\t1230\t2\t\t5321\n" +
//                "QUO\tNASU\t2938\t12300\t829\t1m\tBANANA\tB20082601\t8\t12300\t829\t1m\t0.3\toncall\t5842\n" +
//                "QUO\tNASU\t50131\t6000\t829\t3m\tBANANA\tB20082601\t9\t6000\t829\t3m\t0.5\t\t14321\n";
//        try (BufferedReader reader = new BufferedReader(new StringReader(quoteData))) {
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                String[] quote = line.split("\t", 0);
////                System.out.println(quote[0]);
////                System.out.println(quote[1]);
////                System.out.println(quote[2]);
////                System.out.println(quote[3]);
////                System.out.println(quote[4]);
////                System.out.println(quote[5]);
//                System.out.println(quote[6]);
//                System.out.println(quote[7]);
//                System.out.println(quote[8]);
//                System.out.println(quote[9]);
//                System.out.println(quote[10]);
//                System.out.println(quote[11]);
//                System.out.println(quote[12]);
//                System.out.println(quote[13]);
//                System.out.println(quote[14]);
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String test = DataExports.exportRfqsTolender("MIKAN","YET");
//        System.out.println(test);

//
//        Connection connection = null;
//        Statement statement = null;
//
//        try {
//            Class.forName("org.sqlite.JDBC");
//
//            connection = DriverManager.getConnection("jdbc:sqlite:" + ConfigLoader.databasePath + ConfigLoader.database);
//            statement = connection.createStatement();
//            statement.setQueryTimeout(30);  // set timeout to 30 sec.
//
//            String sqlListTargetBorrowers = "select borrowerName from " + ConfigLoader.transactionTable + " where type='QUO' AND (lenderStatus='NEW' OR lenderStatus='UPDATE') GROUP By borrowerName";
//            String sqlSelectQuoteToBorrower = "select * from " + ConfigLoader.transactionTable + " where type='QUO' AND (lenderStatus='NEW' OR lenderStatus='UPDATE') AND borrowerName=?";
//            ResultSet borrowerList = statement.executeQuery(sqlListTargetBorrowers);
//            while (borrowerList.next()) {
//                System.out.println("================================");
//                System.out.println(borrowerList.getString("borrowerName"));
//                PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectQuoteToBorrower);
//                preparedStatement.setString(1, borrowerList.getString("borrowerName"));
//                ResultSet quotesForBorrower = preparedStatement.executeQuery();
//                while (quotesForBorrower.next()) {
//                    System.out.println("--------------------------------");
//                    System.out.println(quotesForBorrower.getString("borrowerName"));
//                    System.out.println(quotesForBorrower.getString("StockCode"));
//                    System.out.println(quotesForBorrower.getInt("borrowerQty"));
//                }
//            }
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//
//            }
//
//
//
//        }

    }

}


