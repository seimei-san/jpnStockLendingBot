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
            String cleanMessage = MessageHelper.clean(inboundMessage.getMessage())[0];
            String parameter1 = MessageHelper.clean(inboundMessage.getMessage())[1];
            String parameter2 = MessageHelper.clean(inboundMessage.getMessage())[2];
            boolean sendHelpMessage = false;
            String[] msgs;
            msgs = new String[7];
            if (!cleanMessage.equals("")) {
                switch (cleanMessage.toLowerCase()) {
                    case "/help": {
                        sendHelpMessage = true;
                        LOGGER.debug("MessageProcessor.Commend=help evoked");
                        break;
                    }

                    case "/updateconfig" : {
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/updateconfig");
                            LOGGER.debug("/updateconfig executed from not proper room");
                        } else {
                            msgs[0] = ConfigLoader.loadConfig();
                            msgs[1] = DataInitialize.freshCounterPartyTable();
                            msgs[2] = DataImport.importCsv(ConfigLoader.counterPartyTable);
                            msgs[3] = DataServices.getCounterPartyList();
                            OutboundMessage messageOut = MessageSender.getInstance().buildInitializeConfigMessage(msgs[0], msgs[1], msgs[2], msgs[3]);
                            MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
                            LOGGER.debug("MessageProcessor.Commend=updateconfig evoked");

                        }
                        break;

                    }
                    case "/initializesod" : {
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/initializesod");
                            LOGGER.debug("/initializesod executed from not proper room");
                        } else {
                            String initNa = "n/a";

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
                                msgFoot = "<b><i> SOD Initialization: Successful! </i></b>";
                            }
                            OutboundMessage messageOut = MessageSender.getInstance().buildInitializeSodMessage(msgs[0], msgs[1], msgs[2], msgs[3], msgs[4], msgs[5], msgs[6], msgFoot);
                            MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
                            LOGGER.debug("MessageProcessor.Commend=initializesod evoked");
                        }
                        break;
                    }
                    case "/newrfq" : {
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/createrfq");
                            LOGGER.debug("/createrfq executed from not proper room");

                        } else {
                            this.createRfqForm(inboundMessage);
                            LOGGER.debug("MessageProcessor.Commend=createrfq evoked");
                        }
                        break;
                    }

                    case "/newquote" : {
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/newquote");
                            LOGGER.debug("/newquote executed from not proper room");

                        } else {
                            this.createQuoteForm(inboundMessage, "YET");
                            LOGGER.debug("MessageProcessor.Commend=newquote evoked");
                        }
                        break;
                    }

                    case "/submitquote" : {
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/submitquote");
                            LOGGER.debug("/submitquote executed from not proper room");

                        } else {
                            this.submitQuoteForm(inboundMessage, "NEW");
                            LOGGER.debug("MessageProcessor.Commend=submitquote evoked");
                        }
                        break;
                    }


                    default: {
                        this.notifyNotUnderStandCommand(inboundMessage);
                        LOGGER.debug("MessageProcessor.Commend=DONT_KNOW evoked");
                    }

                }
            }
            if (sendHelpMessage) {
                this.sendHelp(inboundMessage);
            }


        }
    }

    public void createRfqForm(InboundMessage inboundMessage) {
        MessageManager.getInstance().reset();
        MessageManager.getInstance().setRequesterName(inboundMessage.getUser().getDisplayName());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage();
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.createRfqForm executed");
    }
    public void createQuoteForm(InboundMessage inboundMessage, String lenderStatus) {
        MessageManager.getInstance().reset();
        String csvFilePath = DataExports.exportRfqsToProvider(ConfigLoader.myCounterPartyName,"YET");
//        DataUpdate.updateLenderStatus("YET", "EXPT");
        String userName = inboundMessage.getUser().getDisplayName();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(false, userName, ConfigLoader.myCounterPartyName, lenderStatus, csvFilePath, true);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.createRfqForm executed");
    }

    public void submitQuoteForm(InboundMessage inboundMessage, String lenderStatus) {
        MessageManager.getInstance().reset();
        String userName = inboundMessage.getUser().getDisplayName();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(false, userName, ConfigLoader.myCounterPartyName, lenderStatus, "", false);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.submitQuoteForm executed");
    }

    public void notifyNotInRoom(InboundMessage inboundMessage, String commandText) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotInRoomMessage(commandText);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.notifyNotInRoom executed");
    }
    public void notifyNotUnderStandCommand(InboundMessage inboundMessage) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotUnderstandMessage();
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.notifyNotInRoom executed");
    }
    public void sendHelp(InboundMessage inboundMessage) {
        OutboundMessage messageOut = MessageSender.getInstance().buildHelpMessage();
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.sendHelp executed");
    }


}
