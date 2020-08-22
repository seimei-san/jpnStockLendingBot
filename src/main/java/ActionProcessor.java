import clients.SymBotClient;
import dataservices.DataServices;
import dataservices.DataExports;
import model.OutboundMessage;
import model.events.SymphonyElementsAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.util.Map;


public class ActionProcessor {
    final private SymBotClient botClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionProcessor.class);


    public ActionProcessor(SymBotClient botClient) {
        this.botClient = botClient;
    }


    public void process(SymphonyElementsAction action) {
        Map<String, Object> formValues = action.getFormValues();
        switch (action.getFormId()) {

            case "create-rfq-form": {
                if (formValues.get("action").equals("import-rfq-button")) {
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
                    this.manageAddRfqForm(action, requestId, lotNo);
                }
                break;
            }
            case "submit-rfq-form" : {
                if (formValues.get("action").equals("recreate-rfq-button")) {
//                  Maintain the Request ID but delete the transactions with the Request ID
                    String requestId = (String)formValues.get("request_id");
                    if (DataServices.deleteRfqsByRequestId(requestId)) {
                        this.reCreateRfqForm(action, requestId);

                    } else {
                        this.noticeError(action);
                    }

                } else if (formValues.get("action").equals("cancel-rfq-button")) {
                    // Delete the transactions for the Request ID and return the simple text message "Cancelled"
                    String requestId =  (String)formValues.get("request_id");
                    DataServices.deleteRfqsByRequestId(requestId);
                    this.noticeCancelCompletion(action, requestId);

                } else if (formValues.get("action").equals("send-rfq-button")) {
                    // Insert the RFQs into the transaction table with the selected Providers and submit the RFQs to those providers
                    String requestId = (String)formValues.get("request_id");
                    String requesterName = (String)formValues.get("counterparty_requester");
                    String providerName1 = (String)formValues.get("provider1-select");
                    String providerName2 = (String)formValues.get("provider2-select");
                    String providerName3 = (String)formValues.get("provider3-select");
                    String providerName4 = (String)formValues.get("provider4-select");
                    String providerName5 = (String)formValues.get("provider5-select");
                    if (providerName1!=null) {
                        requestId = (String)formValues.get("request_id");
                        blastSendReqForm(action, false, requestId, requesterName,1, providerName1, true);
                    } else {
                        this.noticeNoCounterParty(action);
                        this.manageResendRfqForm(action, requestId);
                    }
                    if (providerName2!=null) {
                        blastSendReqForm(action, false, requestId, requesterName,2, providerName2, true);
                    }
                    if (providerName3!=null) {
                        blastSendReqForm(action, false, requestId, requesterName,3, providerName3, true);
                    }
                    if (providerName4!=null) {
                        blastSendReqForm(action, false, requestId, requesterName,4, providerName4, true);

                    }
                    if (providerName5!=null) {
                        blastSendReqForm(action, false, requestId, requesterName,5, providerName5, true);

                    }
                }
                break;
            }
            case "receive-rfq-form" : {
//                After Borrower send RFQ to a target Lenders, this form is used in the External Chat Room where Borrower and Lender are exist together.
//                The actions here expects that the lender in the External Chat Room initiates.

                if (formValues.get("action").equals("accept-rfq-button")) {
//                    This action is triggered at when Lender accepts the RFQ in the External Chat room and then return the RFQ CSV file from Borrower to Lender
                    String requestId = (String)formValues.get("request_id");
                    String requesterName = (String)formValues.get("counterparty_requester");
                    String providerName = (String)formValues.get("counterparty_provider");
                    String csvFilePath = DataExports.exportRfqsForTargetProvider(requestId, requesterName, providerName);
                    sendRfqCsvToProvider(action, requestId, requesterName, providerName, csvFilePath);

                }
                if (formValues.get("action").equals("nothing-quote-button")) {

                }

            }

        }
    }

    public void manageAddRfqForm(SymphonyElementsAction action, String requestId, int lotNo) {


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
                        if (Miscellaneous.getInstance().isNumber(fieldValues[1]) || !fieldValues[1].isEmpty()) {
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
            OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(false, requestId, ConfigLoader.myCounterPartyName,false, false);
            MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
            LOGGER.debug("ActionProcessor.manageAddRfqForm completed");
        }

    }

    public void blastSendReqForm(SymphonyElementsAction action, boolean onErrorProviders, String requestId, String requesterName, int providerNo, String providerName, boolean isSent) {
        DataServices.insertRfqsForTargetCounterParty(requestId, providerName, providerNo);
        String csvFilePath = DataExports.exportRfqsForTargetProvider(requestId, requesterName,providerName);
        String[] providerNameInfo = DataServices.getCounterPartyInfo(providerName);
        OutboundMessage messageOut = MessageSender.getInstance().buildSendRfqFormMessage(onErrorProviders, requestId, requesterName, providerName, csvFilePath, isSent);
        MessageSender.getInstance().sendMessage(providerNameInfo[4], messageOut);
        LOGGER.debug("ActionProcessor.blastSendReqForm completed");
    }

    public void manageResendRfqForm(SymphonyElementsAction action, String requestId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(false, requestId, ConfigLoader.myCounterPartyName,false, false);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageResendRfqForm completed");
    }

    public void manageCancelRfqForm(SymphonyElementsAction action) {
        Map<String, Object> formValues = action.getFormValues();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.manageCancelRfqForm completed");
    }

    public void reCreateRfqForm(SymphonyElementsAction action, String requestId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRequestFormMessage(false, requestId, ConfigLoader.myCounterPartyName,false, true);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.reCreateRfqForm completed");

    }

    public void sendRfqCsvToProvider(SymphonyElementsAction action, String requestId, String requesterName, String providerName, String csvFullPath) {
        OutboundMessage messageOut = MessageSender.getInstance().buildAccpetRfqMessageForProvider(requestId, requesterName, providerName, csvFullPath);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.sendRfqCsvToProvider completed");
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
    public void noticeCancelCompletion(SymphonyElementsAction action, String requestId) {

        OutboundMessage messageOut = MessageSender.getInstance().buildCancelMessage(requestId);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
        LOGGER.debug("ActionProcessor.noticeCancelCompletion completed");

    }
}




