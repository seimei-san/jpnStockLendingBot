import clients.SymBotClient;
import dataservices.*;
import model.OutboundMessage;
import model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;

import java.io.File;

public class MessageSender {
    private static MessageSender messageSender;
    private final SymBotClient botClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);



    private MessageSender(SymBotClient botClient) {
        this. botClient = botClient;
    }

    public static MessageSender getInstance() {
        if (messageSender != null) {
            return messageSender;
        } else {
            throw new RuntimeException("MessageSender needs to be initialized at startup");
        }
    }

    public static void createInstance(SymBotClient botClient) {
        if (messageSender == null) {
            messageSender = new MessageSender(botClient);
        }
    }

    public void sendMessage(String streamId, OutboundMessage messageOut) {
        try {
            this.botClient.getMessagesClient().sendMessage(streamId, messageOut);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OutboundMessage buildInitializeConfigMessage(String msg1, String msg2, String msg3, String msg4) {
        String message =
                "Hi, " + msg1 + "<br/>" + msg2 + "<br/>" + msg3 + "<br/>" + msg4;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildInitializeConfigMessage returned");
        return messageOut;
    }

    public OutboundMessage buildInitializeSodMessage(String msg1, String msg2, String msg3, String msg4, String msg5, String msg6, String msg7, String msgFoot) {
        String message =
                " <b><i> SOD Initialization executed: </i></b><br/>" + msg1 +
                        "<br/>" + msg2 + "<br/>" + msg3 + "<br/>" + msg4 + "<br/>" + msg5 + "<br/>" + msg6 + "<br/>" + msg7 + "<br/>" + msgFoot;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildInitializeSodMessage returned");
        return messageOut;
    }

//    ===================================== Borrower Side Forms ==================================================================
//    ===================================== Borrower Side Forms ==================================================================
//    ===================================== Borrower Side Forms ==================================================================

//    --------------------------------------------------------------------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------
//    ---------------- Build RFQs from copied/pasted RFQ Data by User  ---------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------


    public OutboundMessage buildCreateRequestFormMessage(String userId) {
        LOGGER.debug("buildCreateRequestFormMessage returned");

        return this.buildCreateRequestFormMessage("", userId, "", "", "", "", false, true);
    }


    public OutboundMessage buildCreateRequestFormMessage(String botId, String userId, String userName, String requestId, String borrowerName, String lenderName, boolean isInserted, boolean isNew) {

        String message;
        StringBuilder messageOptions = new StringBuilder();
        String titleForm;


        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptions.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
        }

        String displayRequestId;

        displayRequestId = requestId;


        if (isNew) {
            titleForm = "RFQの作成: ";
        } else {
            titleForm = "RFQの送信: ";
        }

        if (displayRequestId.equals("")) {
            message =
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + borrowerName + " []</h3>";
        } else {
            message =
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + borrowerName+ " [<hash tag=\"" + displayRequestId + "\"/>]</h3>";
        }


        message +=
//                "<h4>作成者： <mention uid='" + userId + "'/></h4>" +
                "<h4>作成者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:800px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:5%;font-weight:bold'>種別</td>" +
                        "<td style='width:5%;font-weight:bold'>枝番</td>" +
                        "<td style='width:10%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:15%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:10%;font-weight:bold'>開始日</td>" +
                        "<td style='width:50%;font-weight:bold'>返却日/期間</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQty = 0;
        for (CreateRfq createRfq : dataServices.getTargetRfqs(requestId, 0)) {
            totalQty += createRfq.getborrowerQty();
            message +=
                    "<tr>" +
                            "<td>" + createRfq.getType() + "</td>" +
                            "<td style='text-align:right'>" + createRfq.getLineNo() + "</td>" +
                            "<td>" + createRfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", createRfq.getborrowerQty()) + "</td>" +
                            "<td>" + createRfq.getborrowerStart() + "</td>" +
                            "<td>" + createRfq.getborrowerEnd() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>合計株数: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQty) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<br/>" +
                        "<div style=\"display: flex;\">" +
                        "<div style=\"width:50%;\">";

//       Div in Left Bottom -----------------------------------------
//       Div in Left Bottom -----------------------------------------
//       Div in Left Bottom -----------------------------------------
//       Div in Left Bottom -----------------------------------------
        if (isNew) {
            // 1st step to import the pasted RFQ data in the textarea

            message +=
                    "<form id=\"create-rfq-form\">";

            if (!isInserted) {

                message +=
                        "<h3>依頼番号: " + requestId + "</h3>" +
                                "<br/>";
                if (ConfigLoader.env.equals("prod")) {
                    message += "<div style=\"display:none\">";
                }
                message +=
                        "<p>botId: userId: userName: requestId: borrowerName: lenderName:  </p>" +
                                "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                                "<text-field name=\"user_id\" required=\"false\">" + userId + "</text-field>" +
                                "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                                "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                                "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                                "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>";
                if (ConfigLoader.env.equals("prod")) {
                    message += "</div>";
                }
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">RFQデータの入力</h3>" +
                            "<br/>" +
//                                "<h6>入力欄にRFQデータをコピー／貼付してください</h6>" +
                            "<textarea placeholder=\"ここにコピーしたRFQデータを貼付てください。 " +
                            "&#13;" +
                            "銘柄, 株数, 開始, 終了／期間 （区切り文字は、半角スペース、カンマ、またはタブ）。" +
                            "&#13;" +
                            "入力例： " +
                            "&#13;" +
                            "1234 7000 201201 210330" +
                            "&#13;" +
                            "4567 10000 200801 3m" +
                            "&#13;" +
                            "7890 3000 200803 3w\"  name=\"inputRfq\" required=\"true\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-rfq-button\">取込</button>";

            message +=
                    "</form>";
        }
        if (!isNew) {
            // after import, the form re-submitted with the following form in order to submit RFQ to selected lenders (max. 5).

            message +=
                    "<form id=\"submit-rfq-form\">";

            message +=
                    "<h3>依頼番号: " + requestId + "</h3>" +
                            "<br/>";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>requestId: botId: borrowerName: </p>" +
                            "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                            "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }
            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">RFQを貸手に送信</h3>" +
                            "<br/>";

            message +=
                    "<select name=\"lender1-select\" required=\"false\" data-placeholder=\"貸手1を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"lender2-select\" required=\"false\" data-placeholder=\"貸手2を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"lender3-select\" required=\"false\" data-placeholder=\"貸手3を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"lender4-select\" required=\"false\" data-placeholder=\"貸手4を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"lender5-select\" required=\"false\" data-placeholder=\"貸手5を選択\">";
            message += messageOptions;
            message +=
                    "</select>";
            message +=
                    "<button type=\"action\" name=\"recreate-rfq-button\">再作成</button>" +
                            "<button type=\"action\" name=\"cancel-rfq-button\">取消</button>" +
                            "<button type=\"action\" name=\"send-rfq-button\">送信</button>" +
                            "</form>";
        }

        message +=
                "</div>";

//      Div in Right Bottom -----------------------------------------------
//      Div in Right Bottom -----------------------------------------------
//      Div in Right Bottom -----------------------------------------------
        message +=
                "<div style=\"width:50%;\">";
        // keep Right Bottom Area empty for ergonomic purpose
        // keep Right Bottom Area empty for ergonomic purpose
        // keep Right Bottom Area empty for ergonomic purpose

        message +=
                "</div>" +
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildCreateRequestFormMessage returned");
        return messageOut;
    }

//    --------------------------------------------------------------------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------
//    ---------------- Build a message for sending RFQs to lender ------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------


    public OutboundMessage buildSendRfqFormMessage(String botId, String userId, String lenderBotInstantMessageId, String externalChatRoomId, String rfqsData, String userName, String requestId,
                                                   String borrowerName, String lenderName, String csvFullPath, boolean isSent) {

        String message;

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">QUOTEの依頼："+ borrowerName + "[<hash tag=\"" + requestId + "\"/>]" + " → " + lenderName + "</h3>";

        message +=
//                "<h4>依頼者： <mention uid='" + userId + "'/></h4>" +
                "<h4>依頼者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1200px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:5%;font-weight:bold'>種別</td>" +
                        "<td style='width:7%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:4%;font-weight:bold'>枝番</td>" +
                        "<td style='width:6%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:9%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:8%;font-weight:bold'>開始日</td>" +
                        "<td style='width:9%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:7%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:10%;font-weight:bold;text-align:right'>見積株数</td>" +
                        "<td style='width:8%;font-weight:bold'>見積開始</td>" +
                        "<td style='width:9%;font-weight:bold'>見積返却</td>" +
                        "<td style='width:8%;font-weight:bold;text-align:right'>見積利率</td>" +
                        "<td style='width:10%;font-weight:bold'>見積条件</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (SendRfq sendRfq : dataServices.getSendRfqs(requestId, lenderName)) {
            totalQtyborrower += sendRfq.getBorrowerQty();
            totalQtylender += sendRfq.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + sendRfq.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + sendRfq.getBorrowerName() + "</td>" +
                            "<td style='text-align:right'>" + sendRfq.getLineNo() + "</td>" +
                            "<td>" + sendRfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendRfq.getBorrowerQty()) + "</td>" +
                            "<td>" + sendRfq.getBorrowerStart() + "</td>" +
                            "<td>" + sendRfq.getBorrowerEnd() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + sendRfq.getLenderName() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendRfq.getLenderQty()) + "</td>" +
                            "<td>" + sendRfq.getLenderStart() + "</td>" +
                            "<td>" + sendRfq.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + sendRfq.getLenderRate() + "</td>" +
                            "<td>" + sendRfq.getLenderCondition() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>合計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>合計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";

        message +=
                "<br/>" +
                        "<div style=\"display: flex;\">" +
                        "<div style=\"width:50%;\">";

