import clients.SymBotClient;
import dataservices.DataServices;
import dataservices.DataExports;
import dataservices.DataUpdate;
import model.InboundMessage;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;


public class ActionProcessor {
    final private SymBotClient botClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionProcessor.class);


    public ActionProcessor(SymBotClient botClient) {
        this.botClient = botClient;
    }


    public void process(SymphonyElementsAction action, User user) {

        Map<String, Object> formValues = action.getFormValues();
        switch (action.getFormId()) {

            case "create-rfq-form": {
                // Form for creating RFQ by pasting the copied RFQ data [INTERNAL CHAT ROOM AT BORROWER]
                if (formValues.get("action").equals("import-rfq-button")) {
                    // Import the pasted RFQ data into table "transactions"
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    int lotNo;
                    String[] requestIdAndLotNo;
                    String requestId;
                    if (formValues.get("request_id").equals(ConfigLoader.NO_REQUESTID)) {
                        // create a new unique Request ID for the day for new RFQ
                        requestIdAndLotNo = DataServices.createRequestId("RFQ");
                        requestId = requestIdAndLotNo[0];
                        lotNo = Integer.parseInt(requestIdAndLotNo[1]);
                    } else {
                        // if not new RFQ (normally re-create RFQ with existing Request ID, extra LineNo from Request ID
                        requestId = (String)formValues.get("request_id");
                        lotNo = Integer.parseInt(requestId.substring(requestId.length()-2));    
                    }
                    this.manageAddRfqForm(action, userId, userName, requestId, lotNo);
//                    this.sendTestMessage(action);
                }
                break;
            }

            case "submit-rfq-form" : {
                // after RFQ imported, this form give user this function to submit RFQ to selected Lenders [Borrower Internal Chat Room
                if (formValues.get("action").equals("recreate-rfq-button")) {
//                  Maintain the Request ID but delete the transactions with the Request ID
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    String requestId = (String)formValues.get("request_id");
                    if (DataServices.deleteRfqsByRequestId(requestId)) {
                        this.reCreateRfqForm(action, userId, userName, requestId);

                    } else {
                        this.noticeError(action);
                    }

                } else if (formValues.get("action").equals("cancel-rfq-button")) {
                    // Delete the transactions for the Request ID and return the simple text message "Cancelled" [INTERNAL CHAT ROOM AT BORROWER]
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    String requestId =  (String)formValues.get("request_id");
                    DataServices.deleteRfqsByRequestId(requestId);
                    this.noticeCancelCompletion(action, userId, userName, requestId);

                } else if (formValues.get("action").equals("send-rfq-button")) {
                    // fetch Lender Names from Elements pull-down boxes (max 5 boxes)   [INTERNAL CHAT ROOM AT BORROWER]
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    String requestId = (String)formValues.get("request_id");
                    String borrowerName = (String)formValues.get("counterparty_borrower");
                    String lenderName1 = (String)formValues.get("lender1-select");
                    String lenderName2 = (String)formValues.get("lender2-select");
                    String lenderName3 = (String)formValues.get("lender3-select");
                    String lenderName4 = (String)formValues.get("lender4-select");
                    String lenderName5 = (String)formValues.get("lender5-select");
                    
                    // send RFQ to each selected Lender
                    if (lenderName1!=null) {
                        requestId = (String)formValues.get("request_id");
                        blastSendReqForm(userId, userName, requestId, borrowerName,1, lenderName1, true);
                    } else {
                        this.noticeNoCounterParty(action);
                        this.manageResendRfqForm(action, userId, userName, requestId);
                    }
                    if (lenderName2!=null) {
                        blastSendReqForm(userId, userName, requestId, borrowerName,2, lenderName2, true);
                    }
                    if (lenderName3!=null) {
                        blastSendReqForm(userId, userName, requestId, borrowerName,3, lenderName3, true);
                    }
                    if (lenderName4!=null) {
                        blastSendReqForm(userId, userName, requestId, borrowerName,4, lenderName4, true);

                    }
                    if (lenderName5!=null) {
                        blastSendReqForm(userId, userName, requestId, borrowerName,5, lenderName5, true);

                    }
                }
                break;
            }

            case "receive-rfq-form" : {
//                After Borrower send RFQ to a target Lender(s), this form is used in the External Chat Room where Borrower and Lender work together.
//                The actions here expects that the lender in the External Chat Room initiates.
                String requestId = (String)formValues.get("request_id");
                String userId = String.valueOf(user.getUserId());
                String userName = String.valueOf(user.getDisplayName());
                String botId = (String)formValues.get("bot_id");
                String lenderBotInstantMessageId = (String)formValues.get("lenderbot_im_id");
                String externalChatRoomId = (String)formValues.get("external_chatroom_id");
                String borrowerName = (String)formValues.get("counterparty_borrower");
                String lenderName = (String)formValues.get("counterparty_lender");
                String rfqsData = (String)formValues.get("rfqs_data");
                String timeStamp = Miscellaneous.getTimeStamp("transaction");
                if (formValues.get("action").equals("accept-rfq-button")) {
                    // Lender Bot will catch the command and the RFQ data and then insert the RFQ data into Lender Database.
                    // Once the RFQ data is inserted, Lender Bot send a notice of the acceptance to External chat room
                    this.sendImToLenderBot("/botcmd1", userId, userName, botId, lenderBotInstantMessageId, externalChatRoomId, requestId, borrowerName, lenderName, rfqsData);
                    // Update transaction status in Borrower Database
                    DataUpdate.updateBorrowerTransactionStatusByRequestId("RFQ", requestId, lenderName, "", "ACK", userName,  timeStamp);
                }
                if (formValues.get("action").equals("nothing-quote-button")) {
//                    Instantly return No Inventory from Lender to Borrower in case of when it is obvious for Lender not to have any inventory
//                    [EXTERNAL CHAT ROOM OPERATED BY LENDER ONLY]
                    this.replyRfqNothing(action, userId, userName, requestId, borrowerName, lenderName);
                }
                break;
            }

            case "create-quote-form" : {
//                Import the copied/pasted quote data into the table "transactions"
//                [INTERNAL CHAT ROOM AT LENDER]
                if (formValues.get("action").equals("import-quote-button")) {
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    this.manageAddQuoteForm(action, userId, userName,"YET", "NEW", "");
                }
                break;
            }

            case "submit-quote-form" : {
//                Submit the imported quote data from Lender to Borrower
//                [INTERNAL CHAT ROOM AT LENDER]
                if (formValues.get("action").equals("recreate-quote-button")) {
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    String csvFilePath = DataExports.exportRfqsTolender(ConfigLoader.myCounterPartyName,"NEW");
                    this.blastSendQuoteForm(action, userId, userName, "NEW", "NEW", csvFilePath, true);
                } else if (formValues.get("action").equals("send-quote-button")) {
//                    get list of borrowerNames
//                    while loop to select quote per borrower and then send selected quote to EXT room for facing each borrower
//                    update lenderStatus from NEW to "SEND"
//                    display the form with lenderStatus = SEND
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    String lenderName = ConfigLoader.myCounterPartyName;
                    this.blastSendQuoteToBorrowerForm(action, userId, userName, lenderName,"NEW","SEND", true);
                }
                break;
            }

            case "receive-quote-form" : {
//                This form is submitted by bot at lender
//                The actions expects by Borrower, not lender
//                but update the DB on both Lender and Borrower
//                [EXTERNAL CHAT ROOM OPERATED BY BORROWER ONLY]
                String userId = String.valueOf(user.getUserId());
                String userName = String.valueOf(user.getDisplayName());
                String botId = (String)formValues.get("bot_id");
                String borrowerBotInstantMessageId = (String)formValues.get("borrowerbot_im_id");
                String externalChatRoomId = (String)formValues.get("external_chatroom_id");
                String borrowerName = (String)formValues.get("counterparty_borrower");
                String lenderName = (String)formValues.get("counterparty_lender");
                String quoteData = (String)formValues.get("quote_data");

                if (formValues.get("action").equals("reject-quote-button")) {

                    // send the notification of this reject in External Chat Room
                    // update the transactions at Lender with status=REJECT
                    // update the transactions at borrower with status=REJECT (need to send IM to Borrower Bot)

                    this.sendImToBorrowerBotForReject(userId, userName, botId, borrowerBotInstantMessageId, externalChatRoomId, borrowerName, lenderName, quoteData);
                    DataUpdate.getUpdateQuoteStatus(quoteData, userName, "REJECT");
                    this.notifyRejectQuoteToBorrowerForm(action, userId, userName, borrowerName, lenderName, externalChatRoomId);


                } else if (formValues.get("action").equals("accept-quote-button")) {
                    this.sendImToBorrowerBotForAccept(userId, userName, botId, borrowerBotInstantMessageId, externalChatRoomId, borrowerName, lenderName, quoteData);
                    DataUpdate.getUpdateQuoteStatus(quoteData, userName, "ACK");
                    this.notifyAcceptQuoteToBorrowerForm(action, userId, userName, borrowerName, lenderName, externalChatRoomId);
                }

            }

            case "view-rfq-form" : {
                String requestId = (String)formValues.get("request-id-select");
                String lenderName = (String)formValues.get("lender-select");
                String status = (String)formValues.get("status-select");
                if (formValues.get("action").equals("refresh-rfq-button")) {
                    this.viewRfqUpdatedByLender(action, user, requestId, lenderName, status);
                }
                break;
            }

            case "import-selection-form" : {
                String userName = user.getDisplayName();
                String userId = user.getUserId().toString();
                if (formValues.get("action").equals("import-selection-button")) {
                    this.manageImportSelectForm(action, userId, userName,"SELECT");

                } else if (formValues.get("action").equals("proceed-selection-button")) {
                    this.manageSelectionForm(action, userId, userName, "SELECT");
                }
                break;
            }

            case "send-selection-form" : {
                String botId = "";
                String userId = user.getUserId().toString();
                
                String userName = user.getDisplayName();
                String timeStamp = Miscellaneous.getTimeStamp("transaction");
                if (formValues.get("action").equals("send-selection-button")) {
                    // send a form to EXT chat room and ask Lender to accept
                    // update borrower trans table status from SELECT to SEND
                    this.manageSelectionToLenderForm(action, userId, userName, "SELECT", "SEND");

                } else if (formValues.get("action").equals("cancel-selection-button")) {
                    DataUpdate.updateSelectionStatus(userName, timeStamp, "SELECT", "NEW");
                    this.manageSelectionForm(action, userId, userName, "NEW");

                }
                break;
            }

            case "confirm-selection-form" : {
                String userId = String.valueOf(user.getUserId());
                String userName = String.valueOf(user.getDisplayName());
                String botId = (String)formValues.get("bot_id");
                String lenderBotInstantMessageId = (String)formValues.get("lenderbot_im_id");
                String externalChatRoomId = (String)formValues.get("external_chatroom_id");
                String requestId = "DUMMY";
                String borrowerName = (String)formValues.get("counterparty_borrower");
                String lenderName = (String)formValues.get("counterparty_lender");
                String selectionData = (String)formValues.get("selection_data");
                if (formValues.get("action").equals("confirm-selection-button")) {
                    // send the selection data in textarea to Lending Bot via IM
                    // Lending Bot will update the selected quotation with status=DONE based on the data via IM
                    // Borrow Bot will update the selected transaction status=DONE
                    this.sendImToLenderBot("/botcmd4", userId, userName, botId, lenderBotInstantMessageId,externalChatRoomId, requestId, borrowerName, lenderName, selectionData);
                    DataUpdate.getUpdateSelectionStatus(selectionData, userName, "DONE");
                    this.notifyAcceptSelectionForm(action, userId, userName, borrowerName, lenderName, externalChatRoomId);


                }
                break;
            }

            case "view-quote-form" : {
                String requestId = (String)formValues.get("request-id-select");
                String borrowerName = (String)formValues.get("borrower-select");
                String status = (String)formValues.get("status-select");
                if (formValues.get("action").equals("refresh-quote-button")) {
                    this.viewRfqUpdatedByBorrower(action, user, requestId, borrowerName, status);
                }
                break;
            }


            case "notify-nothing-form" : {
                String userName = user.getDisplayName();
                String requestId = (String)formValues.get("request_id");
                String borrowerName = (String)formValues.get("counterparty_borrower");
                String lenderName = (String)formValues.get("counterparty_lender");
                String timeStamp = Miscellaneous.getTimeStamp("transaction");
                DataUpdate.updateWithNothing("RFQ", requestId, lenderName, userName,timeStamp,"NONE");
                this.acceptRfqNothing(action, userName, requestId, borrowerName, lenderName);

                break;
            }


        }
    }



    public void manageAddRfqForm(SymphonyElementsAction action, String userId, String userName, String requestId, int lotNo) {

        Map<String, Object> formValues = action.getFormValues();
        String textRfq = (String) formValues.get("inputRfq");

        final int maxFieldNo = 4;

        String[] fieldValues = new String[maxFieldNo];

        int len = textRfq.length();
        int loopCount = 1;

        int recordCount = 0;
        int fieldNo = 0;

        String bufferMojis = "";
        final String type = "RFQ";
        String stockCode;
        int lineNo;
        int borrowerQty;
        String borrowerStart;
        String borrowerEnd;
        final int lenderNo = 0;

        boolean notError = true;


        try {
            for (char moji : textRfq.toCharArray()) {

                // 9=tab, 32=space; 44=comma
                if (((int) moji == 9 || (int) moji == 32 || (int) moji == 44) && fieldNo <= maxFieldNo) {
                    // connecting chars to get a value in the fields
                    fieldValues[fieldNo] = bufferMojis;
                    fieldNo += 1;
                    bufferMojis = "";
                } else {
                    if (((int) moji == 10 && fieldNo > 0) || loopCount == len) {
                        if ((int) moji != 10) {
                            bufferMojis = bufferMojis + String.valueOf(moji);
                        }
                        fieldValues[fieldNo] = bufferMojis;
                        fieldNo = 0;
                        bufferMojis = "";
                        recordCount += 1;

                        lineNo = recordCount;
                        stockCode = fieldValues[0];
                        if (Miscellaneous.isNumber(fieldValues[1]) || !fieldValues[1].isEmpty()) {
                            borrowerQty = Integer.parseInt(fieldValues[1]);

                        } else {
                            borrowerQty = 0;
                        }
                        borrowerStart = fieldValues[2];
                        borrowerEnd = fieldValues[3];

                        DataServices.insertRfq(ConfigLoader.myCounterPartyName, type, lotNo, requestId, 0, lineNo,
                                stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo, Miscellaneous.getTimeStamp("transaction"), userName);

                    } else {
                        bufferMojis = bufferMojis + String.valueOf(moji);
                    }

                }
                loopCount += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("ActionProcessor.manageAddRfqForm.Exception", e);
            notError = false;
            DataServices.deleteRfqsByRequestId(requestId);
            this.noticeDoubtInput(action);

        }
        if (notError) {
            String botId = String.valueOf(botClient.getBotUserId());
            OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(botId, userId, userName, requestId, ConfigLoader.myCounterPartyName, "", false, false);
            MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
            LOGGER.debug("ActionProcessor.manageAddRfqForm completed");
        }

    }

    public void manageImportSelectForm(SymphonyElementsAction action, String userId, String userName, String status) {

        Map<String, Object> formValues = action.getFormValues();
        String selectData = (String) formValues.get("inputSelection");

        boolean notError = true;

        try (BufferedReader reader = new BufferedReader(new StringReader(selectData))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = new String[3];
                String[] quote = line.split("\t", 0);
                items[0] = quote[6]; // lenderName
                items[1] = quote[7]; // requestId
                items[2] = quote[8]; // lineNo
                DataUpdate.updateSelection(userName, items[0], items[1], Integer.parseInt(items[2]), "SELECT") ;
            }
            if (notError) {
                String botId = String.valueOf(botClient.getBotUserId());
                String csvFilePath = DataExports.exportRfqsUpdatedByLender(null,null, "SELECT");
                OutboundMessage messageOut = MessageSender.getInstance().buildViewRfqFormMessage(userName, "", "", "", "SELECT", csvFilePath, true);
                MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
                LOGGER.debug("ActionProcessor.manageAddQuoteForm completed");
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("ActionProcessor.manageAddQuoteForm.Exception", e);
            notError = false;
            this.noticeDoubtInput(action);
        }
    }

    public void manageSelectionForm(SymphonyElementsAction action, String userId, String userName, String status) {
        String csvFilePath = DataExports.exportRfqsUpdatedByLender(null,null, status);
        OutboundMessage messageOut = MessageSender.getInstance().buildViewRfqFormMessage(userName, "", "", "", status, csvFilePath, true);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageSelectionForm completed");

    }
    
    public void manageSelectionToLenderForm(SymphonyElementsAction action, String userId, String userName, String fromStatus, String toStatus) {

        String selectionData;
        ArrayList<String> lenderNames = DataServices.getInstance().listLenderNames(fromStatus);

        for (String lenderName : lenderNames) {
            selectionData = DataServices.getInstance().getSelectionForLenderToTextarea(lenderName, fromStatus, toStatus);
            String[] lenderNameInfo = DataServices.getCounterPartyInfo(lenderName);
            String borrowerName = ConfigLoader.myCounterPartyName;
            String botId = lenderNameInfo[3];
            String externalChatRoomId = lenderNameInfo[4];
            String lenderBotInstantMessageId = lenderNameInfo[5];
            OutboundMessage messageOut = MessageSender.getInstance().buildSendSelectionFormMessage(botId, userId, userName, externalChatRoomId, lenderBotInstantMessageId, borrowerName, lenderName, fromStatus,  selectionData);
            MessageSender.getInstance().sendMessage(externalChatRoomId, messageOut);
            DataUpdate.updateStatusAfterSentSelection(lenderName, fromStatus, toStatus);
        }
        LOGGER.debug("ActionProcessor.manageSelectionToLenderForm completed");
    }

    public void manageAddQuoteForm(SymphonyElementsAction action, String userId, String userName, String fromStatus, String toStatus, String csvFilePath) {

        Map<String, Object> formValues = action.getFormValues();
        String quoteData = (String) formValues.get("inputQuote");

        final String type = "RFQ";

        boolean notError = true;

        try (BufferedReader reader = new BufferedReader(new StringReader(quoteData))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = new String[10];
                String[] quote = line.split("\t", 0);
                items[0] = quote[6]; // lenderName
                items[1] = quote[7]; // requestId
                items[2] = quote[8]; // lineNo
                items[3] = quote[9]; // lenderQty
                items[4] = quote[10];    // lenderStart
                items[5] = quote[11];    // lenderEnd
                items[6] = quote[12];    // lenderRate
                items[7] = quote[13];    // lenderCondition
                items[8] = quote[14];    // price
                items[9] = quote[15];    // Status

                DataUpdate.updateQuote(userName, fromStatus, items[0], items[1],
                        Integer.parseInt(items[2]), Integer.parseInt(items[3]), items[4], items[5],
                                Double.parseDouble(items[6]),items[7],Integer.parseInt(items[8]), toStatus) ;
            }
            if (notError) {
                String botId = String.valueOf(botClient.getBotUserId());
                OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "",ConfigLoader.myCounterPartyName, toStatus, csvFilePath, false);
                MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
                LOGGER.debug("ActionProcessor.manageAddQuoteForm completed");
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("ActionProcessor.manageAddQuoteForm.Exception", e);
            notError = false;
            this.noticeDoubtInput(action);
        }
    }

    public void blastSendReqForm(String userId, String userName, String requestId, String borrowerName, int lenderNo, String lenderName, boolean isSent) {
        String rfqsData = DataServices.insertGetRfqsForTargetCounterParty("RFQ", userName, requestId, lenderName, lenderNo);
//        String csvFilePath = DataExports.exportRfqsForTargetlender(requestId, borrowerName,lenderName);
        String[] counterPartyInfo = DataServices.getCounterPartyInfo(lenderName);
        String botId = counterPartyInfo[3];
//        String botId = String.valueOf(botClient.getBotUserId());
        String externalChatRoomId = counterPartyInfo[4];
        String lenderBotInstantMessageId = counterPartyInfo[5];
        OutboundMessage messageOut = MessageSender.getInstance().buildSendRfqFormMessage(botId, userId, lenderBotInstantMessageId, externalChatRoomId, rfqsData, userName, requestId, borrowerName, lenderName, "", isSent);
        MessageSender.getInstance().sendMessage(externalChatRoomId, messageOut);
        LOGGER.debug("ActionProcessor.blastSendReqForm completed");
    }

    public void sendImToLenderBot(String cmd, String userId, String userName, String botId, String lenderBotInstantMessageId, String externalChatRoomId, String requestId, String borrowerName, String lenderName, String rfqsData) {
        userName = Miscellaneous.convertUserName(userName, true);
        OutboundMessage messageOut = MessageSender.getInstance().buildImToLenderBot(cmd, userId, userName, botId, externalChatRoomId, requestId, borrowerName, lenderName, rfqsData);
        MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(lenderBotInstantMessageId), messageOut);
    }

    public void blastSendQuoteForm(SymphonyElementsAction action, String userId, String userName, String fromStatus, String toStatus, String csvFilePath, boolean isNew) {
        String botId = String.valueOf(botClient.getBotUserId());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "", ConfigLoader.myCounterPartyName, toStatus, csvFilePath, isNew);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.blastSendQuoteForm completed");
    }

    public void blastSendQuoteToBorrowerForm(SymphonyElementsAction action, String userId, String userName, String lenderName, String fromStatus, String toStatus, boolean isNew) {
        String quoteData;
        ArrayList<String> borrowerNames = DataServices.getInstance().listBorrowerNames();

        for (String borrowerName : borrowerNames) {
            quoteData = DataServices.getInstance().getQuoteForBorrowerToTextarea(borrowerName, fromStatus, toStatus);
            String[] borrowerNameInfo = DataServices.getCounterPartyInfo(borrowerName);
            String botId = borrowerNameInfo[3];
            String externalChatRoomId = borrowerNameInfo[4];
            String borrowerBotInstantMessageId = borrowerNameInfo[5];
            OutboundMessage messageOut = MessageSender.getInstance().buildSendQuoteFormMessage(botId, userId, userName, externalChatRoomId, borrowerBotInstantMessageId, borrowerName, lenderName, toStatus,  quoteData, isNew);
            MessageSender.getInstance().sendMessage(externalChatRoomId, messageOut);
            DataUpdate.updateLenderStatusAfterSentQuote(borrowerName, fromStatus, toStatus);
        }
    }

    public void manageResendRfqForm(SymphonyElementsAction action, String userId, String userName, String requestId) {
        String botId = String.valueOf(botClient.getBotUserId());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(botId, userId, userName, requestId, "", ConfigLoader.myCounterPartyName,false, false);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageResendRfqForm completed");
    }

    public void manageCancelRfqForm(SymphonyElementsAction action, User user) {
        Map<String, Object> formValues = action.getFormValues();
        String userId = user.getUserId().toString();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(userId);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageCancelRfqForm completed");
    }

    public void reCreateRfqForm(SymphonyElementsAction action, String userId, String userName, String requestId) {
        String botId = String.valueOf(botClient.getBotUserId());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(botId, userId,
                userName, requestId, ConfigLoader.myCounterPartyName, "", false, true);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.reCreateRfqForm completed");
    }

    public void noticeNotAllowYou(SymphonyElementsAction action, String allowedCounterPartyName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotAllowYouMessage(allowedCounterPartyName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeNowAllowYou completed");
    }

    public void replyRfqNothing(SymphonyElementsAction action, String userId, String userName, String requestId, String borrowerName, String lenderName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNothingMessage(userId, userName, requestId, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.replyRfqNothing completed");
    }

    public void noticeAcceptQuote(SymphonyElementsAction action, String userId, String userName, String requestId, String borrowerName, String lenderName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildAcceptQuoteMessage(requestId, userId, userName, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.notifyAcceptQuote completed");
    }

    public void acceptRfqNothing(SymphonyElementsAction action, String userName, String requestId, String borrowerName, String lenderName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildAcceptNothingMessage(requestId, userName, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.acceptRfqNothing completed");
    }

    public void notifyRejectQuoteToBorrowerForm(SymphonyElementsAction action, String userId, String userName, String borrowerName, String lenderName, String externalChatRoomId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotifyRejectMessage(userId, userName, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(externalChatRoomId, messageOut);
    }

    public void sendImToBorrowerBotForReject(String userId, String userName, String botId, String borrowerBotInstantMessageId, String externalChatRoomId, String borrowerName, String lenderName, String quoteData) {
        userName = Miscellaneous.convertUserName(userName, true);
        OutboundMessage messageOut = MessageSender.getInstance().buildImToBorrowerBotForReject(userId, userName, botId, externalChatRoomId, borrowerName, lenderName, quoteData);
        MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(borrowerBotInstantMessageId), messageOut);
    }

    public void notifyAcceptQuoteToBorrowerForm(SymphonyElementsAction action, String userId, String userName, String borrowerName, String lenderName, String externalChatRoomId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotifyAcceptMessage(userId, userName, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(externalChatRoomId, messageOut);
    }

    public void notifyAcceptSelectionForm(SymphonyElementsAction action, String userId, String userName, String borrowerName, String lenderName, String externalChatRoomId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotifyAcceptSelectionMessage(userId, userName, borrowerName, lenderName);
        MessageSender.getInstance().sendMessage(externalChatRoomId, messageOut);
    }

    public void sendImToBorrowerBotForAccept(String userId, String userName, String botId, String borrowerBotInstantMessageId, String externalChatRoomId, String borrowerName, String lenderName, String quoteData) {
        userName = Miscellaneous.convertUserName(userName, true);
        OutboundMessage messageOut = MessageSender.getInstance().buildImToBorrowerBotForAccept(userId, userName, botId, externalChatRoomId, borrowerName, lenderName, quoteData);
        MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(borrowerBotInstantMessageId), messageOut);
    }

    public void viewRfqUpdatedByLender(SymphonyElementsAction action, User user, String requestId, String lenderName, String status) {
        String csvFilePath = DataExports.exportRfqsUpdatedByLender(requestId,lenderName, status);
        String userName = user.getDisplayName();
        OutboundMessage messageOut = MessageSender.getInstance().buildViewRfqFormMessage(userName, requestId, "", lenderName, status, csvFilePath, false);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }

    public void viewRfqUpdatedByBorrower(SymphonyElementsAction action, User user, String requestId, String borrowerName, String status) {
        String csvFilePath = DataExports.exportRfqsUpdatedByBorrower(requestId, borrowerName, status);
        String userName = user.getDisplayName();
        OutboundMessage messageOut = MessageSender.getInstance().buildViewQuoteFormMessage(userName, requestId, borrowerName, "", status, csvFilePath);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }

    public void noticeError(SymphonyElementsAction action) {
        OutboundMessage messageOut = MessageSender.getInstance().buildErrorMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeError completed");
    }

    public void noticeDoubtInput(SymphonyElementsAction action) {

        OutboundMessage messageOut = MessageSender.getInstance().buildDoubtMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeDoubtInput completed");

    }

    public void noticeNoCounterParty(SymphonyElementsAction action) {

        OutboundMessage messageOut = MessageSender.getInstance().buildNoCounterPartyMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeNoCounterParty completed");

    }

    public void noticeCancelCompletion(SymphonyElementsAction action, String userId, String userName, String requestId) {

        OutboundMessage messageOut = MessageSender.getInstance().buildCancelMessage(requestId, userId, userName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeCancelCompletion completed");

    }

    public void sendTestMessage(SymphonyElementsAction action) {
        OutboundMessage messageOut = MessageSender.getInstance().buildTestMessage();
        MessageSender.getInstance().sendMessage("LZ-hhJnjHo39HmyyXnxmyn___ourpV3CdA", messageOut);
    }
}




