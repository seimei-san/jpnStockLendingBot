import clients.SymBotClient;
import clients.symphony.api.UsersClient;
import dataservices.DataServices;
import model.InboundMessage;
import model.OutboundMessage;
import model.UserInfo;
import model.events.SymphonyElementsAction;
import scripts.ConfigLoader;
import scripts.Miscellaneous;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ActionProcessor {
    private SymBotClient botClient;


    public ActionProcessor(SymBotClient botClient) {
        this.botClient = botClient;
    }

    public void process(SymphonyElementsAction action) {
        Map<String, Object> formValues = action.getFormValues();
        switch (action.getFormId()) {

            case "create-rfq-form": {
                if (formValues.get("action").equals("import-rfq-button")) {
                    int lotNo = 0;
                    String requestIdAndLotNo[] = new String[2];
                    String requestId = "";
                    if (formValues.get("request_id").equals(ConfigLoader.NO_REQUESTID)) {
                        requestIdAndLotNo = DataServices.createRequestId("RFQ");
                        requestId = requestIdAndLotNo[0];
                        lotNo = Integer.parseInt(requestIdAndLotNo[1]);
                    } else {
                        requestId = (String)formValues.get("request_id");
                        lotNo = Integer.parseInt(requestId.substring(requestId.length()-2));
                    }
                    this.manageAddRfqForm(action, requestId, lotNo);
                } else if (formValues.get("action").equals("recreate-rfq-button")) {
                    this.manageCancelRfqForm(action);
                }
                break;
            }
            case "submit-rfq-form" : {
                if (formValues.get("action").equals("recreate-rfq-button")) {
                    /**
                     * Maintain the Request ID but delete the transactions with the Request ID
                     */
                    String requestId = (String)formValues.get("request_id");
                    if (DataServices.deleteRfqsByRequestId(requestId)) {
                        this.reCreateRfqForm(action, requestId);

                    } else {
                        this.noticeError(action);
                    }

                } else if (formValues.get("action").equals("cancel-rfq-button")) {
                    /**
                     * Delete the transactions for the Request ID and return the simple text message "Cancelled"
                     */
                    String requestId =  (String)formValues.get("request_id");
                    DataServices.deleteRfqsByRequestId(requestId);
                    this.noticeCancelCompletion(action, requestId);

                } else if (formValues.get("action").equals("submit-rfq-button")) {
                    /**
                     * Insert the RFQs into the transaction table with the selected Providers and submit the RFQs to those providers
                     */


                }
                break;
            }

        }
    }

    public void manageAddRfqForm(SymphonyElementsAction action, String requestId, int lotNo) {


        Map<String, Object> formValues = action.getFormValues();
        String textRfq = (String) formValues.get("inputRfq");

        final int maxFieldNo = 4;

        String fieldValues[] = new String[maxFieldNo];

        int len = textRfq.length();
        int loopCount = 1;

        int recordCount = 0;
        int fieldNo = 0;

        String bufferMojis = "";
        String type = "RFQ";
        String stock = "";
        int lineNo = 0;
        int qty = 0;
        String start = "";
        String end = "";
        int providerNo = 0;


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
                    stock = fieldValues[0];
                    qty = Integer.parseInt(fieldValues[1]);
                    start = fieldValues[2];
                    end = fieldValues[3];

                    DataServices.insertRfq(ConfigLoader.myCounterPartyName, "RFQ", lotNo, requestId, 0, 0,
                            Miscellaneous.getTimeStamp("transaction"), lineNo, stock, qty, start, end, providerNo);

                } else {
                    bufferMojis = bufferMojis + String.valueOf(moji);
                }

            }

            loopCount += 1;
        }


        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRfqFormMessage(false, requestId, false, false);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }

    public void manageCancelRfqForm(SymphonyElementsAction action) {
        Map<String, Object> formValues = action.getFormValues();
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRfqFormMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }

    public void reCreateRfqForm(SymphonyElementsAction action, String requestId) {
        OutboundMessage messageOut = MessageSender.getInstance().buildCreateRfqFormMessage(false, requestId, false, true);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }
    public void noticeError(SymphonyElementsAction action) {

        OutboundMessage messageOut = MessageSender.getInstance().buildErrorMessage();
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }
    public void noticeCancelCompletion(SymphonyElementsAction action, String requestId) {

        OutboundMessage messageOut = MessageSender.getInstance().buildCancelMessage(requestId);
        MessageSender.getInstance().sendMessage(action.getStreamId(), messageOut);
    }
}