//     Div in Left Bottom  ------------------
//     Div in Left Bottom  ------------------
//     Div in Left Bottom  ------------------
//     Div in Left Bottom  ------------------
        if (!isSent) {
//            reserve the section for future function
        }

        message +=
                "</div>";

//        Div in Right Bottom ----------------
//        Div in Right Bottom ----------------
//        Div in Right Bottom ----------------
        message +=
                "<div style=\"width:50%;\">";

        if (isSent) {

            message +=
                    "<form id=\"receive-rfq-form\">";

            message +=
                    "<h3>依頼番号: " + requestId + "</h3>" +
                            "<br/>";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>requestId: usrName: botId: externalChatRoomId: lenderBotInstantMessageId: borrowerName: lenderName: rfqsData:</p>" +
                            "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                            "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                            "<text-field name=\"external_chatroom_id\" required=\"false\">" + externalChatRoomId + "</text-field>" +
                            "<text-field name=\"lenderbot_im_id\" required=\"false\">" + lenderBotInstantMessageId + "</text-field>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<textarea name=\"rfqs_data\" required=\"false\">" + rfqsData + "</textarea>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }
            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--blue\">貸手(" + lenderName + ")の対応</h3>" +
                            "<br/>";
            message +=
                    "<button type=\"action\" name=\"nothing-quote-button\">在庫無</button>" +
                            "<button type=\"action\" name=\"accept-rfq-button\">受付</button>" +
                            "</form>";
        }

        message +=
                "</div>" +
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);

        return messageOut;
    }

    public OutboundMessage buildSendIoiFormMessage(String botId, String userId, String borrowerBotInstantMessageId, String externalChatRoomId, String ioisData, String userName, String requestId,
                                                   String borrowerName, String lenderName, String csvFullPath, boolean isSent) {

        String message;
        String titleForm;


        titleForm = "IOIの送信: ";

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + borrowerName + " ← " + lenderName + "</h3>";



        message +=
//                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>担当者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>提示先</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>銘柄</td>" +
                        "<td style='width:8%;font-weight:bold;color:#3300ff;text-align:right'>株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>提示元</td>" +
                        "<td style='width:8%;font-weight:bold'>提示番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>提示株数</td>" +
                        "<td style='width:6%;font-weight:bold'>提示開始</td>" +
                        "<td style='width:7%;font-weight:bold'>提示返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right'>提示利率</td>" +
                        "<td style='width:7%;font-weight:bold'>提示条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.getSendIois(requestId, borrowerName)) {
            totalQtyborrower += viewIoi.getBorrowerQty();
            totalQtylender += viewIoi.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewIoi.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                            "<td>" + viewIoi.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                            "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                            "<td>" + viewIoi.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


//      Div in Left Bottom -----------------
//      Div in Left Bottom -----------------
//      Div in Left Bottom -----------------
        message +=
                "<div style=\"width:50%;\">";

        message +=
                "<form id=\"receive-ioi-form\">";
        if (ConfigLoader.env.equals("prod")) {
            message += "<div style=\"display:none\">";
        }
        message +=
                "<p>userName: botId: externalChatRoomId: borrowerBotInstantMessageId: borrowerName: lenderName: quoteData: </p>" +
                        "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                        "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                        "<text-field name=\"external_chatroom_id\" required=\"false\">" + externalChatRoomId + "</text-field>" +
                        "<text-field name=\"borrowerbot_im_id\" required=\"false\">" + borrowerBotInstantMessageId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                        "<textarea name=\"ioi_data\" required=\"false\">" + ioisData + "</textarea>";
        if (ConfigLoader.env.equals("prod")) {
            message += "</div>";
        }

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">借手(" + borrowerName + ")の対応</h3>" +
                "<br/>";

        message +=

                "<button type=\"action\" name=\"reject-ioi-button\">需要無</button>" +
                "<button type=\"action\" name=\"accept-ioi-button\">受付</button>" +
                "</form>";
        message +=
                "</div>" ;

//       Div in Right Bottom
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";



        message +=
                "</div>"+
                "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildSendIoiFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildCancelMessage(String requestId, String userId, String userName, String commandLine) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>依頼番号[<hash tag=\"" + requestId + "\"/>]は、<mention uid='" + userId + "'/>によって取消されました。</h3>" +
                        "<p>新たに作成する場合は、<b>@" + botUserInfo.getDisplayName() + " " + commandLine + "</b> のようにチャットボットに命令文を送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildAcceptNothingMessage(String requestId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>依頼番号[<hash tag=\"" + requestId + "\"/>] " + lenderName + "からの<b>在庫無し連絡</b>は、" + borrowerName + "(" + userName + ")に承諾されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildAcceptQuoteMessage(String requestId, String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>依頼番号[<hash tag=\"" + requestId + "\"/>] " + borrowerName + "からの<b>QUOTE依頼</b>は、<mention uid='" + userId + "'/> に受付されました。</h5>" +
//                "<h5>依頼番号[<hash tag=\"" + requestId + "\"/>] " + borrowerName + "からの<b>QUOTE依頼</b>は、" + userName + "に受付されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildAcceptIoiMessage(String requestId, String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>依頼番号[<hash tag=\"" + requestId + "\"/>] " + lenderName + "からの<b>IOIの送信</b>は、<mention uid='" + userId + "'/> に受付されました。</h5>" +
//                "<h5>依頼番号[<hash tag=\"" + requestId + "\"/>] " + borrowerName + "からの<b>QUOTE依頼</b>は、" + userName + "に受付されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.ioiHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildErrorMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>エラーが発生しました</h3>" +
                        "<p>チャットボット @" + botUserInfo.getDisplayName() + " の処理において問題が発生しました。</p>" +
                        "<p>システム管理者にお問い合わせください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildNotInRoomMessage(String commandText) {
        String message =
                "<h3>コマンド[" + commandText + "]は、このチャットルームでは使えません</h3>" +
                        "<p>このコマンドは、予め定められた社内チャットルームでの使用に限られています。</p>" +
                        "<p>詳しくは、システム管理者にお問い合わせください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildHelpMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h5>StockLendingBotは、貸手と借手の間で行われるRFQ（見積依頼）、IOI（貸株掲示）、ROLL（継続）のワークフローをお手伝いします。</h5>" +
                        "<p>チャットボット「@" + botUserInfo.getDisplayName() + "」に続けて以下の命令を指定してメッセージを送ってください。</p>" +
                        "<br/>" +
                        "<p style=\"color:#0000FF;\"> ============= 業務関連用の命令 ==============</p>" +
                        "<p><b>/newrfq</b>：   [借手用] 新規RFQ（見積依頼）の作成</p>" +
                        "<p><b>/newrfq</b>：   [貸手用] RFQ（見積依頼）への見積回答</p>" +
                        "<p><b>/viewrfq</b>：  [借手用] RFQ（見積依頼）とQUOTE（見積回答）の状況を表示</p>" +
                        "<p><b>/newioi</b>：   [貸手用] 新規IOI（貸株掲示）の作成</p>" +
                        "<p><b>/replyioi</b>： [借手用] 新規IOI（貸株掲示）への回答</p>" +
                        "<p><b>/newroll</b>：  [貸手用] 貸出中の株式を借手に確認</p>" +
                        "<br/>" +
                        "<p style=\"color:#FF0000;\">======= チャットボットメンテナンス用の命令 =======</p>" +
                        "<p><b>/initializesod</b>： 日次業務開始前の初期化</p>" +
                        "<p><b>/updateconfig</b>：  チャットボット環境設定の更新</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildNothingMessage(String userId, String userName, String requestId, String borrowerName, String lenderName) {

        String message =
                "<h3 class=\"tempo-text-color--black tempo-bg-color--yellow\">RFQへの回答："+ borrowerName + "[<hash tag=\"" + requestId + "\"/>]" + " ← " + lenderName + "</h3>" +
//                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                        "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<div style=\"display: flex;\">";

        message +=
                "<div style=\"width:50%;\">" +
                        "<form id=\"notify-nothing-form\">";
        if (ConfigLoader.env.equals("prod")) {
            message += "<div style=\"display:none\">";
        }
        message +=
                "<p>RequestID: BorrowerName: LenderName: </p>"+
                        "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" ;
        if (ConfigLoader.env.equals("prod")) {
            message += "</div>";
        }
        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">借手(" + borrowerName + ")の対応</h3>" +
                        "<br/>";
        message +=
                "<button type=\"action\" name=\"nothing-accept-button\">承諾</button>" +
                        "<br/>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>" +
                        "</form>" +
                        "</div>";

        message +=
                "<div style=\"width:50%;\">" +
//                         "<form id=\"notify-nothing-form\">" +
//                         "</form>" +
                        "<h5>" + lenderName + "(" + userName + ")が依頼番号[" + requestId + "]に<b>在庫無し</b>と回答しました。</h5>" +
                        "</div>";

        message +=
                "</div>";

        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNothingMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNotUnderstandMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>"  + botUserInfo.getUsername() + "が送信された命令を理解できませんでした。</h3>" +
                        "<p>利用可能な命令は、@" + botUserInfo.getDisplayName() + " <b>/help</b> とメッセージを送ることで確認できます。</p>" +
                        "<p>当チャットボットの詳細については、システム管理者にお問い合わせくだだい。</p>";
        OutboundMessage messageOut = new OutboundMessage();

        messageOut.setMessage(message);
        LOGGER.debug("buildNotUnderstandMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNoCounterPartyMessage() {
        String message =
                "<h3>貸手1が選ばれていません！</h3>" +
                        "<p>貸手1は、必須です。 貸手1を選択して再度送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNoCounterPartyMessage returned");
        return messageOut;
    }

    public OutboundMessage buildDoubtMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>エラーが発生しました</h3>" +
                        "<p>貼付られたRFQデータに誤りがあると思われます。 今一度、貼付データをご確認ください。</p>" +
                        "<p>再度、依頼を作成する場合は、<b>@" + botUserInfo.getDisplayName() + " /createrfq</b> のようにチャットボットに命令文を送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildDoubtMessage returned");
        System.out.println("test");
        return messageOut;
    }

    public OutboundMessage buildImToLenderBot(String cmd, String userId, String userName, String botId, String externalChatRoomId, String requestId, String borrowerName, String lenderName, String rfqsData) {
        String message = "<mention uid='" + botId + "'/> " + cmd + " " + requestId + " " + userId + " " + userName + " " + borrowerName + " " + lenderName + " " + externalChatRoomId + " " + rfqsData;
        OutboundMessage  messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildImToBorrowerBot(String cmd, String userId, String userName, String botId, String externalChatRoomId, String requestId, String borrowerName, String lenderName, String ioisData) {
        String message = "<mention uid='" + botId + "'/> " + cmd + " " + requestId + " " + userId + " " + userName + " " + borrowerName + " " + lenderName + " " + externalChatRoomId + " " + ioisData;
        OutboundMessage  messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }


    public OutboundMessage buildViewRfqFormMessage(String userName, String requestId, String borrowerName, String lenderName, String status, String csvFullPath, boolean isSelected) {

        String message;
        StringBuilder messageOptionsForLenders = new StringBuilder();
        StringBuilder messageOptionsForRequestId = new StringBuilder();
        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptionsForLenders.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
        }
        for (String optionRequestId : DataServices.getRequestIdList("RFQ")) {
            messageOptionsForRequestId.append("<option value=\"").append(optionRequestId).append("\">").append(optionRequestId).append("</option>");
        }

        message =
                "<h4>担当者： " + userName + "</h4>" +
                        "<br/>";
        message +=
                "<div style=\"display: flex;\">";
        // div for top left vvvvvvvvvvv
        // div for top left vvvvvvvvvvv
        // div for top left vvvvvvvvvvv
        message +=
                "<div style=\"width:35%;\">";
        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">RFQの表示</h3>";

        message +=
                "<table style='table-layout:fixed;width:550px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:8%;font-weight:bold'>種別</td>" +
                        //                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:13%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:12%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:10%;font-weight:bold'>開始日</td>" +
                        "<td style='width:18%;font-weight:bold'>返却日/期間</td>" +
//                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:17%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:5%;font-weight:bold'>枝番</td>" +
//                        "<td style='width:7%;font-weight:bold;text-align:right'>見積株数</td>" +
//                        "<td style='width:6%;font-weight:bold'>見積開始</td>" +
//                        "<td style='width:7%;font-weight:bold'>見積返却日</td>" +
//                        "<td style='width:6%;font-weight:bold;text-align:right'>見積利率</td>" +
//                        "<td style='width:7%;font-weight:bold'>見積条件</td>" +
//                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
//                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";



        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        for (ViewRfq viewRfq : dataServices.viewTargetRfqs(requestId)) {
            totalQtyborrower += viewRfq.getBorrowerQty();

            message +=
                    "<tr>" +
                            "<td>" + viewRfq.getType() + "</td>" +
//                        "<td class='tempo-text-color--green'>" + viewRfqQuote.getBorrowerName() + "</td>" +
                            "<td>" + viewRfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewRfq.getBorrowerQty()) + "</td>" +
                            "<td>" + viewRfq.getBorrowerStart() + "</td>" +
                            "<td>" + viewRfq.getBorrowerEnd() + "</td>" +
//                            "<td class='tempo-text-color--blue'>" + viewRfqQuote.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewRfq.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewRfq.getLineNo() + "</td>" +
//                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getLenderQty()) + "</td>" +
//                            "<td>" + viewRfqQuote.getLenderStart() + "</td>" +
//                            "<td>" + viewRfqQuote.getLenderEnd() + "</td>" +
//                            "<td style='text-align:right'>" + viewRfqQuote.getLenderRate() + "</td>" +
//                            "<td>" + viewRfqQuote.getLenderCondition() + "</td>" +
//                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getPrice()) + "</td>" +
//                            "<td>" + viewRfqQuote.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
//                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計：</td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
//                        "<td></td>" +
                        "<td></td>" +
//                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
//                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
//                        "<td></td>" +
//                        "<td></td>" +
//                        "<td></td>" +
//                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +

                        "</div>";
        // div for top left ^^^^^^^^^^^^
        // div for top left ^^^^^^^^^^^^
        // div for top left ^^^^^^^^^^^^

        // div for top right vvvvvvvvvv
        // div for top right vvvvvvvvvv
        // div for top right vvvvvvvvvv
        message +=
                "<div style=\"width:65%;\">";
        message +=
                "<h3 class=\"tempo-text-color--black tempo-bg-color--purple\">QUOTEの表示</h3>";
        message +=
                "<table style='table-layout:fixed;width:1000px' class='tempo-ui--background'>" +
                        "<thead>" +
                        "<tr>" +
//                        "<td style='width:4%;font-weight:bold'>種別</td>" +
//                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:10%;font-weight:bold'>銘柄</td>" +
//                        "<td style='width:8%;font-weight:bold;text-align:right'>株数</td>" +
//                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
//                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:10%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:10%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:5%;font-weight:bold'>枝番</td>" +
                        "<td style='width:8%;font-weight:bold;text-align:right;color:#3300ff'>見積株数</td>" +
                        "<td style='width:10%;font-weight:bold;color:#3300ff'>見積開始</td>" +
                        "<td style='width:12%;font-weight:bold;color:#3300ff'>見積返却日</td>" +
                        "<td style='width:10%;font-weight:bold;text-align:right;color:#3300ff'>見積利率</td>" +
                        "<td style='width:10%;font-weight:bold;color:#3300ff'>見積条件</td>" +
                        "<td style='width:10%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:8%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";



//        DataServices dataServices = DataServices.getInstance();
        int totalQtylender = 0;
        for (ViewRfqQuote viewRfqQuote : dataServices.viewRfqUpdatedByLender(requestId, lenderName, status)) {
            totalQtyborrower += viewRfqQuote.getBorrowerQty();
            totalQtylender += viewRfqQuote.getLenderQty();

            message +=
                    "<tr>" +
//                        "<td>" + viewRfqQuote.getType() + "</td>" +
//                        "<td class='tempo-text-color--green'>" + viewRfqQuote.getBorrowerName() + "</td>" +
                            "<td>" + viewRfqQuote.getStockCode() + "</td>" +
//                        "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getBorrowerQty()) + "</td>" +
//                        "<td>" + viewRfqQuote.getBorrowerStart() + "</td>" +
//                        "<td>" + viewRfqQuote.getBorrowerEnd() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewRfqQuote.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewRfqQuote.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewRfqQuote.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getLenderQty()) + "</td>" +
                            "<td>" + viewRfqQuote.getLenderStart() + "</td>" +
                            "<td>" + viewRfqQuote.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewRfqQuote.getLenderRate() + "</td>" +
                            "<td>" + viewRfqQuote.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getPrice()) + "</td>" +
                            "<td>" + viewRfqQuote.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
//                        "<td></td>" +
//                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'></td>" +
//                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
//                        "<td></td>" +
//                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" +
                        "</div>";
        // div for top right ^^^^^^^^^
        // div for top right ^^^^^^^^^
        // div for top right ^^^^^^^^^

        message +=
                "</div>";



//      Lower Section ==================================
//      Lower Section ==================================
//      Lower Section ==================================
        message +=
                "<div style=\"display: flex;\">";



//      Div in Left Bottom ============
//      Div in Left Bottom ============
//      Div in Left Bottom ============
        message +=
                "<div style=\"width:50%;\">";

        message +=
                "<form id=\"view-rfq-form\">";

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">借手の確認作業</h3>" +
                        "<h3>QUOTEのデータの絞り込み</h3>" +
                        "<br/>";
        message +=
                "<select name=\"lender-select\" required=\"false\" data-placeholder=\"貸手を選択\">";
        message += messageOptionsForLenders;
        message +=
                "</select>";

        message +=
                "<select name=\"request-id-select\" required=\"false\" data-placeholder=\"依頼番号を選択\">";
        message += messageOptionsForRequestId;
        message +=
                "</select>";

        message +=
                "<select name=\"status-select\" required=\"false\" data-placeholder=\"状況を選択\">";
        message +=
                "<option value=\"NEW\">NEW</option><option value=\"REJECT\">REJECT</option><option value=\"SELECT\">SELECT</option>" +
                        "<option value=\"SEND\">SEND</option><option value=\"DONE\">DONE</option>";
        message +=
                "</select>";

        message +=
//                "<button type=\"action\" name=\"reject-quote-button\">却下</button>" +
                "<button type=\"action\" name=\"refresh-rfq-button\">再表示</button>" +
                        "</form>";
        message +=
                "</div>" ;

//       Div in Right Bottom  -------------------
//       Div in Right Bottom  -------------------
//       Div in Right Bottom  -------------------
        if (isSelected) {
            message +=
                    "<br/>" +
                            "<div style=\"width:50%;\">"+
                            "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">意志決定の送信</h3>" +
                            "<h5>[送信]を選択すると、状況＝SELECTのQUOTEが全てSENDとなります。</h5>" +
                            "<h5>[取消]を選択すると、状況＝SELECTのQUOTEが全てNEWとなります。</h5>" ;


            message +=
                    "<form id=\"send-selection-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: borrowerName: lenderName: userName: </p>" +
//                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
//                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
//                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }
            message +=
                    "<button type=\"action\" name=\"cancel-selection-button\">取消</button><button type=\"action\" name=\"send-selection-button\">送信</button>";
            message +=
                    "</form>" +
                            "</div>";



        } else {
            message +=
                    "<br/>" +
                            "<div style=\"width:50%;\">"+
                            "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">借手の意志決定</h3>" ;

            message +=
                    "<form id=\"import-selection-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: borrowerName: lenderName: userName: </p>" +
//                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
//                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
//                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3>DONEのデータの入力</h3>" +
                            "<br/>" +
                            "<textarea name=\"inputSelection\" placeholder=\"ここにコピーしたDONEデータを貼付てください。 " +
                            "&#13;" +
                            "コピー範囲は、エクセルで開いたCSVファイルの先頭列（列名：種別）から最終列（列名：状況）を含めてください。\" required=\"false\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-selection-button\">取込</button><button type=\"action\" name=\"proceed-selection-button\">送信へ</button>";
            message +=
                    "</form>" +
                            "</div>";
        }

        message +=
                "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setAttachment(new File(csvFullPath));
        messageOut.setMessage(message);
        LOGGER.debug("buildViewRfqFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildCheckIoiFormMessage(String userName, String requestId, String borrowerName, String lenderName, String status, String csvFullPath, boolean isSelected) {

        String message;
        StringBuilder messageOptionsForBorrowers = new StringBuilder();
        StringBuilder messageOptionsForRequestId = new StringBuilder();
        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptionsForBorrowers.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
        }
        for (String optionRequestId : DataServices.getRequestIdList("IOI")) {
            messageOptionsForRequestId.append("<option value=\"").append(optionRequestId).append("\">").append(optionRequestId).append("</option>");
        }

        message =
            "<h4>担当者： " + userName + "</h4>" +
            "<br/>";
        message +=
            "<div style=\"display: flex;\">";

        // div for top left vvvvvvvvvvv
        // div for top left vvvvvvvvvvv
        // div for top left vvvvvvvvvvv

        message +=
                "<div style=\"width:50%;\">";
        message +=
                "<h3 class=\"tempo-text-color--black tempo-bg-color--yellow\">IOIへの配分状況</h3>";
        message +=
                "<table style='table-layout:fixed;width:800px' class='tempo-ui--background'>" +
                        "<thead>" +
                        "<tr>" +
    //                        "<td style='width:4%;font-weight:bold'>種別</td>" +
    //                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                            "<td style='width:8%;font-weight:bold;color:#3300ff'>銘柄</td>" +
    //                        "<td style='width:8%;font-weight:bold;text-align:right'>株数</td>" +
    //                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
    //                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                            "<td style='width:12%;font-weight:bold;color:#3300ff'>提示先</td>" +
                            "<td style='width:12%;font-weight:bold;color:#3300ff'>提示番号</td>" +
                            "<td style='width:7%;font-weight:bold;color:#3300ff'>枝番</td>" +
                            "<td style='width:10%;font-weight:bold;text-align:right;color:#3300ff'>株数</td>" +
                            "<td style='width:8%;font-weight:bold;color:#3300ff'>開始</td>" +
                            "<td style='width:10%;font-weight:bold;color:#3300ff'>返却日</td>" +
                            "<td style='width:8%;font-weight:bold;text-align:right;color:#3300ff'>利率</td>" +
                            "<td style='width:10%;font-weight:bold;color:#3300ff'>条件</td>" +
//                            "<td style='width:8%;font-weight:bold;text-align:right'>価格</td>" +
                            "<td style='width:10%;font-weight:bold;color:#3300ff'>状況</td>" +
                            "</tr>" +
                        "</thead>" +
                    "<tbody>";

        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        for (ViewIoi viewIoi : dataServices.viewIoiUpdatedByBorrower(requestId, lenderName, status)) {
            totalQtyborrower += viewIoi.getBorrowerQty();

            message +=
                    "<tr>" +
//                        "<td>" + viewIoi.getType() + "</td>" +
                        "<td>" + viewIoi.getStockCode() + "</td>" +
                        "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                        "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                        "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                        "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                        "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                        "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                        "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                        "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
//                        "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
//                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
//                            "<td>" + viewIoi.getLenderStart() + "</td>" +
//                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
//                        "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                        "<td>" + viewIoi.getStatus() + "</td>" +
                    "</tr>";
        }

        message +=
                    "</tbody>" +
                    "<tfoot>" +
                        "<tr>" +
            //                        "<td></td>" +
            //                        "<td></td>" +
            //                        "<td style='text-align:right;font-weight:bold'></td>" +
            //                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
            //                        "<td></td>" +
            //                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                    "</tfoot>" +
                    "</table>" +
                "<br/>" +
            "</div>";

        // div for top left ^^^^^^^^^^^^
        // div for top left ^^^^^^^^^^^^
        // div for top left ^^^^^^^^^^^^

        // div for top right vvvvvvvvvv
        // div for top right vvvvvvvvvv
        // div for top right vvvvvvvvvv
        message +=
                "<div style=\"width:50%;\">";
        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">IOIの表示</h3>";

        message +=
                "<table style='table-layout:fixed;width:800px'>" +
                    "<thead>" +
                        "<tr>" +
                        "<td style='width:8%;font-weight:bold'>種別</td>" +
                        "<td style='width:10%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:10%;font-weight:bold'>提示番号</td>" +
                        "<td style='width:5%;font-weight:bold'>枝番</td>" +
    //                        "<td style='width:2%;font-weight:bold'>提示元</td>" +
                        "<td style='width:10%;font-weight:bold;text-align:right'>提示株数</td>" +
                        "<td style='width:10%;font-weight:bold'>提示開始日</td>" +
                        "<td style='width:12%;font-weight:bold'>提示返却日/期間</td>" +
                        "<td style='width:8%;font-weight:bold'>提示利率</td>" +
                        "<td style='width:10%;font-weight:bold'>提示条件</td>" +
                        "<td style='width:10%;font-weight:bold;text-align:right'>株価</td>" +
    //                        "<td style='width:7%;font-weight:bold;text-align:right'>見積株数</td>" +
    //                        "<td style='width:6%;font-weight:bold'>見積開始</td>" +
    //                        "<td style='width:7%;font-weight:bold'>見積返却日</td>" +
    //                        "<td style='width:6%;font-weight:bold;text-align:right'>見積利率</td>" +
    //                        "<td style='width:7%;font-weight:bold'>見積条件</td>" +
    //                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
    //                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                    "</thead>" +
                    "<tbody>";

//        DataServices dataServices = DataServices.getInstance();
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.viewTargetIois(requestId)) {
            totalQtylender += viewIoi.getLenderQty();

            message +=
                        "<tr>" +
                                "<td>" + viewIoi.getType() + "</td>" +
    //                        "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                                "<td>" + viewIoi.getStockCode() + "</td>" +
                                "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                                "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
    //                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
    //                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
    //                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
    //                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
    //                            "<td>" + viewIoi.getStatus() + "</td>" +
                        "</tr>";
        }

        message +=
                     "</tbody>" +
                    "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
    //                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計：</td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
    //                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
    //                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
    //                        "<td></td>" +
    //                        "<td></td>" +
    //                        "<td></td>" +
    //                        "<td></td>" +
                        "</tr>" +
                    "</tfoot>" +
                "</table>" +

            "</div>";


        message +=
            "</div>";

