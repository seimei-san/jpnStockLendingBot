import clients.SymBotClient;
import dataservices.DataServices;
import dataservices.CreateRfq;
import dataservices.SendRfq;
import model.OutboundMessage;
import model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;

import java.io.File;

public class MessageSender {
    private static MessageSender messageSender;
    private static MessageManager messageManager;
    private SymBotClient botClient;
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

    public OutboundMessage buildInitializeSodMessage(String msg1, String msg2, String msg3, String msg4, String msg5, String msg6, String msg7, String msgFoot) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                " SOD Initialization executed: <br/>" + msg1 +
                        "<br/>" + msg2 + "<br/>" + msg3 + "<br/>" + msg4 + "<br/>" + msg5 + "<br/>" + msg6 + "<br/>" + msg7 + "<br/>" + msgFoot;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }



    public OutboundMessage buildCreateRequestFormMessage() {
        return this.buildCreateRequestFormMessage(false, "", "", false, true);
    }


//    ====================================================================================================================
//    ====================================================================================================================
//    ================ Build RFQs from copied/pasted RFQ Data by User  ===================================================
//    ====================================================================================================================
//    ====================================================================================================================

    public OutboundMessage buildCreateRequestFormMessage(boolean errorOnProviders, String requestId, String requesterName, boolean isInserted, boolean isNew) {
        MessageManager messageManager = MessageManager.getInstance();
        DataServices ds = DataServices.getInstance();


        String message;
        StringBuilder messageOptions = new StringBuilder();
        String titleForm;


        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptions.append("<option value=\"").append(ds.counterPartiesList[i]).append("\">").append(ds.counterPartiesList[i]).append("</option>");
        }

        String displayRequestId;
        if (!requestId.equals(ConfigLoader.NO_REQUESTID)) {
            displayRequestId = requestId;
        } else {
            displayRequestId = "";
        }

        if (isNew) {
            titleForm = "RFQの作成: ";
        } else {
            titleForm = "RFQの送信: ";
        }

        if (displayRequestId.equals("")) {
            message =
            "<h3 class=\"tempo-text-color-white tempo-bg-color--green\">" + titleForm + requesterName + " []</h3>";
        } else {
            message =
            "<h3 class=\"tempo-text-color-white tempo-bg-color--green\">" + titleForm + requesterName+ " [<hash tag=\"" + displayRequestId + "\"/>]</h3>";
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
        for (CreateRfq createRfq : dataServices.getTargetRfqs(requestId, 0)) {
            totalQty += createRfq.getRequesterQty();
            message +=
                        "<tr>" +
                            "<td>" + createRfq.getType() + "</td>" +
                            "<td style='text-align:right'>" + createRfq.getLineNo() + "</td>" +
                            "<td>" + createRfq.getStockCode() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", createRfq.getRequesterQty()) + "</td>" +
                            "<td>" + createRfq.getRequesterStart() + "</td>" +
                            "<td>" + createRfq.getRequesterEnd() + "</td>" +
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

//       Div in Left Bottom
        if (isNew) {
            message +=
                    "<form id=\"create-rfq-form\">";

            if (!isInserted) {
                if (requestId.equals("")) {
                    requestId = ConfigLoader.NO_REQUESTID;
                }
                message +=
                        "<h3>依頼番号: " + requestId + "</h3>" +
                        "<br/>" +
                        "<div style=\"display:none\">" +
                        "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                        "<text-field name=\"counterparty_requester\" required=\"false\">" + requesterName + "</text-field>" +
                        "</div>";
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

//      Div in Right Bottom
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
                    "<h3>依頼番号: " + requestId + "</h3>" +
                    "<br/>" +
                    "<div style=\"display:none\">" +
                    "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                    "<text-field name=\"counterparty_requester\" required=\"false\">" + requesterName + "</text-field>" +
                    "</div>"+
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

//    ====================================================================================================================
//    ====================================================================================================================
//    ================ Build a message for sending RFQs to Provider ======================================================
//    ====================================================================================================================
//    ====================================================================================================================


    public OutboundMessage buildSendRfqFormMessage(boolean errorOnProviders, String requestId, String requesterName, String providerName, String csvFulPath, boolean isSent) {
        MessageManager messageManager = MessageManager.getInstance();
        DataServices ds = DataServices.getInstance();


        String message;

        String messageOptions = "";

        message =
                    "<h3 class=\"tempo-text-color-white tempo-bg-color--green\">QUOTEの依頼："+ requesterName + "[<hash tag=\"" + requestId + "\"/>]" + " >>> " + providerName + "</h3>";

        message +=
                "<h4>依頼者： " + messageManager.getRequesterName() + "</h4>" +
                "<br/>" +
                "<table style='table-layout:fixed;width:1200px'>" +
                    "<thead>" +
                        "<tr>" +
                        "<td style='width:5%;font-weight:bold'>種別</td>" +
                        "<td style='width:7%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:4%;font-weight:bold'>行番</td>" +
                        "<td style='width:6%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:9%;font-weight:bold'>株数</td>" +
                        "<td style='width:8%;font-weight:bold'>開始日</td>" +
                        "<td style='width:9%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:7%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:10%;font-weight:bold'>可能株数</td>" +
                        "<td style='width:8%;font-weight:bold'>開始日</td>" +
                        "<td style='width:9%;font-weight:bold'>開始日</td>" +
                        "<td style='width:8%;font-weight:bold'>利率</td>" +
                        "<td style='width:10%;font-weight:bold'>条件</td>" +
                        "</tr>" +
                    "</thead>" +
                    "<tbody>";


        DataServices dataServices = DataServices.getInstance();
        int totalQtyRequester = 0;
        int totalQtyProvider = 0;
        for (SendRfq sendRfq : dataServices.getSendRfqs(requestId, providerName)) {
            totalQtyRequester += sendRfq.getBorrowerQty();
            totalQtyProvider += sendRfq.getLenderQty();

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
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyRequester) + "</td>" +
                        "<td></td>" +
                        "<td></td>" +
                        "<td style='text-align:right;font-weight:bold'>合計: </td>" +
                        "<td style='text-align:right;font-weight:bold'>" + String.format("%,d",totalQtyProvider) + "</td>" +
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

//     Div in Left Bottom
        if (!isSent) {
//            reserve the section for future function

        }

        message +=
                "</div>";

//        Div in Right Bottom
        message +=
                "<div style=\"width:50%;\">";

        if (isSent) {

            message +=
                    "<form id=\"receive-rfq-form\">";

            message +=
                    "<h3>依頼番号: " + requestId + "</h3>" +
                    "<br/>" +
                    "<div style=\"display:none\">" +
                    "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                    "<text-field name=\"counterparty_requester\" required=\"false\">" + requesterName + "</text-field>" +
                    "<text-field name=\"counterparty_provider\" required=\"false\">" + providerName + "</text-field>" +
                    "</div>"+
                    "<h3>貸手(" + providerName + ")の対応</h3>" ;


            message +=
                    "<button type=\"action\" name=\"nothing-quote-button\">在庫無</button>" +
                    "<button type=\"action\" name=\"accept-rfq-button\">受付</button>" +
                    "</form>";
        }

        message +=
                "</div>" +
            "</div>";


        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setAttachment(new File(csvFulPath));
        messageOut.setMessage(message);

        return messageOut;
    }




    public OutboundMessage buildCancelMessage(String requestId) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>依頼番号[<hash tag=\"" + requestId + "\"/>]は、取消されました。</h3>" +
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

    public OutboundMessage buildNotInRoomMessage(String commandText) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
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
                "<h3>Use StockLendingBot to create and submit an expense from using Symphony Elements</h3>" +
                        "<p>Type @" + botUserInfo.getUsername() + " <b>create expense</b> to create an expense approval form</p>" +
                        "<p>In order to assign your expense approval form to your manager, you must first add an expense</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildAccpetRfqMessageForProvider(String requestId, String requesterName, String providerName, String csvFullPath) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>" + requesterName + "の依頼[<hash tag=\"" + requestId + "\"/>]を" + providerName + "が受け付けました。</h3>" +
                        "<p>" + providerName + "は、添付のCSVファイルをダウンロードして、見積を回答してください。</p>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setAttachment(new File(csvFullPath));

        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildNotUnderstandMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>"  + botUserInfo.getUsername() + "が送信された命令を理解できませんでした。</h3>" +
                        "<p>利用可能な命令は、@" + botUserInfo.getUsername() + " <b>/help</b> とメッセージを送ることで確認できます。</p>" +
                        "<p>当チャットボットの詳細については、システム管理者にお問い合わせくだだい。</p>";
        OutboundMessage messageOut = new OutboundMessage();

        messageOut.setMessage(message);
        return messageOut;
    }
    public OutboundMessage buildNoCounterPartyMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>貸手1が選ばれていません！</h3>" +
                        "<p>貸手1は、必須です。 貸手1を選択して再度送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }
    public OutboundMessage buildDoubtMessage() {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>エラーが発生しました</h3>" +
                        "<p>貼付られたRFQデータに誤りがあると思われます。 今一度、貼付データをご確認ください。</p>" +
                        "<p>再度、依頼を作成する場合は、<b>@" + botUserInfo.getUsername() + " /createrfq</b> のようにチャットボットに命令文を送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }


}

