import clients.SymBotClient;
import dataservices.*;
import model.InboundMessage;
import model.OutboundMessage;
import model.UserInfo;
import scripts.ConfigLoader;
import utils.SymMessageParser;

import java.util.List;

public class MessageProcessor {
    private SymBotClient botClient;

    public MessageProcessor(SymBotClient botClient) {
        this.botClient = botClient;
    }
    public void process(InboundMessage inboundMessage) {
        List<Long> mentions = SymMessageParser.getInstance().getMentions(inboundMessage);
        UserInfo botUserInfo = this.botClient.getBotUserInfo();

        if (mentions.contains(botUserInfo.getId())) {
            String cleanMessage = MessageHelper.clean(inboundMessage.getMessage());
            Boolean sendHelpMessage = false;
            if (!cleanMessage.equals("")) {
                switch (cleanMessage.toLowerCase()) {
                    case "/help": {
                        sendHelpMessage = true;
                        break;
                    }

                    case "/initializeconfig" : {
                        String msg1 = ConfigLoader.loadConfig();
                        String msg2 = DataServices.getCounterPartyList();
                        OutboundMessage messageOut = MessageSender.getInstance().buildInitializeConfigMessage(msg1, msg2);
                        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
                        break;
                    }
                    case "/initializesod" : {
                        String initNa = "n/a";
                        String msg1 = initNa, msg2 = initNa, msg3 = initNa, msg4 = initNa, msg5 = initNa, msg6 = initNa;
                        String msgFoot = "";

                        msg1 = ConfigLoader.loadConfig();
                        msg2 = DbBackup.backupTables(ConfigLoader.counterPartyTable);
                        msg3 = DbBackup.backupTables(ConfigLoader.transactionTable);
                        msg4 = DbInitialize.initializeTables();
                        msg5 = DataImport.importCsv(ConfigLoader.counterPartyTable);
                        msg6 = DataServices.getCounterPartyList();
                        if (msg1.contains("Successful") && msg2.contains("Successful") && msg3.contains("Successful") && msg4.contains("Successful") && msg5.contains("Successful") && msg6.contains("Successful")) {
                            msgFoot = "<b> SOD Initialization: Successful! </b>";
                        }
                        OutboundMessage messageOut = MessageSender.getInstance().buildInitializeSodMessage(msg1, msg2, msg3, msg4, msg5, msg6, msgFoot);
                        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
                        break;

                    }
                    case "/createrfq" : {
                        this.createRfqForm(inboundMessage);
                        break;
                    }
                    default: {
                        sendHelpMessage = true;
                    }
                }
            }
            if (sendHelpMessage) {
                OutboundMessage messageOut = MessageSender.getInstance().buildHelpMessage();
                ExpenseManager.getInstance().setPersonName(inboundMessage.getUser().getDisplayName());
                MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);

            }

        }
    }

    public void createRfqForm(InboundMessage inboundMessage) {
        MessageManager.getInstance().reset();
        MessageManager.getInstance().setRequesterName(inboundMessage.getUser().getDisplayName());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRfqFormMessage();
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
    }

}