//      Lower Section vvvvvvvvvvvvvvvvvvvvvv
//      Lower Section vvvvvvvvvvvvvvvvvvvvvv
//      Lower Section vvvvvvvvvvvvvvvvvvvvvv
        message +=
                "<div style=\"display: flex;\">";



//      Div in Left Bottom vvvvvvvvvvvvvvvvv
//      Div in Left Bottom vvvvvvvvvvvvvvvvv
//      Div in Left Bottom vvvvvvvvvvvvvvvvv
        if (isSelected) {
            message +=
                    "<br/>" +
                            "<div style=\"width:50%;\">"+
                            "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">意志決定の送信</h3>" +
                            "<h5>[送信]を選択すると、状況＝SELECTのIOIが全てSENDとなります。</h5>" +
                            "<h5>[取消]を選択すると、状況＝SELECTのIOIが全てNEWとなります。</h5>" ;


            message +=
                    "<form id=\"send-alloc-ioi-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: borrowerName: lenderName: userName: </p>" +
//                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
//                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
//                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }
            message +=
                    "<button type=\"action\" name=\"cancel-alloc-ioi-button\">取消</button><button type=\"action\" name=\"send-alloc-ioi-button\">送信</button>";
            message +=
                    "</form>" +
                            "</div>";



        } else {
            message +=
                    "<br/>" +
                            "<div style=\"width:50%;\">"+
                            "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">貸手の意志決定</h3>" ;

            message +=
                    "<form id=\"import-alloc-ioi-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: borrowerName: lenderName: userName: </p>" +
//                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
//                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
//                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3>配分データの入力</h3>" +
                            "<br/>" +
                            "<textarea name=\"input_alloc_ioi\" placeholder=\"ここにコピーした配分データを貼付てください。 " +
                            "&#13;" +
                            "コピー範囲は、エクセルで開いたCSVファイルの先頭列（列名：種別）から最終列（列名：状況）を含めてください。\" required=\"false\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-alloc-ioi-button\">取込</button><button type=\"action\" name=\"proceed-alloc-ioi-button\">送信へ</button>";
            message +=
                    "</form>" +
                            "</div>";
        }

