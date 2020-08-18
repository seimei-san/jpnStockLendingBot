package scripts;
import dataservices.DataServices;
import dataservices.Rfq;

import java.util.ArrayList;
import java.util.List;

public class test {
//    private Arraylist<> rfqs;

    public static void main(String[] args) {

        System.out.println(ConfigLoader.loadConfig());
//        System.out.println(ConfigLoader.transactionSchema);
//        System.out.println(ConfigLoader.isInitialized);

//        System.out.println(DbBackup.backupTables(ConfigLoader.counterPartyTable));
//        System.out.println(DbBackup.backupTables(ConfigLoader.transactionTable));
//        System.out.println(DbInitialize.initializeTables());
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
        System.out.println(DataServices.deleteRfqsByRequestId("B20081806"));



    }



}


