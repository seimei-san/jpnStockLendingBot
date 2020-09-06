import clients.SymBotClient;
import com.sun.corba.se.impl.protocol.MinimalServantCacheLocalCRDImpl;
import dataservices.DataServices;
import dataservices.DataExports;
import dataservices.DataUpdate;
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
        String botIdForm = (String) formValues.get("bot_id");
        String botIdSelf = String.valueOf(botClient.getBotUserId());
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
                    String requestId =  (String)formValues.get("request_id");
                    DataServices.deleteRfqsByRequestId(requestId);
                    this.noticeCancelCompletion(action, userName, requestId);

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
                String userName = (String)formValues.get("user_name");
                String userId = String.valueOf(user.getUserId());
                String botId = (String)formValues.get("bot_id");
                String lenderBotInstantMessageId = (String)formValues.get("lenderbot_im_id");
                String externalChatRoomId = (String)formValues.get("external_chatroom_id");
                String borrowerName = (String)formValues.get("counterparty_borrower");
                String lenderName = (String)formValues.get("counterparty_lender");
                String rfqsData = (String)formValues.get("rfqs_data");
                if (formValues.get("action").equals("accept-rfq-button")) {
//                    This action only allowed for Lender(=lender), not allowed for Borrower(=borrower)
//                    if (formValues.get("counterparty_borrower").equals(ConfigLoader.myCounterPartyName)) {     // should be comment out for production or testing
//                    if (formValues.get("counterparty_lender").equals(ConfigLoader.myCounterPartyName)) {    // should be comment out for development
//                        ================== working =====================
//                        ================== working =====================
//                        ================== working =====================
//                        ================== working =====================
//                        ================== working =====================
                        // When Lender click accept-rfq-button, Borrower Bot sends IM to the lender Bot with the command /acceptrfp with the Rfq Data.
                        // Lender Bot will catch the command and the RFQ data and then insert the RFQ data into Lender Database.
                        // Once the RFQ data is inserted, Lender Bot send a notice of the acceptance to External chat room

                        this.sendImToLenderBot(userId, botId, lenderBotInstantMessageId, externalChatRoomId, requestId, borrowerName, lenderName, rfqsData);
                        MessageManager.getInstance().reset();
                        MessageManager.getInstance().setUserName(userName);


//                        DataServices.getInsertRfqsIntoTargetCounterParty(rfqsData, userName);
//                        this.noticeAcceptQuote(action, userName, requestId, borrowerName, lenderName);




//                        ================== working =====================
//                        ================== working =====================
//                        ================== working =====================
//                        ================== working =====================
//                        ================== working =====================


//
//                    } else {
//                        lenderName = (String)formValues.get("counterparty_lender");
//                        noticeNotAllowYou(action, lenderName);
//                    }
                }
                if (formValues.get("action").equals("nothing-quote-button")) {
//                    Instantly return No Inventory from Lender to Borrower in case of when it is obvious for Lender not to have any inventory
//                    [EXTERNAL CHAT ROOM OPERATED BY LENDER ONLY]
                    if (formValues.get("counterparty_borrower").equals(ConfigLoader.myCounterPartyName)) {     // should be comment out for production or testing
//                    if (formValues.get("counterparty_lender").equals(ConfigLoader.myCounterPartyName)) {    // should be comment out for development
                        this.replyRfqNothing(action, userId, userName, requestId, borrowerName, lenderName);
                    } else {
                        lenderName = (String)formValues.get("counterparty_lender");
                        noticeNotAllowYou(action, lenderName);
                    }
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
//                    update lenderStatus from NEW to "SENT"
//                    display the form with lenderStatus = SENT
                    String userName = user.getDisplayName();
                    String userId = user.getUserId().toString();
                    String lenderName = ConfigLoader.myCounterPartyName;
                    this.blastSendQuoteToBorrowerForm(action, userId, userName, lenderName,"NEW","SENT", true);
                }
                break;
            }

            case "receive-quote-form" : {
//                This form is submitted by bot at lender(lender)
//                The actions expects by Borrower, not Lender(lender)
//                but update the DB on both Lender and Borrower
//                [EXTERNAL CHAT ROOM OPERATED BY BORROWER ONLY]
                if (formValues.get("action").equals("reject-quote-button")) {
                    if (botIdForm.equals(botIdSelf)) {
                        if (formValues.get("counterparty_borrower").equals(ConfigLoader.myCounterPartyName)) {

                        } else {
                            String borrowerName = (String)formValues.get("counterparty_borrower");
                            noticeNotAllowYou(action, borrowerName);
                        }

                    } else {
//                        Borrower BOT needs to insert Quote Data into Borrower's database

                    }
                } else if (formValues.get("action").equals("accept-quote-button")) {
                    if (botIdForm.equals(botIdSelf)) {

                    } else {

                    }
                }



//                if (formValues.get("counterparty_borrower").equals(ConfigLoader.myCounterPartyName)) {     // should be comment out for production or testing
                if (formValues.get("counterparty_lender").equals(ConfigLoader.myCounterPartyName)) {    // should be comment out for development
                    if (formValues.get("action").equals("reject-quote-button")) {
                        // Borrower rejects quote provided by Lender in EXT Chat Room
                        // update LenderStatus from SENT to REJECT by the Lender's bot listening EXT Chat room



                    } else if (formValues.get("action").equals("accept-quote-button")) {

                    }


                } else {
                    String borrowerName = (String)formValues.get("counterparty_borrower");
                    noticeNotAllowYou(action, borrowerName);
                }
                break;
            }



            case "notify-nothing-form" : {
                if (formValues.get("counterparty_borrower").equals(ConfigLoader.myCounterPartyName)) {
                    String userName = user.getDisplayName();
                    String requestId = (String)formValues.get("request_id");
                    String borrowerName = (String)formValues.get("counterparty_borrower");
                    String lenderName = (String)formValues.get("counterparty_lender");
                    String timeStamp = Miscellaneous.getTimeStamp("transaction");
                    DataUpdate.updateWithNothing("RFQ", requestId,lenderName,lenderName,timeStamp,"NONE","NONE");
                    this.acceptRfqNothing(action, userName, requestId, borrowerName, lenderName);
                } else {
                    String lenderName = (String)formValues.get("counterparty_borrower");
                    noticeNotAllowYou(action, lenderName);
                }
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
                                stockCode, borrowerQty, borrowerStart, borrowerEnd, lenderNo, Miscellaneous.getTimeStamp("transaction"));

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


    public void manageAddQuoteForm(SymphonyElementsAction action, String userId, String userName, String fromLenderStatus, String toLenderStatus, String csvFilePath) {

        Map<String, Object> formValues = action.getFormValues();
        String quoteData = (String) formValues.get("inputQuote");

        final String type = "QUO";

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

                DataUpdate.updateQuote(items[0], fromLenderStatus, items[0], items[1],
                        Integer.parseInt(items[2]), Integer.parseInt(items[3]), items[4], items[5],
                                Double.parseDouble(items[6]),items[7],Integer.parseInt(items[8]), toLenderStatus) ;
            }
            if (notError) {
                String botId = String.valueOf(botClient.getBotUserId());
                OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "",ConfigLoader.myCounterPartyName, toLenderStatus, csvFilePath, false);
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
        String rfqsData = DataServices.insertGetRfqsForTargetCounterParty("RFQ", requestId, lenderName, lenderNo);
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
    public void sendImToLenderBot(String userId, String botId, String lenderBotInstantMessageId, String externalChatRoomId, String requestId, String borrowerName, String lenderName, String rfqsData) {
        OutboundMessage messageOut = MessageSender.getInstance().buildImToLenderBot(userId, botId, externalChatRoomId, requestId, borrowerName, lenderName, rfqsData);
        MessageSender.getInstance().sendMessage(Miscellaneous.convertRoomId(lenderBotInstantMessageId), messageOut);

    }

    public void blastSendQuoteForm(SymphonyElementsAction action, String userId, String userName, String fromLenderStatus, String toLenderStatus, String csvFilePath, boolean isNew) {
        String botId = String.valueOf(botClient.getBotUserId());
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(botId, userId, userName, "", ConfigLoader.myCounterPartyName, toLenderStatus, csvFilePath, isNew);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.blastSendQuoteForm completed");
    }

    public void blastSendQuoteToBorrowerForm(SymphonyElementsAction action, String userId, String userName, String lenderName, String fromLenderStatus, String toLenderStatus, boolean isNew) {
        String quoteData;
        ArrayList<String> borrowerNames = DataServices.getInstance().listBorrowerNames();
        String botId = String.valueOf(botClient.getBotUserId());

        for (String borrowerName : borrowerNames) {
            quoteData = DataServices.getInstance().getQuoteForBorrowerToTextarea(borrowerName, fromLenderStatus, toLenderStatus);
            String[] lenderNameInfo = DataServices.getCounterPartyInfo(borrowerName);
            OutboundMessage messageOut = MessageSender.getInstance().buildSendQuoteFormMessage(botId, userId, userName, borrowerName, lenderName, toLenderStatus,  quoteData, isNew);
            MessageSender.getInstance().sendMessage(lenderNameInfo[4], messageOut);
            DataUpdate.updateLenderStatusAfterSentQuote(borrowerName, fromLenderStatus, toLenderStatus);
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
        OutboundMessage messageOut = MessageSender.getInstance().buildNothingMessage(userName, userId, requestId, borrowerName, lenderName);
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
    public void noticeCancelCompletion(SymphonyElementsAction action, String userName, String requestId) {

        OutboundMessage messageOut = MessageSender.getInstance().buildCancelMessage(requestId, userName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeCancelCompletion completed");

    }
    public void sendTestMessage(SymphonyElementsAction action) {
        OutboundMessage messageOut = MessageSender.getInstance().buildTestMessage();
        MessageSender.getInstance().sendMessage("LZ-hhJnjHo39HmyyXnxmyn___ourpV3CdA", messageOut);
    }
}