//       Div in Right Bottom  vvvvvvvvvvvvvvvvvvvvvvv
//       Div in Right Bottom  vvvvvvvvvvvvvvvvvvvvvvv
//       Div in Right Bottom  vvvvvvvvvvvvvvvvvvvvvvv




        message +=
                "<div style=\"width:50%;\">";

        message +=
                "<form id=\"view-ioi-form\">";

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">貸手の確認作業</h3>" +
                        "<h3>IOIのデータの絞り込み</h3>" +
                        "<br/>";
        message +=
                "<select name=\"borrower-select\" required=\"false\" data-placeholder=\"借手を選択\">";
        message += messageOptionsForBorrowers;
        message +=
                "</select>";

        message +=
                "<select name=\"request-id-select\" required=\"false\" data-placeholder=\"提示番号を選択\">";
        message += messageOptionsForRequestId;
        message +=
                "</select>";

        message +=
                "<select name=\"status-select\" required=\"false\" data-placeholder=\"状況を選択\">";
        message +=
                "<option value=\"NEW\">NEW</option><option value=\"REJECT\">REJECT</option><option value=\"ACK\">ACK</option>" +
                        "<option value=\"SEND\">SEND</option><option value=\"TAKE\">TAKE</option><option value=\"ALLOC\">ALLOC</option>" +
                        "<option value=\"DONE\">DONE</option>";
        message +=
                "</select>";

        message +=
