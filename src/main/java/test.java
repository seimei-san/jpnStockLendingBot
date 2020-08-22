import clients.SymBotClient;
import dataservices.DataExports;
import dataservices.DataServices;
import dataservices.SendRfq;
import model.OutboundMessage;
import scripts.ConfigLoader;

import java.io.File;

public class test {
//    private Arraylist<> rfqs;
    private SymBotClient botClient;
    public static void main(String[] args) {

        System.out.println(ConfigLoader.loadConfig());
//        System.out.println(ConfigLoader.transactionSchema);
//        System.out.println(ConfigLoader.isInitialized);

//        System.out.println(DataBackup.backupTables(ConfigLoader.counterPartyTable));
//        System.out.println(DataBackup.backupTables(ConfigLoader.transactionTable));
//        System.out.println(DataInitialize.initializeTables());
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
//        }
        DataServices.insertRfqsForTargetCounterParty("B20082202","MTBJ",1);
        DataServices dataServices = DataServices.getInstance();
        int i = 0;
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
//            System.out.println(sendRfq.getProviderNo());
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
//        String csvName = DataExports.exportRfqsForTargetProvider("B20082103", "SYMPNY", "NKKO");
//        System.out.println(csvName);
//
// 

    }

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



}


