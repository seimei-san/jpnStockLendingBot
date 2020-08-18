package scripts;

import dataservices.DataServices;

/**
 * generate Element Form for newly creating a RFQ
 */
public class GenerateElementsForm {


    public static String createReqElementsForm(String requestId) {
        Miscellaneous ms = new Miscellaneous();
        DataServices ds = new DataServices();
        String messageOptions = "";
        String messageForm = "";

        for (int i=0; i < ds.counterPartiesList.length; i++) {
            messageOptions += "<option value=\"" + ds.counterPartiesList[i] + "\">" + ds.counterPartiesList[i] + "</option>";
        }

        messageForm =
                "<form id=\"{{form_id}}\">" +
                "<h3 class=\"tempo-text-color-white tempo-bg-color--red\">Create RFQ</h3>" +
                "<p class=\"tempo-text-color--red\"><i>Do not update this Request ID</i></p>" +
                "<text-field name=\"request_id\" maxlength=\"9\">" + requestId + "</text-field>" +
                "<br/>" +
                "<h6>Please copy and paste the list of your RFQ into this box.</h6>" +
                "<textarea placeholder=\"Copy/paste the list of RFQ (Stock Code, Qty, Start, End).  For example,  " +
                        "&#13;" +
                        "1234 7000 0801 0930" +
                        "&#13;" +
                        "4567 10000 0801 3m" +
                        "&#13;" +
                        "7890 3000 0803 3w\"  name=\"rfqInput\" required=\"true\"></textarea>" +
                "<h6>Please select Providers. You need to select one at least.</h6>" +
                "<select name=\"provider1-select\" required=\"true\" data-placeholder=\"Select the 1st provider\">";

        messageForm += messageOptions;

        messageForm +=
                "</select>" +
                "<select name=\"provider2-select\" required=\"false\" data-placeholder=\"Select the 2nd provider\">";

        messageForm += messageOptions;

        messageForm +=
                "</select>" +
                "<select name=\"provider3-select\" required=\"false\" data-placeholder=\"Select the 3rd provider\">";

        messageForm += messageOptions;

        messageForm +=
                "</select>" +
                "<select name=\"provider4-select\" required=\"false\" data-placeholder=\"Select the 4th provider\">";

        messageForm += messageOptions;

        messageForm +=
                "</select>" +
                "<select name=\"provider5-select\" required=\"false\" data-placeholder=\"Select the 5th provider\">";

        messageForm += messageOptions;

        messageForm +=
                "</select>" +
                "<button type=\"reset\">Reset</button><button name=\"createrfq\" type=\"action\">Create</button>" +
                "</form>";

        return messageForm;
    }


    /**
     * Generate Element Form for confirming the created RFQ
     */
    public static String confirmCreatedRfq(String rfqMessage, String provider) {
//    public static String confirmCreatedRfq() {
        Miscellaneous miscellaneous = Miscellaneous.getInstance();

        final String JOB_NAME = "Create RFQ: ";
        final int maxFieldNo = 4;


        String requestIdForTheDay = "";
        String fieldValues[] = new String[maxFieldNo];
        String messageForm2 = "";


        // generate unique RequestNo for the day
        requestIdForTheDay = "B" + Miscellaneous.getTimeStamp("RFQ") + "XX";

        if (rfqMessage.length() == 0) {
            return "ERROR (no RFQ message)";
        }

        String messageForm = "<form id=\"{{form_id}}\">" +
                "<h1>Confirm RFQ: {{request_id}}</h1>" +
                "<br/>" +
                "<h3>RFQ List : </h3>" +
                "<table>" +
                "<thead>" +
                "<tr>" +
                "<td>Type</td>" +
                "<td>LineNo</td>" +
                "<td>Stock</td>" +
                "<td>Qty</td>" +
                "<td>Start</td>" +
                "<td>End</td>" +
                "</tr>" +
                "</thead>" +
                "<tbody>";


//        Parse pasted RFQ Message and convert into the table into Elements
        int len = rfqMessage.length();
        int loopCount = 1;

        int recordCount = 0;
        int fieldNo = 0;
        int totalQty = 0;
        String bufferMojis = "";
        String totalQtyStr = "";

        for(char moji: rfqMessage.toCharArray()){


            // 9=tab, 32=space; 44=comma
            if (((int)moji==9 || (int)moji==32 || (int)moji==44) && fieldNo <= maxFieldNo) {
                // connecting chars to get a value in the fields
                fieldValues[fieldNo] = bufferMojis;
                fieldNo += 1;
                bufferMojis = "";
            } else if (((int)moji==10 && fieldNo > 0) || loopCount == len) {
                bufferMojis = bufferMojis + String.valueOf(moji);
                fieldValues[fieldNo] = bufferMojis;
                fieldNo = 0;
                bufferMojis = "";
                recordCount += 1;

                String type = "RFQ";
                String lineNo = String.valueOf(recordCount);
                String stockCode = fieldValues[0];
                int qty =  Integer.valueOf(fieldValues[1]);
                String requesterQty = String.format("%,d",qty);
                totalQty += Integer.valueOf(qty);
                totalQtyStr = String.format("%,d",totalQty);


                String requesterStart = fieldValues[2];
                String requesterEnd = fieldValues[3];
                messageForm +=
                                    "<tr>" +
                                        "<td>" + type + "</td>" +
                                        "<td style='text-align:right'>" + lineNo + "</td>" +
                                        "<td>" + stockCode + "</td>" +
                                        "<td style='text-align:right'>" + requesterQty + "</td>" +
                                        "<td>" + requesterStart + "</td>" +
                                        "<td>" + requesterEnd + "</td>" +
                                  "</tr>";
            } else {
                bufferMojis = bufferMojis + String.valueOf(moji);
            }

            loopCount += 1;
        }
        messageForm +=
                            "</tbody>" +
                            "<tfoot>" +
                                    "<tr>" +
                                        "<td></td>" +
                                        "<td></td>" +
                                        "<td style='text-align:right'>TTL: </td>" +
                                        "<td style='text-align:right'>" + totalQtyStr + "</td>" +
                                        "<td></td>" +
                                        "<td></td>" +
                                    "</tr>" +
                            "</tfoot>" +
                        "</table>" +
                        "<br/>" +
                        "<h3>Providers:</h3>" +
                        "<p>Provider1: " + "TEST" + "</p>" +
//                            "<p>Provider2: " + providers[1] + "</p>" +
//                            "<p>Provider3: " + providers[2] + "</p>" +
//                            "<p>Provider4: " + providers[3] + "</p>" +
//                            "<p>Provider5: " + providers[4] + "</p>" +
                        "<button type=\"reset\">Reset</button><button type=\"action\" name=\"confirm-rfq\">Confirm</button>" +
                         "</form>";


        System.out.println(messageForm2);

        return messageForm;


    }

}
