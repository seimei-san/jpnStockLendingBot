import clients.SymBotClient;
import com.fasterxml.jackson.module.jaxb.ser.DataHandlerJsonSerializer;
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
        switch (action.getFormId()) {

            case "create-rfq-form": {
                if (formValues.get("action").equals("import-rfq-button")) {
                    String userName = user.getDisplayName();
                    int lotNo;
                    String[] requestIdAndLotNo;
                    String requestId;
                    if (formValues.get("request_id").equals(ConfigLoader.NO_REQUESTID)) {
                        requestIdAndLotNo = DataServices.createRequestId("RFQ");
                        requestId = requestIdAndLotNo[0];
                        lotNo = Integer.parseInt(requestIdAndLotNo[1]);
                    } else {
                        requestId = (String)formValues.get("request_id");
                        lotNo = Integer.parseInt(requestId.substring(requestId.length()-2));
                    }
                    this.manageAddRfqForm(action, userName, requestId, lotNo);
                }
                break;
            }
            case "submit-rfq-form" : {
                if (formValues.get("action").equals("recreate-rfq-button")) {
//                  Maintain the Request ID but delete the transactions with the Request ID
                    String userName = user.getDisplayName();
                    String requestId = (String)formValues.get("request_id");
                    if (DataServices.deleteRfqsByRequestId(requestId)) {
                        this.reCreateRfqForm(action, userName, requestId);

                    } else {
                        this.noticeError(action);
                    }

                } else if (formValues.get("action").equals("cancel-rfq-button")) {
                    // Delete the transactions for the Request ID and return the simple text message "Cancelled"
                    String userName = user.getDisplayName();
                    String requestId =  (String)formValues.get("request_id");
                    DataServices.deleteRfqsByRequestId(requestId);
                    this.noticeCancelCompletion(action, userName, requestId);

                } else if (formValues.get("action").equals("send-rfq-button")) {
                    // Insert the RFQs into the transaction table with the selected Providers and submit the RFQs to those providers
                    String userName = user.getDisplayName();
                    String requestId = (String)formValues.get("request_id");
                    String requesterName = (String)formValues.get("counterparty_requester");
                    String providerName1 = (String)formValues.get("provider1-select");
                    String providerName2 = (String)formValues.get("provider2-select");
                    String providerName3 = (String)formValues.get("provider3-select");
                    String providerName4 = (String)formValues.get("provider4-select");
                    String providerName5 = (String)formValues.get("provider5-select");
                    if (providerName1!=null) {
                        requestId = (String)formValues.get("request_id");
                        blastSendReqForm(false, userName, requestId, requesterName,1, providerName1, true);
                    } else {
                        this.noticeNoCounterParty(action);
                        this.manageResendRfqForm(action, userName, requestId);
                    }
                    if (providerName2!=null) {
                        blastSendReqForm(false, userName, requestId, requesterName,2, providerName2, true);
                    }
                    if (providerName3!=null) {
                        blastSendReqForm(false, userName, requestId, requesterName,3, providerName3, true);
                    }
                    if (providerName4!=null) {
                        blastSendReqForm(false, userName, requestId, requesterName,4, providerName4, true);

                    }
                    if (providerName5!=null) {
                        blastSendReqForm(false, userName, requestId, requesterName,5, providerName5, true);

                    }
                }
                break;
            }
            case "receive-rfq-form" : {
//                After Borrower send RFQ to a target Lenders, this form is used in the External Chat Room where Borrower and Lender are exist together.
//                The actions here expects that the lender in the External Chat Room initiates.
                String requestId = (String)formValues.get("request_id");
                String borrowerName = (String)formValues.get("counterparty_requester");
                String lenderName = (String)formValues.get("counterparty_provider");
                String rfqData = (String)formValues.get("rfqs_data");
                if (formValues.get("action").equals("accept-rfq-button")) {
                    if (formValues.get("counterparty_requester").equals(ConfigLoader.myCounterPartyName)) {     // should be comment out for production or testing
//                    if (formValues.get("counterparty_provider").equals(ConfigLoader.myCounterPartyName)) {    // should be comment out for development
                        String userName = user.getDisplayName();
                        DataServices.getInsertRfqsIntoTargetCounterParty(rfqData, userName);
                      this.noticeAcceptQuote(action, userName, requestId, borrowerName, lenderName);

                    } else {
                        String providerName = (String)formValues.get("counterparty_provider");
                        noticeNotAllowYou(action, providerName);
                    }
                }
                if (formValues.get("action").equals("nothing-quote-button")) {
                    if (formValues.get("counterparty_requester").equals(ConfigLoader.myCounterPartyName)) {     // should be comment out for production or testing
//                    if (formValues.get("counterparty_provider").equals(ConfigLoader.myCounterPartyName)) {    // should be comment out for development
                        String userName = user.getDisplayName();
                        this.replyRfqNothing(action, userName, requestId, borrowerName, lenderName);
                    } else {
                        String providerName = (String)formValues.get("counterparty_provider");
                        noticeNotAllowYou(action, providerName);
                    }
                }
                break;
            }
            case "create-quote-form" : {
                if (formValues.get("action").equals("import-quote-button")) {
                    String userName = user.getDisplayName();
                    this.manageAddQuoteForm(action, userName,"YET", "NEW", "");

                }
                break;
            }
            case "submit-quote-form" : {
                if (formValues.get("action").equals("recreate-quote-button")) {
                    String userName = user.getDisplayName();
                    String csvFilePath = DataExports.exportRfqsToProvider(ConfigLoader.myCounterPartyName,"NEW");
                    this.blastSendQuoteForm(action, userName, "NEW", "NEW", csvFilePath, true);
                } else if (formValues.get("action").equals("send-quote-button")) {
//                    get list of borrowerNames
//                    while loop to select quote per borrower and then send selected quote to EXT room for facing each borrower
//                    update lenderStatus from NEW to "SENT"
//                    display the form with lenderStatus = SENT
                    String userName = user.getDisplayName();
                    String providerName = ConfigLoader.myCounterPartyName;
                    this.blastSendQuoteToBorrowerForm(action, userName, providerName,"NEW","SENT", true);



                }

                break;
            }



            case "notify-nothing-form" : {
                if (formValues.get("counterparty_requester").equals(ConfigLoader.myCounterPartyName)) {
                    String userName = user.getDisplayName();
                    String requestId = (String)formValues.get("request_id");
                    String borrowerName = (String)formValues.get("counterparty_requester");
                    String lenderName = (String)formValues.get("counterparty_provider");
                    String timeStamp = Miscellaneous.getTimeStamp("transaction");
                    DataUpdate.updateWithNothing("RFQ", requestId,lenderName,lenderName,timeStamp,"NONE","NONE");
                    this.acceptRfqNothing(action, userName, requestId, borrowerName, lenderName);
                } else {
                    String providerName = (String)formValues.get("counterparty_requester");
                    noticeNotAllowYou(action, providerName);
                }
                break;
            }
        }
    }

    public void manageAddRfqForm(SymphonyElementsAction action, String userName, String requestId, int lotNo) {

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
        final int providerNo = 0;

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
                                stockCode, borrowerQty, borrowerStart, borrowerEnd, providerNo, Miscellaneous.getTimeStamp("transaction"));

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

            OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(false, userName, requestId, ConfigLoader.myCounterPartyName, "", false, false);
            MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
            LOGGER.debug("ActionProcessor.manageAddRfqForm completed");
        }

    }


    public void manageAddQuoteForm(SymphonyElementsAction action, String userName, String fromLenderStatus, String toLenderStatus, String csvFilePath) {

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
                OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(false, userName, ConfigLoader.myCounterPartyName, toLenderStatus, csvFilePath, false);
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

    public void blastSendReqForm(boolean onErrorProviders, String userName, String requestId, String requesterName, int providerNo, String providerName, boolean isSent) {
        String rfqsData = DataServices.insertGetRfqsForTargetCounterParty("RFQ", requestId, providerName, providerNo);
//        String csvFilePath = DataExports.exportRfqsForTargetProvider(requestId, requesterName,providerName);
        String[] providerNameInfo = DataServices.getCounterPartyInfo(providerName);
        OutboundMessage messageOut = MessageSender.getInstance().buildSendRfqFormMessage(onErrorProviders, userName, requestId, requesterName, providerName, rfqsData, "", isSent);
        MessageSender.getInstance().sendMessage(providerNameInfo[4], messageOut);
        LOGGER.debug("ActionProcessor.blastSendReqForm completed");
    }

    public void blastSendQuoteForm(SymphonyElementsAction action, String userName, String fromLenderStatus, String toLenderStatus, String csvFilePath, boolean isNew) {
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateQuoteFormMessage(false, userName, ConfigLoader.myCounterPartyName, toLenderStatus, csvFilePath, isNew);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.blastSendQuoteForm completed");
    }

    public void blastSendQuoteToBorrowerForm(SymphonyElementsAction action, String userName, String providerName, String fromLenderStatus, String toLenderStatus, boolean isNew) {
        String quoteData;
        ArrayList<String> borrowerNames = DataServices.getInstance().listBorrowerNames();
        for (String borrowerName : borrowerNames) {
            quoteData = DataServices.getInstance().getQuoteForBorrowerToTextarea(borrowerName, fromLenderStatus, toLenderStatus);
            String[] providerNameInfo = DataServices.getCounterPartyInfo(borrowerName);
            OutboundMessage messageOut = MessageSender.getInstance().buildSendQuoteFormMessage(false, userName, borrowerName, providerName, toLenderStatus,  quoteData, isNew);
            MessageSender.getInstance().sendMessage(providerNameInfo[4], messageOut);
            DataUpdate.updateLenderStatusAfterSentQuote(borrowerName, fromLenderStatus, toLenderStatus);
        }
    }



    public void manageResendRfqForm(SymphonyElementsAction action, String userName, String requestId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(false, userName, requestId, "", ConfigLoader.myCounterPartyName,false, false);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageResendRfqForm completed");
    }

    public void manageCancelRfqForm(SymphonyElementsAction action) {
        Map<String, Object> formValues = action.getFormValues();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageCancelRfqForm completed");
    }

    public void reCreateRfqForm(SymphonyElementsAction action, String userName, String requestId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(false, userName, requestId, ConfigLoader.myCounterPartyName, "", false, true);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.reCreateRfqForm completed");
    }

    public void noticeNotAllowYou(SymphonyElementsAction action, String tmpName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNotAllowYouMessage(tmpName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeNowAllowYou completed");
    }

    public void replyRfqNothing(SymphonyElementsAction action, String userName, String requestId, String requesterName, String providerName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildNothingMessage(userName, requestId, requesterName, providerName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.replyRfqNothing completed");
    }

    public void noticeAcceptQuote(SymphonyElementsAction action, String userName, String requestId, String requesterName, String providerName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildAcceptQuoteMessage(requestId, userName, requesterName, providerName);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.notifyAcceptQuote completed");
    }
    public void acceptRfqNothing(SymphonyElementsAction action, String userName, String requestId, String requesterName, String providerName) {
        OutboundMessage messageOut = MessageSender.getInstance().buildAcceptNothingMessage(requestId, userName, requesterName, providerName);
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
}