//                "<button type=\"action\" name=\"reject-quote-button\">却下</button>" +
                "<button type=\"action\" name=\"refresh-ioi-button\">再表示</button>" +
                        "</form>";
        message +=
                "</div>" ;




        message +=
                "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setAttachment(new File(csvFullPath));
        messageOut.setMessage(message);
        LOGGER.debug("buildCheckIoiFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildViewQuoteFormMessage(String userName, String requestId, String borrowerName, String lenderName, String status, String csvFullPath) {
        String message;
        String titleForm;
        StringBuilder messageOptionsForBorrowers = new StringBuilder();
        StringBuilder messageOptionsForRequestId = new StringBuilder();
        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptionsForBorrowers.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
        }
        for (String optionRequestId : DataServices.getRequestIdList("RFQ")) {
            messageOptionsForRequestId.append("<option value=\"").append(optionRequestId).append("\">").append(optionRequestId).append("</option>");
        }

        titleForm = "QUOTEの一覧 ";

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + "</h3>";

        message +=
                "<h4>閲覧者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1500px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:5%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:5%;font-weight:bold'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:7%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff;text-align:right'>見積株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>見積開始</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積返却日</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff;text-align:right'>見積利率</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right;color:#3300ff'>価格</td>" +
                        "<td style='width:6%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewRfqQuote receivedRfq : dataServices.viewReceivedRfqs(requestId, borrowerName, status)) {
            totalQtyborrower += receivedRfq.getBorrowerQty();
            totalQtylender += receivedRfq.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + receivedRfq.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + receivedRfq.getBorrowerName() + "</td>" +
                            "<td>" + receivedRfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getBorrowerQty()) + "</td>" +
                            "<td>" + receivedRfq.getBorrowerStart() + "</td>" +
                            "<td>" + receivedRfq.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + receivedRfq.getBorrowerRate() + "</td>" +
                            "<td>" + receivedRfq.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + receivedRfq.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + receivedRfq.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + receivedRfq.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getLenderQty()) + "</td>" +
                            "<td>" + receivedRfq.getLenderStart() + "</td>" +
                            "<td>" + receivedRfq.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + receivedRfq.getLenderRate() + "</td>" +
                            "<td>" + receivedRfq.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getPrice()) + "</td>" +
                            "<td>" + receivedRfq.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";

//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
        message +=
                "<div style=\"width:50%;\">"+
                        "</div>" ;

//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";
        message +=
                "<form id=\"view-quote-form\">";

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">貸手の操作</h3>" +
                        "<h3>QUOTEのデータの絞り込み</h3>" +
                        "<br/>";
        message +=
                "<select name=\"borrower-select\" required=\"false\" data-placeholder=\"借手を選択\">";
        message += messageOptionsForBorrowers;
        message +=
                "</select>";

        message +=
                "<select name=\"request-id-select\" required=\"false\" data-placeholder=\"依頼番号を選択\">";
        message += messageOptionsForRequestId;
        message +=
                "</select>";

        message +=
                "<select name=\"status-select\" required=\"false\" data-placeholder=\"状況を選択\">";
        message +=
                "<option value=\"NEW\">NEW</option><option value=\"REJECT\">REJECT</option><option value=\"SELECT\">SELECT</option>" +
                        "<option value=\"SEND\">SEND</option><option value=\"DONE\">DONE</option>";
        message +=
                "</select>";

        message +=
//                "<button type=\"action\" name=\"reject-quote-button\">却下</button>" +
                "<button type=\"action\" name=\"refresh-quote-button\">再表示</button>" +
                        "</form>";
//        message +=
//                "</div>" ;

        message +=
                "</div>"+
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        if (!csvFullPath.equals("")) {
            messageOut.setAttachment(new File(csvFullPath));
        }
        messageOut.setMessage(message);
        LOGGER.debug("buildViewQuoteFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildViewIoiFormMessage(String userName, String requestId, String borrowerName, String lenderName, String status, String csvFullPath) {
        String message;
        String titleForm;
        StringBuilder messageOptionsForLenders = new StringBuilder();
        StringBuilder messageOptionsForRequestId = new StringBuilder();
        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptionsForLenders.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
        }
        for (String optionRequestId : DataServices.getRequestIdList("IOI")) {
            messageOptionsForRequestId.append("<option value=\"").append(optionRequestId).append("\">").append(optionRequestId).append("</option>");
        }

        titleForm = "IOIの一覧 ";

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + "</h3>";

        message +=
                "<h4>閲覧者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1500px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>掲示先</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>銘柄</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right;color:#3300ff'>株数</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>掲示元</td>" +
                        "<td style='width:7%;font-weight:bold'>掲示番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>掲示株数</td>" +
                        "<td style='width:6%;font-weight:bold'>掲示開始</td>" +
                        "<td style='width:7%;font-weight:bold'>掲示返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right'>掲示利率</td>" +
                        "<td style='width:7%;font-weight:bold'>掲示条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:6%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.viewAllocIois(requestId, lenderName, status)) {
            totalQtyborrower += viewIoi.getBorrowerQty();
            totalQtylender += viewIoi.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewIoi.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                            "<td>" + viewIoi.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                            "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                            "<td>" + viewIoi.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";

//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
        message +=
                "<div style=\"width:50%;\">";
        message +=
                "<form id=\"view-alloc-ioi-form\">";

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">借手の操作</h3>" +
                        "<h3>IOIのデータの絞り込み</h3>" +
                        "<br/>";
        message +=
                "<select name=\"lender-select\" required=\"false\" data-placeholder=\"貸手を選択\">";
        message += messageOptionsForLenders;
        message +=
                "</select>";

        message +=
                "<select name=\"request-id-select\" required=\"false\" data-placeholder=\"掲示番号を選択\">";
        message += messageOptionsForRequestId;
        message +=
                "</select>";

        message +=
                "<select name=\"status-select\" required=\"false\" data-placeholder=\"状況を選択\">";
        message +=
                "<option value=\"NEW\">NEW</option><option value=\"REJECT\">REJECT</option><option value=\"ACK\">ACK</option>" +
                        "<option value=\"SEND\">SEND</option><option value=\"TAKE\">TAKE</option><option value=\"ALLOC\">ALLOC</option>" +
                        "<option value=\"DONE\">DONE</option>";
        message +=
                "</select>";

        message +=
//                "<button type=\"action\" name=\"reject-quote-button\">却下</button>" +
                "<button type=\"action\" name=\"refresh-alloc-ioi-button\">再表示</button>" +
                        "</form>";


        message +=
                "</div>";

