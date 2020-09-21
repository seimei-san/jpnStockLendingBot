import dataservices.DataExports;
import dataservices.DataServices;
import dataservices.DataUpdate;
import model.OutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.io.*;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;

import static java.nio.file.StandardWatchEventKinds.*;


public class AutoLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoLoader.class);


    public static void inputWatcher() throws Exception {

        Path dir = Paths.get(ConfigLoader.uploadCsvPath);
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, ENTRY_CREATE);
        Timer timer = new Timer();


        for (;;) {
            WatchKey watchKey = watcher.take();
            for (WatchEvent<?> event: watchKey.pollEvents()) {
                if (event.kind() == OVERFLOW) continue;
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path sourcePath = dir.resolve(name);
                String read_file =String.format("%s", sourcePath);
                String target_file = ConfigLoader.tableBackUpPath + "/" + name;

                // Timer waits to copy the data file completed.
                TimerTask task = new TimerTask() {
                    public void run() {
                        try {
                            autoLoading(read_file, target_file);
                        } catch (Exception e) {
                            System.out.println("autoLoading was failed");
                        }
                    }
                };
                timer.schedule(task, 3000);
            }
            watchKey.reset();

        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public static void autoLoading(String read_file, String target_file){
        String requestIdAndLotNo[];
        String requestId;
        int lotNo;
        try(FileReader fileReader = new FileReader(read_file);BufferedReader bufferedReader = new BufferedReader(fileReader)){

            if (read_file.toLowerCase().contains("rfq")) {
                requestIdAndLotNo = DataServices.createRequestId("RFQ");
                String botId = ConfigLoader.owner;
                String userName = "BOT";
                String userId = botId;
                String type = "RFQ";
                String timeStamp = Miscellaneous.getTimeStamp("transaction");
                int lenderNo = 0;
                requestId = requestIdAndLotNo[0];
                lotNo = Integer.parseInt(requestIdAndLotNo[1]);
                String line;
                int lineNo=1;
                String items[] = new String[4];
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("read line");
                    items = line.split(",",0);

                    DataServices.insertRfq(ConfigLoader.myCounterPartyName, type, lotNo, requestId, 0, lineNo,
                            items[0], Integer.parseInt(items[1]), items[2], items[3], lenderNo, timeStamp, userName);
                    lineNo += 1;
                }

                OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(botId, userId , userName, requestId, ConfigLoader.myCounterPartyName, "", false, false);
                MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(ConfigLoader.intChatRoomId), messageOut);
                LOGGER.debug("AutoLoader.autoLoading.rfqFileUpload completed");

            } else {
                if (read_file.toLowerCase().contains("quote")) {
                    String botId = ConfigLoader.owner;
                    String userName = "BOT";
                    String userId = botId;
                    String fromStatus = "YET";
                    String toStatus = "NEW";
                    String csvFilePath = "";
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] items = new String[11];
                        String[] quote = line.split(",", 0);
                        items[0] = quote[1]; // borrowerName
                        items[1] = quote[8]; // lenderName
                        items[2] = quote[9]; // requestId
                        items[3] = quote[10]; // lineNo
                        items[4] = quote[11]; // lenderQty
                        items[5] = quote[12];    // lenderStart
                        items[6] = quote[13];    // lenderEnd
                        if (quote[14] == null || quote[14].isEmpty()) {
                            items[7] = "0.0";
                        } else {
                            items[7] = quote[14]; // lenderRate
                        }
                        items[8] = quote[15];    // lenderCondition
                        if (quote[16] == null || quote[16].isEmpty()) {
                            items[9] = "0";
                        } else {
                            items[9] = quote[16];    // price
                        }
                        items[10] = quote[17];    // Status

                        DataUpdate.updateQuote(userName, fromStatus, items[0], items[1], items[2],
                                Integer.parseInt(items[3]), Integer.parseInt(items[4]), items[5], items[6],
                                Double.parseDouble(items[7]), items[8], Integer.parseInt(items[9]), toStatus);
                    }

                    OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "",ConfigLoader.myCounterPartyName, toStatus, csvFilePath, false);
                    MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(ConfigLoader.intChatRoomId), messageOut);
                    LOGGER.debug("AutoLoader.autoLoading.quoteFileUpload completed");


                } else if (read_file.toLowerCase().contains("select")) {
                    String botId = ConfigLoader.owner;
                    String userName = "BOT";
                    String userId = botId;
                    String fromStatus = "YET";
                    String toStatus = "NEW";
                    String csvFilePath = "";
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        String[] items = new String[9];
                        String[] quote = line.split(",", 0);
                        items[0] = quote[1]; // borrowerName
                        items[1] = quote[8]; // lenderName
                        items[2] = quote[9]; // requestId
                        items[3] = quote[10]; // lineNo
                        items[4] = quote[11]; // lenderQty
                        items[5] = quote[12];    // lenderStart
                        items[6] = quote[13];    // lenderEnd
                        items[7] = quote[14];  // lenderRate
                        items[8] = quote[15];  // lenderCondition
                        DataUpdate.updateSelection(userName, items[0], items[1], items[2], Integer.parseInt(items[3]), Integer.parseInt(items[4]), items[5], items[6], Double.parseDouble(items[7]), items[8], "SELECT") ;
                    }

                    csvFilePath = DataExports.exportRfqsUpdatedByLender(null,null, "SELECT");
                    OutboundMessage messageOut = MessageSender.getInstance().buildViewRfqFormMessage(userName, "", "", "", "SELECT", csvFilePath, true);
                    MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(ConfigLoader.intChatRoomId), messageOut);
                    LOGGER.debug("AutoLoader.autoLoading.selectFileUpload completed");


                }
                bufferedReader.close();
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                Path sourcePath = Paths.get(read_file);
                Path targetPath = Paths.get(target_file);
                File sourceFile = new File(read_file);
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            if (sourceFile.exists()) {
                    sourceFile.delete();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized void wtWait() throws InterruptedException {
        wait(5000);
    }

    public synchronized void wtNotify() {
        notify();
    }



}
