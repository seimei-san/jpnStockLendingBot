import clients.SymBotClient;
import dataservices.*;
import model.InboundMessage;
import model.OutboundMessage;
import model.User;
import model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;
import utils.SymMessageParser;

import java.util.ArrayList;
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
            String[] commandMessage = MessageHelper.clean(inboundMessage.getMessage());
            String cleanMessage;
            cleanMessage = commandMessage[0].trim();
            System.out.println("commandMessage1=" + cleanMessage);

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

                            // to confirm all above jobs are successful or not
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
                        // this command should not be executed in EXT chat rooms which are shared with lender.
                        // To avoid not unexpected command, check the current chat room is not EXT chat room or not.
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/newrfq");
                            LOGGER.debug("/newrfq executed in not expected EXT chat room");

                        } else {
                            this.createRfqForm(inboundMessage);
                            LOGGER.debug("MessageProcessor.Commend=newrfq evoked");
                        }
                        break;
                    }

                    case "/botcmd1" : {
                        // This command is given by IM from Borrower Bot with the 7 parameters
                        String requestId = commandMessage[1];
                        String userId = commandMessage[2];
                        String userName = Miscellaneous.convertUserName(commandMessage[3], false);
                        String borrowerName = commandMessage[4];
                        String lenderName = commandMessage[5];
                        String extChatRoomId = Miscellaneous.convertRoomId(commandMessage[6]);
                        String rfqsData = commandMessage[7];
                        DataServices.getInsertRfqsIntoTargetCounterParty(rfqsData, userName);
                        this.notifyAcceptanceRfqByLender(inboundMessage, requestId, userId, userName, borrowerName, lenderName, extChatRoomId);
                        break;
                    }

                    case "/botcmd2" : {
                        // This command is given by IM from Lender Bot with the 7 parameters
                        String requestId = commandMessage[1];
                        String userId = commandMessage[2];
                        String userName = Miscellaneous.convertUserName(commandMessage[3], false);
                        String borrowerName = commandMessage[4];
                        String lenderName = commandMessage[5];
                        String extChatRoomId = Miscellaneous.convertRoomId(commandMessage[6]);
                        String quoteData = commandMessage[7];
                        DataUpdate.getUpdateQuoteStatus(quoteData, userName, "REJECT");
                        break;
                    }

                    case "/botcmd3" : {
                        // This command is given by IM from Lender Bot with the 7 parameters
                        String requestId = commandMessage[1];
                        String userId = commandMessage[2];
                        String userName = Miscellaneous.convertUserName(commandMessage[3], false);
                        String borrowerName = commandMessage[4];
                        String lenderName = commandMessage[5];
                        String extChatRoomId = Miscellaneous.convertRoomId(commandMessage[6]);
                        String quoteData = commandMessage[7];
                        DataUpdate.getUpdateQuoteByLender(quoteData, userName, "NEW");
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

                    case "/viewrfq" : {
                        if (Miscellaneous.checkRoomId(streamId)) {
                            this.notifyNotInRoom(inboundMessage,"/submitquote");
                            LOGGER.debug("/viewrfq executed from not proper room");

                        } else {
                            this.viewRfqUpdatedByLender(inboundMessage);
                            LOGGER.debug("MessageProcessor.Commend=viewrfq evoked");
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
        String userId = inboundMessage.getUser().getUserId().toString();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(userId);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.createRfqForm executed");
    }
    public void createQuoteForm(InboundMessage inboundMessage, String lenderStatus) {
        String csvFilePath = DataExports.exportRfqsTolender(ConfigLoader.myCounterPartyName,"YET");
        String botId = String.valueOf(botClient.getBotUserId());
        String userId = inboundMessage.getUser().getUserId().toString();
//        DataUpdate.updateLenderStatus("YET", "EXPT");
        String userName = inboundMessage.getUser().getDisplayName();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "", ConfigLoader.myCounterPartyName, lenderStatus, csvFilePath, true);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.createQuoteForm executed");
    }
    public void viewRfqUpdatedByLender(InboundMessage inboundMessage) {
        String csvFilePath = DataExports.exportRfqsUpdatedByLender("","", "");
        String userName = inboundMessage.getUser().getDisplayName();
        OutboundMessage messageOut = MessageSender.getInstance().buildViewRfqFormMessage(userName, "", "", "", "", csvFilePath, false);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.viewRfqUpdatedByLender executed");
    }

    public void submitQuoteForm(InboundMessage inboundMessage, String lenderStatus) {
        String userName = inboundMessage.getUser().getDisplayName();
        String botId = String.valueOf(botClient.getBotUserId());
        String userId = inboundMessage.getUser().getUserId().toString();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "", ConfigLoader.myCounterPartyName, lenderStatus, "", false);
        MessageSender.getInstance().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        LOGGER.debug("MessageProcessor.submitQuoteForm executed");
    }

    public void notifyAcceptanceRfqByLender(InboundMessage inboundMessage, String requestId, String userId, String userName, String borrowerName, String lenderName, String extChatRoomId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildAcceptQuoteMessage(requestId, userId, userName, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(extChatRoomId, messageOut);

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