//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
        message +=
                "<br/>" +
                "<div style=\"width:50%;\">";


        message +=
                "</div>";

        message +=
                "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        if (!csvFullPath.equals("")) {
            messageOut.setAttachment(new File(csvFullPath));
        }
        messageOut.setMessage(message);
        LOGGER.debug("buildViewIoiFormMessage returned");
        return messageOut;
    }
    
    public OutboundMessage buildCreateQuoteFormMessage() {
        LOGGER.debug("buildCreateQuoteFormMessage returned");
        return this.buildCreateQuoteFormMessage("", "", "", "","", "", "", true);
    }

    public OutboundMessage buildCreateQuoteFormMessage(String botId, String userId, String userName, String borrowerName, String lenderName, String lenderStatus, String csvFullPath, boolean isNew) {
        String message;
        String titleForm;

        if (isNew) {
            titleForm = "QUOTEの作成: ";
        } else {
            titleForm = "QUOTEの送信: ";
        }

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + "依頼者（一社／数社）← " + lenderName + "</h3>";

        message +=
//                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>回答者： " + userId + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:5%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:8%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:8%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff;text-align:right'>見積株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>見積開始</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right;color:#3300ff'>見積利率</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ReceivedRfq receivedRfq : dataServices.getReceivedRfqs(lenderStatus)) {
            totalQtyborrower += receivedRfq.getBorrowerQty();
            totalQtylender += receivedRfq.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + receivedRfq.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + receivedRfq.getBorrowerName() + "</td>" +
                            "<td>" + receivedRfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getBorrowerQty()) + "</td>" +
                            "<td>" + receivedRfq.getBorrowerStart() + "</td>" +
                            "<td>" + receivedRfq.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + receivedRfq.getBorrowerRate() + "</td>" +
                            "<td>" + receivedRfq.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + receivedRfq.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + receivedRfq.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + receivedRfq.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getLenderQty()) + "</td>" +
                            "<td>" + receivedRfq.getLenderStart() + "</td>" +
                            "<td>" + receivedRfq.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + receivedRfq.getLenderRate() + "</td>" +
                            "<td>" + receivedRfq.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getPrice()) + "</td>" +
                            "<td>" + receivedRfq.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
        message +=
                "<div style=\"width:50%;\">"+
                        "</div>" ;

//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";

        if (isNew) {
            message +=
                    "<form id=\"create-quote-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: borrowerName: lenderName: userName: </p>" +
                            "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">QUOTEのデータの入力</h3>" +
                            "<br/>" +
//                                "<h6>入力欄にQUOTEデータをコピー／貼付してください</h6>" +
                            "<textarea name=\"inputQuote\" placeholder=\"ここにコピーしたQUOTEデータを貼付てください。 " +
                            "&#13;" +
                            "コピー範囲は、エクセルで開いたCSVファイルの先頭列（列名：種別）から最終列（列名：状況）を含めてください。\" required=\"true\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-quote-button\">取込</button>";

            message +=
                    "</form>";
        }
        if (!isNew) {

            message +=
                    "<form id=\"submit-quote-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>borrowerName: lenderName: userName: </p>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">QUOTEを借手に送信</h3>" +
                            "<br/>";

            message +=

                    "<button type=\"action\" name=\"recreate-quote-button\">再作成</button>" +
                            "<button type=\"action\" name=\"send-quote-button\">送信</button>" +
                            "</form>";
        }

        message +=
                "</div>"+


                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        if (!csvFullPath.equals("")) {
            messageOut.setAttachment(new File(csvFullPath));
        }
        messageOut.setMessage(message);
        LOGGER.debug("buildCreateQuoteFormMessage returned");
        return messageOut;
    }


    public OutboundMessage buildSelectIoiFormMessage(String botId, String userId, String userName, String borrowerName, String lenderName, String status, String csvFullPath, boolean isNew) {
        String message;
        String titleForm;

        if (isNew) {
            titleForm = "IOIの選択: ";
        } else {
            titleForm = "IOIへの回答: ";
        }

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + borrowerName + " → 貸手（一社／数社） </h3>";

        message +=
//                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>提示先</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>銘柄</td>" +
                        "<td style='width:5%;font-weight:bold;;text-align:right;color:#3300ff'>株数</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>提示元</td>" +
                        "<td style='width:8%;font-weight:bold'>提示番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>提示株数</td>" +
                        "<td style='width:6%;font-weight:bold'>提示開始</td>" +
                        "<td style='width:7%;font-weight:bold'>提示返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right'>提示利率</td>" +
                        "<td style='width:6%;font-weight:bold'>提示条件</td>" +
                        "<td style='width:4%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:6%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.getReceivedIois(status)) {
            totalQtyborrower += viewIoi.getBorrowerQty();
            totalQtylender += viewIoi.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewIoi.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                            "<td>" + viewIoi.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                            "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                            "<td>" + viewIoi.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
        message +=
                "<div style=\"width:50%;\">";

        if (isNew) {
            message +=
                    "<form id=\"select-ioi-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: borrowerName: lenderName: userName: </p>" +
                            "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">IOIデータの入力</h3>" +
                            "<br/>" +
//                                "<h6>入力欄にIOIデータをコピー／貼付してください</h6>" +
                            "<textarea name=\"input_ioi\" placeholder=\"ここにコピーしたIOIデータを貼付てください。 " +
                            "&#13;" +
                            "コピー範囲は、エクセルで開いたCSVファイルの先頭列（列名：種別）から最終列（列名：状況）を含めてください。\" required=\"true\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-select-ioi-button\">取込</button>";

            message +=
                    "</form>";
        }
        if (!isNew) {

            message +=
                    "<form id=\"submit-select-ioi-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>borrowerName: lenderName: userName: </p>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">貸手のIOIに回答</h3>" +
                            "<br/>";

            message +=

                    "<button type=\"action\" name=\"reselect-select-ioi-button\">再選択</button>" +
                            "<button type=\"action\" name=\"send-select-ioi-button\">回答</button>" +
                            "</form>";
        }
        message +=
                "</div>" ;

//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";
        message +=
                "</div>"+

        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        if (!csvFullPath.equals("")) {
            messageOut.setAttachment(new File(csvFullPath));
        }
        messageOut.setMessage(message);
        LOGGER.debug("buildCreateQuoteFormMessage returned");
        return messageOut;
    }


    public OutboundMessage buildSendSelectionFormMessage(String botId, String userId, String userName, String externalChatRoomId,
                                                         String lenderBotInstantMessageId, String borrowerName, String lenderName, String status, String selectionData) {

        String message;
        String titleForm;

        titleForm = "RESULTSの回答: ";

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">" + titleForm + borrowerName+ " → " + lenderName + "</h3>";



        message +=
                //                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:5%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:7%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right;color:#3300ff'>見積株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>見積開始</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right;color:#3300ff'>見積利率</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:6%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";



        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewRfqQuote viewRfqQuote : dataServices.viewSendSelectionFromBorrowerToLender(lenderName, status)) {
            totalQtyborrower += viewRfqQuote.getBorrowerQty();
            totalQtylender += viewRfqQuote.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewRfqQuote.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewRfqQuote.getBorrowerName() + "</td>" +
                            "<td>" + viewRfqQuote.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getBorrowerQty()) + "</td>" +
                            "<td>" + viewRfqQuote.getBorrowerStart() + "</td>" +
                            "<td>" + viewRfqQuote.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewRfqQuote.getBorrowerRate() + "</td>" +
                            "<td>" + viewRfqQuote.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewRfqQuote.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewRfqQuote.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewRfqQuote.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getLenderQty()) + "</td>" +
                            "<td>" + viewRfqQuote.getLenderStart() + "</td>" +
                            "<td>" + viewRfqQuote.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewRfqQuote.getLenderRate() + "</td>" +
                            "<td>" + viewRfqQuote.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewRfqQuote.getPrice()) + "</td>" +
                            "<td>" + viewRfqQuote.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


        //      Div in Left Bottom -----------------
        //      Div in Left Bottom -----------------
        //      Div in Left Bottom -----------------
        message +=
                "<div style=\"width:50%;\">";


        message +=
                "</div>" ;

        //       Div in Right Bottom -----------------
        //       Div in Right Bottom -----------------
        //       Div in Right Bottom -----------------
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";

        message +=
                "<form id=\"confirm-selection-form\">";
        if (ConfigLoader.env.equals("prod")) {
            message += "<div style=\"display:none\">";
        }
        message +=
                "<p>userName: botId: externalChatRoomId: borrowerBotInstantMessageId: borrowerName: lenderName: quoteData: </p>" +
                        "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                        "<text-field name=\"external_chatroom_id\" required=\"false\">" + externalChatRoomId + "</text-field>" +
                        "<text-field name=\"lenderbot_im_id\" required=\"false\">" + lenderBotInstantMessageId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                        "<textarea name=\"selection_data\" required=\"false\">" + selectionData + "</textarea>";
        if (ConfigLoader.env.equals("prod")) {
            message += "</div>";
        }

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--blue\">貸手(" + lenderName + ")の対応</h3>" +
                        "<br/>";

        message +=

                "<button type=\"action\" name=\"confirm-selection-button\">確認</button>" +
                        "</form>";

        message +=
                "</div>"+
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildSendQuoteFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildSendSelectedIoiFormMessage(String botId, String userId, String userName, String externalChatRoomId,
                                                         String lenderBotInstantMessageId, String borrowerName, String lenderName, String status, String selectionData) {

        String message;
        String titleForm;

        titleForm = "IOIへの回答: ";

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + borrowerName+ " → " + lenderName + "</h3>";



        message +=
                //                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>提示先</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>銘柄</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right;color:#3300ff'>株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>提示元</td>" +
                        "<td style='width:7%;font-weight:bold'>提示番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>提示株数</td>" +
                        "<td style='width:6%;font-weight:bold'>提示開始</td>" +
                        "<td style='width:7%;font-weight:bold'>提示返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right'>提示利率</td>" +
                        "<td style='width:7%;font-weight:bold'>提示条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:6%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";



        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.viewSendSelectedIoiFromBorrowerToLender(lenderName, status)) {
            totalQtyborrower += viewIoi.getBorrowerQty();
            totalQtylender += viewIoi.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewIoi.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                            "<td>" + viewIoi.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                            "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                            "<td>" + viewIoi.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


        //      Div in Left Bottom -----------------
        //      Div in Left Bottom -----------------
        //      Div in Left Bottom -----------------
        message +=
                "<div style=\"width:50%;\">";


        message +=
                "</div>" ;

        //       Div in Right Bottom -----------------
        //       Div in Right Bottom -----------------
        //       Div in Right Bottom -----------------
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";

        message +=
                "<form id=\"confirm-selected-ioi-form\">";
        if (ConfigLoader.env.equals("prod")) {
            message += "<div style=\"display:none\">";
        }
        message +=
                "<p>userName: botId: externalChatRoomId: borrowerBotInstantMessageId: borrowerName: lenderName: quoteData: </p>" +
                        "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                        "<text-field name=\"external_chatroom_id\" required=\"false\">" + externalChatRoomId + "</text-field>" +
                        "<text-field name=\"lenderbot_im_id\" required=\"false\">" + lenderBotInstantMessageId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                        "<textarea name=\"selection_data\" required=\"false\">" + selectionData + "</textarea>";
        if (ConfigLoader.env.equals("prod")) {
            message += "</div>";
        }

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--blue\">貸手(" + lenderName + ")の対応</h3>" +
                        "<br/>";

        message +=

                "<button type=\"action\" name=\"confirm-selected-ioi-button\">確認</button>" +
                        "</form>";

        message +=
                "</div>"+
            "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildSendQuoteFormMessage returned");
        return messageOut;
    }


    public OutboundMessage buildSendAllocIoiFormMessage(String botId, String userId, String userName, String externalChatRoomId,
                                                           String lenderBotInstantMessageId, String borrowerName, String lenderName, String status, String selectionData) {

        String message;
        String titleForm;

        titleForm = "IOIの配分: ";

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + borrowerName+ " ← " + lenderName + "</h3>";



        message +=
                //                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>提示先</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>銘柄</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right;color:#3300ff'>株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>提示元</td>" +
                        "<td style='width:7%;font-weight:bold'>提示番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>提示株数</td>" +
                        "<td style='width:6%;font-weight:bold'>提示開始</td>" +
                        "<td style='width:7%;font-weight:bold'>提示返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right'>提示利率</td>" +
                        "<td style='width:7%;font-weight:bold'>提示条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:6%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";



        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.viewSendSelectedIoiFromBorrowerToLender(lenderName, status)) {
            totalQtyborrower += viewIoi.getBorrowerQty();
            totalQtylender += viewIoi.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewIoi.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                            "<td>" + viewIoi.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                            "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                            "<td>" + viewIoi.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


        //      Div in Left Bottom -----------------
        //      Div in Left Bottom -----------------
        //      Div in Left Bottom -----------------
        message +=
                "<div style=\"width:50%;\">";
        message +=
                "<form id=\"confirm-alloc-ioi-form\">";
        if (ConfigLoader.env.equals("prod")) {
            message += "<div style=\"display:none\">";
        }
        message +=
                "<p>userName: botId: externalChatRoomId: borrowerBotInstantMessageId: borrowerName: lenderName: quoteData: </p>" +
                        "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                        "<text-field name=\"external_chatroom_id\" required=\"false\">" + externalChatRoomId + "</text-field>" +
                        "<text-field name=\"lenderbot_im_id\" required=\"false\">" + lenderBotInstantMessageId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                        "<textarea name=\"selection_data\" required=\"false\">" + selectionData + "</textarea>";
        if (ConfigLoader.env.equals("prod")) {
            message += "</div>";
        }

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--blue\">借手(" + borrowerName + ")の対応</h3>" +
                        "<br/>";

        message +=

                "<button type=\"action\" name=\"confirm-alloc-ioi-button\">確認</button>" +
                        "</form>";

        message +=
                "</div>" ;

        //       Div in Right Bottom -----------------
        //       Div in Right Bottom -----------------
        //       Div in Right Bottom -----------------
        message +=
                "<br/>" +
                "<div style=\"width:50%;\">";



        message +=
                "</div>"+
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildSendQuoteFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildSendQuoteFormMessage(String botId, String userId, String userName, String externalChatRoomId,
                                                     String borrowerBotInstantMessageId, String borrowerName, String lenderName, String lenderStatus, String quoteData, boolean isNew) {

        String message;
        String titleForm;

        if (isNew) {
            titleForm = "QUOTEの回答: ";
        } else {
            titleForm = "QUOTEの更新: ";
        }

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + borrowerName + " ← " + lenderName + "</h3>";



        message +=
//                "<h4>回答者： <mention uid='" + userId + "'/></h4>" +
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:5%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:8%;font-weight:bold;text-align:right'>株数</td>" +
                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:8%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right;color:#3300ff'>見積株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>見積開始</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right;color:#3300ff'>見積利率</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>見積条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (SendQuote sendQuote : dataServices.getQuoteForBorrower(borrowerName, "SEND")) {
            totalQtyborrower += sendQuote.getBorrowerQty();
            totalQtylender += sendQuote.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + sendQuote.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + sendQuote.getBorrowerName() + "</td>" +
                            "<td>" + sendQuote.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendQuote.getBorrowerQty()) + "</td>" +
                            "<td>" + sendQuote.getBorrowerStart() + "</td>" +
                            "<td>" + sendQuote.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + sendQuote.getBorrowerRate() + "</td>" +
                            "<td>" + sendQuote.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + sendQuote.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + sendQuote.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + sendQuote.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendQuote.getLenderQty()) + "</td>" +
                            "<td>" + sendQuote.getLenderStart() + "</td>" +
                            "<td>" + sendQuote.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + sendQuote.getLenderRate() + "</td>" +
                            "<td>" + sendQuote.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendQuote.getPrice()) + "</td>" +
                            "<td>" + sendQuote.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


