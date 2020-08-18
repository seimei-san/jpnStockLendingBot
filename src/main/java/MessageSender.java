import clients.SymBotClient;
import dataservices.DataServices;
import dataservices.Rfq;
import model.OutboundMessage;
import model.UserInfo;
import scripts.ConfigLoader;

import java.text.DecimalFormat;

public class MessageSender {
    private static MessageSender messageSender;
    private static MessageManager messageManager;
    private SymBotClient botClient;


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
    public static MessageSender createInstance(SymBotClient botClient) {
        if (messageSender == null) {
            messageSender = new MessageSender(botClient);
            return messageSender;
        } else {
            return messageSender;
        }
    }
    public void sendMessage(String streamId, OutboundMessage messageOut) {
        try {
            this.botClient.getMessagesClient().sendMessage(streamId, messageOut);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public OutboundMessage buildInitializeConfigMessage(String msg1, String msg2) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
            "Hi, " + msg1 + "<br/>" + msg2;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildInitializeSodMessage(String msg1, String msg2, String msg3, String msg4, String msg5, String msg6, String msgFoot) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                " SOD Initialization executed: <br/>" + msg1 +
                        "<br/>" + msg2 + "<br/>" + msg3 + "<br/>" + msg4 + "<br/>" + msg5 + "<br/>" + msg6 + "<br/>" + msgFoot;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }



    public OutboundMessage buildCreateRfqFormMessage() {
        return this.buildCreateRfqFormMessage(false, "", false, true);
    }
    
    public OutboundMessage buildCreateRfqFormMessage(boolean errorOnProviders, String requestId, boolean isInserted, boolean isNew) {
        MessageManager messageManager = MessageManager.getInstance();
        DataServices ds = DataServices.getInstance();


        String message = "";

        String messageOptions = "";

        for (int i=0; i < ds.counterPartiesList.length; i++) {
            messageOptions += "<option value=\"" + ds.counterPartiesList[i] + "\">" + ds.counterPartiesList[i] + "</option>";
        }

        String displayRequestId = "";
        if (requestId!= ConfigLoader.NO_REQUESTID) {
            displayRequestId = requestId;
        }

        if (isNew) {
            message =
                    "<h3 class=\"tempo-text-color-white tempo-bg-color--green\">RFQの作成: " + displayRequestId + "</h3>";
        } else {
            message =
                    "<h3 class=\"tempo-text-color-white tempo-bg-color--red\">RFQの送信:   " + displayRequestId + "</h3>";
        }



        message +=
                "<h4>作成者： " + messageManager.getRequesterName() + "</h4>" +
                "<br/>" +
                "<table style='table-layout:fixed;width:800px'>" +
                    "<thead>" +
                        "<tr>" +
                            "<td style='width:5%;font-weight:bold'>種別</td>" +
                            "<td style='width:5%;font-weight:bold'>行番</td>" +
                            "<td style='width:10%;font-weight:bold'>銘柄</td>" +
                            "<td style='width:15%;font-weight:bold'>株数</td>" +
                            "<td style='width:10%;font-weight:bold'>開始日</td>" +
                            "<td style='width:50%;font-weight:bold'>返却日/期間</td>" +
                        "</tr>" +
                    "</thead>" +
                    "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQty = 0;
        for (Rfq rfq : dataServices.getTargetRfqs(requestId, 0)) {
            totalQty += rfq.getRequesterQty();
            message +=
                        "<tr>" +
                            "<td>" + rfq.getType() + "</td>" +
                            "<td style='text-align:right'>" + rfq.getLineNo() + "</td>" +
                            "<td>" + rfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d",rfq.getRequesterQty()) + "</td>" +
                            "<td>" + rfq.getRequesterStart() + "</td>" +
                            "<td>" + rfq.getRequesterEnd() + "</td>" +
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

        /**
         * Div in Left Bottom
         */
        if (isNew) {
            message +=
                    "<form id=\"create-rfq-form\">";

            if (!isInserted) {
                if (requestId == "") {
                    requestId = ConfigLoader.NO_REQUESTID;
                }
                message +=
                        "<h3>依頼番号</h3>" +
                        "<p class='tempo-text-color--red'>編集不可：依頼番号は自動的に割り当てられます。 </p>" +
                        "<text-field name=\"request_id\" maxlength=\"9\" required=\"true\">" + requestId + "</text-field>";
            }

            message +=
                    "<h3>RFQデータの入力</h3>" +
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

        message +=
                 "</div>";

        /**
         *  Div in Right Bottom
         */
        message +=
                "<div style=\"width:50%;\">";

        if (!isNew) {

            if (errorOnProviders) {
                message +=
                        "<span class=\"tempo-text-color--red\">" +
                                "You need to choose a least on Provider1 before to submit your RFQ form." +
                                "</span>" ;
            }

            message +=
                    "<form id=\"submit-rfq-form\">";

            message +=
                    "<h3>依頼番号</h3>" +
                    "<p class='tempo-text-color--red'>編集不可：依頼番号は自動的に割り当てられます。 </p>" +
                    "<text-field name=\"request_id\" maxlength=\"9\" required=\"true\">" + requestId + "</text-field>"+
                    "<h3>RFQを貸手に送信</h3>" ;

//                                "<p>少なくとも貸手1を選択してください（最大5社まで）。</p>" +
            message +=
                    "<select name=\"provider1-select\" required=\"false\" data-placeholder=\"貸手1を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"provider2-select\" required=\"false\" data-placeholder=\"貸手2を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"provider3-select\" required=\"false\" data-placeholder=\"貸手3を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"provider4-select\" required=\"false\" data-placeholder=\"貸手4を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +

                            "<select name=\"provider5-select\" required=\"false\" data-placeholder=\"貸手5を選択\">";
            message += messageOptions;
            message +=
                    "</select>" +
                        "<button type=\"action\" name=\"recreate-rfq-button\">再作成</button>" +
                        "<button type=\"action\" name=\"cancel-rfq-button\">取消</button>" +
                        "<button type=\"action\" name=\"send-rfq-button\">送信</button>" +
                        "</form>";
        }

        message +=
                        "</div>" +
                    "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);

        return messageOut;
    }



    public OutboundMessage buildConfirmMessage(String referalUsers) {
        String message =
                "<h3>Your expense has been submitted to " + referalUsers + ".</h3>" +
                        "<p>Thanks for using StockLendingBot !</p>";

        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);

        return messageOut;

    }

    public OutboundMessage buildHelpMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>Use StockLendingBot to create and submit an expense from using Symphony Elements</h3>" +
                        "<p>Type @" + botUserInfo.getUsername() + " <b>create expense</b> to create an expense approval form</p>" +
                        "<p>In order to assign your expense approval form to your manager, you must first add an expense</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildCancelMessage(String requestId) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>依頼番号[" + requestId + "]は、取消されました。</h3>" +
                        "<p>新たな依頼を作成する場合は、<b>@" + botUserInfo.getUsername() + " /createrfq</b> のようにチャットボットに命令文を送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }
    public OutboundMessage buildErrorMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>エラーが発生しました</h3>" +
                        "<p>チャットボット @" + botUserInfo.getUsername() + " の処理において問題が発生しました。</p>" +
                        "<p>システム管理者にお問い合わせください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }
}

