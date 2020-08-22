import clients.SymBotClient;
import dataservices.*;
import model.InboundMessage;
import model.OutboundMessage;
import model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;
import utils.SymMessageParser;

import java.util.List;

public class MessageProcessor {
    private final SymBotClient botClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    public MessageProcessor(SymBotClient botClient) {
        this.botClient = botClient;
    }
    public void process(InboundMessage inboundMessage) {
        List<Long> mentions = SymMessageParser.getMentions(inboundMessage);
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String streamId = inboundMessage.getStream().getStreamId();

        if (mentions.contains(botUserInfo.getId())) {
            String cleanMessage = MessageHelper.clean(inboundMessage.getMessage());
            boolean sendHelpMessage = false;
            if (!cleanMessage.equals("")) {
                switch (cleanMessage.toLowerCase()) {
                    case "/help": {
                        sendHelpMessage = true;
                        LOGGER.debug("MessageProcessor.Commend=help evoked");
                        break;
                    }

                    case "/initializeconfig" : {
                        String msg1 = ConfigLoader.loadConfig();
                        String msg2 = DataServices.getCounterPartyList();
                        OutboundMessage messageOut = MessageSender.getInstance().buildInitializeConfigMessage(msg1, msg2);
                        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
                        LOGGER.debug("MessageProcessor.Commend=initializeconfig evoked");

                        break;
                    }
                    case "/initializesod" : {
                        String initNa = "n/a";
                        String[] msgs;
                        msgs = new String[7];
                        String msgFoot = "";

                        msgs[0] = ConfigLoader.loadConfig();
                        msgs[1] = DataBackup.backupTables(ConfigLoader.counterPartyTable);
                        msgs[2] = DataBackup.backupTables(ConfigLoader.transactionTable);
                        msgs[3] = DataInitialize.initializeTables();
                        msgs[4] = DataImport.importCsv(ConfigLoader.counterPartyTable);
                        msgs[5] = DataServices.getCounterPartyList();
                        msgs[6] = DataServices.getExtRoomIdList();
                        if (msgs[0].contains("Successful") && msgs[1].contains("Successful") && msgs[2].contains("Successful") &&
                                msgs[3].contains("Successful") && msgs[4].contains("Successful") && msgs[5].contains("Successful") && msgs[6].contains("Successful")) {
                            msgFoot = "<b> SOD Initialization: Successful! </b>";
                        }
                        OutboundMessage messageOut = MessageSender.getInstance().buildInitializeSodMessage(msgs[0], msgs[1], msgs[2], msgs[3], msgs[4], msgs[5], msgs[6], msgFoot);
                        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
                        LOGGER.debug("MessageProcessor.Commend=initializesod evoked");
                        break;

                    }
                    case "/createrfq" : {
                        System.out.println(streamId);
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/createrfq");

                        } else {
                            this.createRfqForm(inboundMessage);
                            LOGGER.debug("MessageProcessor.Commend=createrfq evoked");
                        }


                        break;
                    }
                    default: {

                    }
                }
            }
            if (sendHelpMessage) {
                // TODO


            }


        }
    }

    public void createRfqForm(InboundMessage inboundMessage) {
        MessageManager.getInstance().reset();
        MessageManager.getInstance().setRequesterName(inboundMessage.getUser().getDisplayName());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage();
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.createRfqForm completed");

    }
    public void notifyNotInRoom(InboundMessage inboundMessage, String commandText) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotInRoomMessage(commandText);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.notifyNotInRoom completed");

    }



}