//      Div in Left Bottom -----------------
//      Div in Left Bottom -----------------
//      Div in Left Bottom -----------------
        message +=
                "<div style=\"width:50%;\">";

        message +=
                "<form id=\"receive-quote-form\">";
        if (ConfigLoader.env.equals("prod")) {
            message += "<div style=\"display:none\">";
        }
        message +=
                "<p>userName: botId: externalChatRoomId: borrowerBotInstantMessageId: borrowerName: lenderName: quoteData: </p>" +
                        "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                        "<text-field name=\"external_chatroom_id\" required=\"false\">" + externalChatRoomId + "</text-field>" +
                        "<text-field name=\"borrowerbot_im_id\" required=\"false\">" + borrowerBotInstantMessageId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                        "<textarea name=\"quote_data\" required=\"false\">" + quoteData + "</textarea>";
        if (ConfigLoader.env.equals("prod")) {
            message += "</div>";
        }

        message +=
                "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">借手(" + borrowerName + ")の対応</h3>" +
                        "<br/>";

        message +=

                "<button type=\"action\" name=\"reject-quote-button\">却下</button>" +
                        "<button type=\"action\" name=\"accept-quote-button\">受付</button>" +
                        "</form>";
        message +=
                "</div>" ;

//       Div in Right Bottom
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";



        message +=
                "</div>"+
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildSendQuoteFormMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNotifyRejectMessage(String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>" + lenderName + "からの<b>QUOTEの回答</b>は、" + borrowerName + "(" + userName + ")に却下されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyRejectMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNotifyRejectIoiMessage(String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>" + lenderName + "からの<b>IOIの送信</b>は、" + borrowerName + "(" + userName + ")に需要無しと回答されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.ioiHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyRejectIoiMessage returned");
        return messageOut;
    }

    public OutboundMessage buildImToBorrowerBotForRejectQuote(String userId, String userName, String botId, String externalChatRoomId, String borrowerName, String lenderName, String quoteData) {
        String message = "<mention uid='" + botId + "'/> /botcmd2 DUMMY " + userId + " " + userName + " " + borrowerName + " " + lenderName + " " + externalChatRoomId + " " + quoteData;
        OutboundMessage  messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildImToBorrowerBotForRejectQuote returned");

        return messageOut;
    }

    public OutboundMessage buildImToBorrowerBotForRejectIoi(String userId, String userName, String botId, String externalChatRoomId, String borrowerName, String lenderName, String ioiData) {
        String message = "<mention uid='" + botId + "'/> /botcmd5 DUMMY " + userId + " " + userName + " " + borrowerName + " " + lenderName + " " + externalChatRoomId + " " + ioiData;
        OutboundMessage  messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildImToBorrowerBotForRejectIoi returned");

        return messageOut;
    }

    public OutboundMessage buildNotifyAcceptMessage(String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>" + lenderName + "からの<b>QUOTEの回答</b>は、" + borrowerName + "(" + userName + ")に受付されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyAcceptMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNotifyAcceptMessageIoi(String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>" + lenderName + "からの<b>IOIの送信</b>は、" + borrowerName + "(" + userName + ")に受付されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.ioiHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyAcceptMessageIoi returned");
        return messageOut;
    }

    public OutboundMessage buildNotifyAcceptSelectionMessage(String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>" + borrowerName + "からの<b>RESULTSの送信</b>は、" + lenderName + "(" + userName + ")に確認されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyAcceptSelectionMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNotifyAllocIoiMessage(String userId, String userName, String borrowerName, String lenderName) {
        String message =
            "<h5>" + lenderName + "からの<b>IOIの配分</b>は、" + borrowerName + "(" + userName + ")に確認されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.ioiHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyAllocIoiMessage returned");
        return messageOut;
    }

    public OutboundMessage buildNotifyAcceptSelectedIoiMessage(String userId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>" + borrowerName + "からの<b>IOIへの回答は送信</b>は、" + lenderName + "(" + userName + ")に確認されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.ioiHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildNotifyAcceptSelectedIoiMessage returned");
        return messageOut;
    }

    public OutboundMessage buildImToBorrowerBotForAccept(String userId, String userName, String botId, String externalChatRoomId, String borrowerName, String lenderName, String quoteData) {
        String message = "<mention uid='" + botId + "'/> /botcmd3 DUMMY " + userId + " " + userName + " " + borrowerName + " " + lenderName + " " + externalChatRoomId + " " + quoteData;
        OutboundMessage  messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildImToBorrowerBotForAccept returned");

        return messageOut;
    }

    public OutboundMessage buildImToBorrowerBotForAcceptIoi(String userId, String userName, String botId, String externalChatRoomId, String borrowerName, String lenderName, String quoteData) {
        String message = "<mention uid='" + botId + "'/> /botcmd6 DUMMY " + userId + " " + userName + " " + borrowerName + " " + lenderName + " " + externalChatRoomId + " " + quoteData;
        OutboundMessage  messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildImToBorrowerBotForAcceptIoi returned");

        return messageOut;
    }

    public OutboundMessage buildCreateIoiFormMessage(String botId, String userId, String userName, String requestId, String lenderName, String status, boolean isNew) {
        String message;
        String titleForm;
        StringBuilder messageOptionsForBorrowers = new StringBuilder();
        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptionsForBorrowers.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
        }

        String displayRequestId;

        displayRequestId = requestId;


        if (isNew) {
            titleForm = "IOIの作成: ";
        } else {
            titleForm = "IOIの送信: ";
        }

        if (displayRequestId.equals("")) {
            message =
                    "<h3 class=\"tempo-text-color--black tempo-bg-color--cyan\">" + titleForm + "借手（一社／数社）← " + lenderName + " []</h3>";
        } else {
            message =
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + "借手（一社／数社）← " + lenderName+ " [<hash tag=\"" + displayRequestId + "\"/>]</h3>";
        }

        message +=
//                "<h4>担当者： <mention uid='" + userId + "'/></h4>" +
                "<h4>担当者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1400px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>提示先</td>" +
                        "<td style='width:5%;font-weight:bold;color:#3300ff'>銘柄</td>" +
                        "<td style='width:8%;font-weight:bold;text-align:right;color:#3300ff'>株数</td>" +
                        "<td style='width:6%;font-weight:bold;color:#3300ff'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold;color:#3300ff'>返却日/期間</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff;text-align:right'>利率</td>" +
                        "<td style='width:4%;font-weight:bold;color:#3300ff'>条件</td>" +
                        "<td style='width:6%;font-weight:bold'>提示元</td>" +
                        "<td style='width:8%;font-weight:bold'>提示番号</td>" +
                        "<td style='width:3%;font-weight:bold'>枝番</td>" +
                        "<td style='width:7%;font-weight:bold;text-align:right'>提示株数</td>" +
                        "<td style='width:6%;font-weight:bold'>提示開始</td>" +
                        "<td style='width:7%;font-weight:bold'>提示返却日</td>" +
                        "<td style='width:6%;font-weight:bold;text-align:right'>提示利率</td>" +
                        "<td style='width:7%;font-weight:bold'>提示条件</td>" +
                        "<td style='width:5%;font-weight:bold;text-align:right'>価格</td>" +
                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (ViewIoi viewIoi : dataServices.viewImportIoi(requestId,0)) {
            totalQtyborrower += viewIoi.getBorrowerQty();
            totalQtylender += viewIoi.getLenderQty();

            message +=
                    "<tr>" +
                            "<td>" + viewIoi.getType() + "</td>" +
                            "<td class='tempo-text-color--green'>" + viewIoi.getBorrowerName() + "</td>" +
                            "<td>" + viewIoi.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getBorrowerQty()) + "</td>" +
                            "<td>" + viewIoi.getBorrowerStart() + "</td>" +
                            "<td>" + viewIoi.getBorrowerEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getBorrowerRate() + "</td>" +
                            "<td>" + viewIoi.getBorrowerCondition() + "</td>" +
                            "<td class='tempo-text-color--blue'>" + viewIoi.getLenderName() + "</td>" +
                            "<td style='color:#ff0000'>" + viewIoi.getRequestId() + "</td>" +
                            "<td style='text-align:right;color:#ff0000'>" + viewIoi.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getLenderQty()) + "</td>" +
                            "<td>" + viewIoi.getLenderStart() + "</td>" +
                            "<td>" + viewIoi.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + viewIoi.getLenderRate() + "</td>" +
                            "<td>" + viewIoi.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", viewIoi.getPrice()) + "</td>" +
                            "<td>" + viewIoi.getStatus() + "</td>" +
                            "</tr>";
        }

        message +=
                "</tbody>" +
                        "<tfoot>" +
                        "<tr>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyborrower) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtylender) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "</tr>" +
                        "</tfoot>" +
                        "</table>" +
                        "<br/>" ;

        message +=
                "<div style=\"display: flex;\">";


//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
//      Div in Left Bottom ---------------------
        message +=
                "<div style=\"width:50%;\">"+
                        "</div>" ;

//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
//       Div in Right Bottom -------------------
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";

        if (isNew) {

            message +=
                    "<form id=\"create-ioi-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>botId: requestId: lenderName: userName: </p>" +
                            "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                            "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">IOIのデータの入力</h3>" +
                            "<br/>" +
//                                "<h6>入力欄にIOIデータをコピー／貼付してください</h6>" +
                            "<textarea name=\"inputIoi\" placeholder=\"ここにコピーしたIOIデータを貼付てください。 " +
                            "&#13;" +
                            "コピー範囲は、エクセルで開いたCSVファイルの先頭列（列名：種別）から最終列（列名：状況）を含めてください。\" required=\"true\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-ioi-button\">取込</button>";

            message +=
                    "</form>";
        }
        if (!isNew) {

            message +=
                    "<form id=\"submit-ioi-form\">";
            if (ConfigLoader.env.equals("prod")) {
                message += "<div style=\"display:none\">";
            }
            message +=
                    "<p>requestId: lenderName: userName: </p>" +
                            "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                            "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>";
            if (ConfigLoader.env.equals("prod")) {
                message += "</div>";
            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">IOIを借手に送信</h3>" +
                            "<br/>";

            message +=
                    "<select name=\"borrower1-select\" required=\"false\" data-placeholder=\"借手1を選択\">";
            message += messageOptionsForBorrowers;
            message +=
                    "</select>" +

                            "<select name=\"borrower2-select\" required=\"false\" data-placeholder=\"借手2を選択\">";
            message += messageOptionsForBorrowers;
            message +=
                    "</select>" +

                            "<select name=\"borrower3-select\" required=\"false\" data-placeholder=\"借手3を選択\">";
            message += messageOptionsForBorrowers;
            message +=
                    "</select>" +

                            "<select name=\"borrower4-select\" required=\"false\" data-placeholder=\"借手4を選択\">";
            message += messageOptionsForBorrowers;
            message +=
                    "</select>" +

                            "<select name=\"borrower5-select\" required=\"false\" data-placeholder=\"借手5を選択\">";
            message += messageOptionsForBorrowers;
            message +=
                    "</select>";

            message +=

                    "<button type=\"action\" name=\"recreate-ioi-button\">再作成</button>" +
                            "<button type=\"action\" name=\"cancel-ioi-button\">取消</button>" +
                            "<button type=\"action\" name=\"send-ioi-button\">送信</button>" +
                            "</form>";
        }

        message +=
                "</div>"+
                        "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildCreateIoiFormMessage returned");
        return messageOut;
    }



    public OutboundMessage buildTestMessage() {
        String message = "<mention uid='349026222350969'/> /newrfq 123,123,123,123\n345,345,345,345\n456,456,456,456";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

}

