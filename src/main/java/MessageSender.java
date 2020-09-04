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
        } else {
//            TODO
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
//    ===================================== Borrower Side Forms ==================================================================
//    ===================================== Borrower Side Forms ==================================================================


//    --------------------------------------------------------------------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------
//    ---------------- Build RFQs from copied/pasted RFQ Data by User  ---------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------
//    --------------------------------------------------------------------------------------------------------------------


    public OutboundMessage buildCreateRequestFormMessage() {
        LOGGER.debug("buildCreateRequestFormMessage returned");
        return this.buildCreateRequestFormMessage(false, "", "", "", "", "", false, true);
    }



    public OutboundMessage buildCreateRequestFormMessage(boolean errorOnlenders, String botId, String userName, String requestId, String borrowerName, String lenderName, boolean isInserted, boolean isNew) {
        MessageManager messageManager = MessageManager.getInstance();


        String message;
        StringBuilder messageOptions = new StringBuilder();
        String titleForm;


        for (int i = 0; DataServices.counterPartiesList.length > i; i++) {
            messageOptions.append("<option value=\"").append(DataServices.counterPartiesList[i]).append("\">").append(DataServices.counterPartiesList[i]).append("</option>");
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
            "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + borrowerName + " []</h3>";
        } else {
            message =
            "<h3 class=\"tempo-text-color--white tempo-bg-color--purple\">" + titleForm + borrowerName+ " [<hash tag=\"" + displayRequestId + "\"/>]</h3>";
        }



        message +=
                "<h4>作成者： " + userName + "</h4>" +
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
                        "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                        "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                        "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                        "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                        "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>"+
                        "</div>";
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

            if (errorOnlenders) {
                message +=
                        "<span class=\"tempo-text-color--red\">" +
                                "You need to choose a least on lender1 before to submit your RFQ form." +
                                "</span>" ;
            }

            message +=
                    "<form id=\"submit-rfq-form\">";

            message +=
                    "<h3>依頼番号: " + requestId + "</h3>" +
                    "<br/>" +
//                    "<div style=\"display:none\">" +
                    "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                    "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                    "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
//                    "</div>"+
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
                    "</select>" +
                    "<button type=\"action\" name=\"recreate-rfq-button\">再作成</button>" +
                    "<button type=\"action\" name=\"cancel-rfq-button\">取消</button>" +
                    "<button type=\"action\" name=\"send-rfq-button\">送信</button>" +
                    "</form>";
        }

        message +=
                 "</div>";

//      Div in Right Bottom
        message +=
                "<div style=\"width:50%;\">";



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


    public OutboundMessage buildSendRfqFormMessage(boolean errorOnlenders, String botId, String userName, String requestId,
                                                   String borrowerName, String lenderName, String rfqsData, String csvFullPath, boolean isSent) {
        MessageManager messageManager = MessageManager.getInstance();


        String message;

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">QUOTEの依頼："+ borrowerName + "[<hash tag=\"" + requestId + "\"/>]" + " → " + lenderName + "</h3>";

        message +=
                "<h4>依頼者： " + userName + "</h4>" +
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
                        "<td style='width:10%;font-weight:bold'>見積株数</td>" +
                        "<td style='width:8%;font-weight:bold'>見積開始</td>" +
                        "<td style='width:9%;font-weight:bold'>見積返却</td>" +
                        "<td style='width:8%;font-weight:bold'>見積利率</td>" +
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

//     Div in Left Bottom
        if (!isSent) {
//            reserve the section for future function
            // TODO

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
//                    "<div style=\"display:none\">" +
                    "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                    "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                    "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                    "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                    "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                    "<textarea name=\"rfqs_data\" required=\"false\">" + rfqsData + "</textarea>" +
//                    "</div>"+
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
//        messageOut.setAttachment(new File(csvFullPath));
        messageOut.setMessage(message);

        return messageOut;
    }




    public OutboundMessage buildCancelMessage(String requestId, String userName) {
        UserInfo botUserInfo = this.botClient.getBotUserInfo();
        String message =
                "<h3>依頼番号[<hash tag=\"" + requestId + "\"/>]は、" + userName + "によって取消されました。</h3>" +
                        "<p>新たな依頼を作成する場合は、<b>@" + botUserInfo.getDisplayName() + " /createrfq</b> のようにチャットボットに命令文を送信してください。</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildNotAllowYouMessage(String allowedCounterPartyName) {
        String message =
                "<h3>この操作は、" + allowedCounterPartyName + "のみが行える操作です。</h3>" +
                "<p>チャットボットの動作についての詳細は、システム管理者にお問い合わせください。</p>";
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


    public OutboundMessage buildAcceptQuoteMessage(String requestId, String userName, String borrowerName, String lenderName) {
        String message =
                "<h5>依頼番号[<hash tag=\"" + requestId + "\"/>] " + borrowerName + "からの<b>QUOTE依頼</b>は、" + userName + "に受付されました。</h5>" +
                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
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
                        "<p><b>/createrfq</b>：   [借手用] 新規RFQ（見積依頼）の作成</p>" +
                        "<p><b>/quoterfq</b>：    [貸手用] RFQ（見積依頼）への見積回答</p>" +
                        "<p><b>/viewrfq</b>：     [借手用] RFQ（見積依頼）とQUOTE（見積回答）の状況を表示</p>" +
                        "<p><b>/createioi</b>：   [貸手用] 新規IOI（貸株掲示）の作成</p>" +
                        "<p><b>/responseioi</b>： [借手用] 新規IOI（貸株掲示）への回答</p>" +
                        "<p><b>/createroll</b>：  [貸手用] 貸出中の株式を借手に確認</p>" +
                        "<br/>" +
                        "<p style=\"color:#FF0000;\">======= チャットボットメンテナンス用の命令 =======</p>" +
                        "<p><b>/initializesod</b>： 日次業務開始前の初期化</p>" +
                        "<p><b>/updateconfig</b>：  チャットボット環境設定の更新</p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        return messageOut;
    }

    public OutboundMessage buildAccpetRfqMessageForlender(String requestId, String borrowerName, String lenderName, String csvFullPath) {
        String message =
                "<h3>" + borrowerName + "の依頼[<hash tag=\"" + requestId + "\"/>]を" + lenderName + "が受け付けました。</h3>";
//                        "<p>ハッシュタグ： <hash tag=\"" + ConfigLoader.rfqHashTag + "\"/></p>";
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setAttachment(new File(csvFullPath));

        messageOut.setMessage(message);
        LOGGER.debug("buildAccpetRfqMessageForlender returned");
        return messageOut;
    }


    public OutboundMessage buildNothingMessage(String userName, String requestId, String borrowerName, String lenderName) {
        MessageManager messageManager = MessageManager.getInstance();


        String message =
                "<h3 class=\"tempo-text-color--black tempo-bg-color--yellow\">RFQへの回答："+ borrowerName + "[<hash tag=\"" + requestId + "\"/>]" + " ← " + lenderName + "</h3>" +
                "<h4>回答者： " + userName + "</h4>" +
                "<br/>" +
                "<div style=\"display: flex;\">";

        message +=
                    "<div style=\"width:50%;\">" +
                        "<form id=\"notify-nothing-form\">" +
//                            "<div style=\"display:none\">" +
                            "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                            "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
//                            "</div>"+
                            "<h3 class=\"tempo-text-color--white tempo-bg-color--red\">借手(" + borrowerName + ")の対応</h3>" +
                            "<br/>" +
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
        return messageOut;
    }

    
    
//    ================================ Lender Side Forms =======================================
//    ================================ Lender Side Forms =======================================
//    ================================ Lender Side Forms =======================================
//    ================================ Lender Side Forms =======================================
//    ================================ Lender Side Forms =======================================
//    ================================ Lender Side Forms =======================================



    public OutboundMessage buildCreateQuoteFormMessage() {
        LOGGER.debug("buildCreateQuoteFormMessage returned");
        return this.buildCreateQuoteFormMessage(false, "", "", "","", "", "", true);
    }



    public OutboundMessage buildCreateQuoteFormMessage(boolean errorOnlenders, String botId, String userName, String borrowerName, String lenderName, String lenderStatus, String csvFullPath, boolean isNew) {
        MessageManager messageManager = MessageManager.getInstance();


        String message;
        String titleForm;

        if (isNew) {
            titleForm = "QUOTEの作成: ";
        } else {
            titleForm = "QUOTEの送信: ";
        }

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">" + titleForm + "依頼者（複数）← " + lenderName + "</h3>";



        message +=
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1300px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:5%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:8%;font-weight:bold'>株数</td>" +
                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:8%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:3%;font-weight:bold'>行番</td>" +
                        "<td style='width:7%;font-weight:bold'>見積株数</td>" +
                        "<td style='width:6%;font-weight:bold'>見積開始</td>" +
                        "<td style='width:7%;font-weight:bold'>見積返却日</td>" +
                        "<td style='width:6%;font-weight:bold'>見積利率</td>" +
                        "<td style='width:7%;font-weight:bold'>見積条件</td>" +
                        "<td style='width:5%;font-weight:bold'>価格</td>" +
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
                            "<td class='tempo-text-color--blue'>" + receivedRfq.getLenderName() + "</td>" +
                            "<td >" + receivedRfq.getRequestId() + "</td>" +
                            "<td style='text-align:right'>" + receivedRfq.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getLenderQty()) + "</td>" +
                            "<td>" + receivedRfq.getLenderStart() + "</td>" +
                            "<td>" + receivedRfq.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + receivedRfq.getLenderRate() + "</td>" +
                            "<td>" + receivedRfq.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", receivedRfq.getPrice()) + "</td>" +
                            "<td>" + receivedRfq.getLenderStatus() + "</td>" +
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


//      Div in Left Bottom
        message +=
                "<div style=\"width:50%;\">"+
                "</div>" ;

//       Div in Right Bottom
        message +=
                "<br/>" +
                        "<div style=\"width:50%;\">";

        if (isNew) {
            message +=
                    "<form id=\"create-quote-form\">"+
//                    "<div style=\"display:none\">" +
                    "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                    "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                    "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                    "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" ;
//                    "</div>";

//            if (!isInserted) {
//
//                message +=
//                        "<h3>依頼番号: " + requestId + "</h3>" +
//                                "<br/>" +
//                                "<div style=\"display:none\">" +
//                                "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
//                                "</div>";
//            }

            message +=
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--cyan\">QUOTEのデータの入力</h3>" +
                            "<br/>" +
//                                "<h6>入力欄にQUOTEデータをコピー／貼付してください</h6>" +
                            "<textarea name=\"inputQuote\" placeholder=\"ここにコピーしたQUOTEデータを貼付てください。 " +
                            "&#13;" +
                            "銘柄, 株数, 開始, 終了／期間 （区切り文字は、半角スペース、カンマ、またはタブ）。" +
                            "&#13;" +
                            "入力例： " +
                            "&#13;" +
                            "1234 7000 201201 210330" +
                            "&#13;" +
                            "4567 10000 200801 3m" +
                            "&#13;" +
                            "7890 3000 200803 3w\"  required=\"true\"></textarea>" +
                            "<button type=\"reset\">消去</button><button type=\"action\" name=\"import-quote-button\">取込</button>";

            message +=
                    "</form>";
        }
        if (!isNew) {

            if (errorOnlenders) {
                message +=
                        "<span class=\"tempo-text-color--cyan\">" +
                                "You need to choose a least on lender1 before to submit your RFQ form." +
                                "</span>" ;
            }

            message +=
                    "<form id=\"submit-quote-form\">"+
//                     "<div style=\"display:none\">" +
                    "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                    "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                    "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" ;
//                    "</div>";

            message +=
//                    "<h3>依頼番号: " + requestId + "</h3>" +
//                            "<br/>" +
//                            "<div style=\"display:none\">" +
//                            "<text-field name=\"request_id\" maxlength=\"9\" required=\"false\">" + requestId + "</text-field>" +
//                            "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
//                            "</div>"+
                    "<h3 class=\"tempo-text-color--white tempo-bg-color--green\">QUOTEを借手に送信</h3>" +
                    "<br/>";

            message +=

                    "<button type=\"action\" name=\"recreate-quote-button\">再作成</button>" +
//                            "<button type=\"action\" name=\"cancel-rfq-button\">取消</button>" +
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


    public OutboundMessage buildSendQuoteFormMessage(boolean errorOnlenders, String botId, String userName, String borrowerName, String lenderName, String lenderStatus, String quoteData, boolean isNew) {
        MessageManager messageManager = MessageManager.getInstance();


        String message;
        String titleForm;

        if (isNew) {
            titleForm = "QUOTEの返信: ";
        } else {
            titleForm = "QUOTEの更新: ";
        }

        message =
                "<h3 class=\"tempo-text-color--white tempo-bg-color--blue\">" + titleForm + borrowerName + " ← " + lenderName + "</h3>";



        message +=
                "<h4>回答者： " + userName + "</h4>" +
                        "<br/>" +
                        "<table style='table-layout:fixed;width:1300px'>" +
                        "<thead>" +
                        "<tr>" +
                        "<td style='width:4%;font-weight:bold'>種別</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼元</td>" +
                        "<td style='width:5%;font-weight:bold'>銘柄</td>" +
                        "<td style='width:8%;font-weight:bold'>株数</td>" +
                        "<td style='width:6%;font-weight:bold'>開始日</td>" +
                        "<td style='width:7%;font-weight:bold'>返却日/期間</td>" +
                        "<td style='width:6%;font-weight:bold'>依頼先</td>" +
                        "<td style='width:8%;font-weight:bold'>依頼番号</td>" +
                        "<td style='width:3%;font-weight:bold'>行番</td>" +
                        "<td style='width:7%;font-weight:bold'>見積株数</td>" +
                        "<td style='width:6%;font-weight:bold'>見積開始</td>" +
                        "<td style='width:7%;font-weight:bold'>見積返却日</td>" +
                        "<td style='width:6%;font-weight:bold'>見積利率</td>" +
                        "<td style='width:7%;font-weight:bold'>見積条件</td>" +
                        "<td style='width:5%;font-weight:bold'>価格</td>" +
                        "<td style='width:4%;font-weight:bold'>状況</td>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody>";



        DataServices dataServices = DataServices.getInstance();
        int totalQtyborrower = 0;
        int totalQtylender = 0;
        for (SendQuote sendQuote : dataServices.getQuoteForBorrower(borrowerName, "SENT")) {
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
                            "<td class='tempo-text-color--blue'>" + sendQuote.getLenderName() + "</td>" +
                            "<td >" + sendQuote.getRequestId() + "</td>" +
                            "<td style='text-align:right'>" + sendQuote.getLineNo() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendQuote.getLenderQty()) + "</td>" +
                            "<td>" + sendQuote.getLenderStart() + "</td>" +
                            "<td>" + sendQuote.getLenderEnd() + "</td>" +
                            "<td style='text-align:right'>" + sendQuote.getLenderRate() + "</td>" +
                            "<td>" + sendQuote.getLenderCondition() + "</td>" +
                            "<td style='text-align:right'>" + String.format("%,d", sendQuote.getPrice()) + "</td>" +
                            "<td>" + sendQuote.getLenderStatus() + "</td>" +
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


//      Div in Left Bottom
        message +=
                "<div style=\"width:50%;\">";


            if (errorOnlenders) {
                message +=
                        "<span class=\"tempo-text-color--cyan\">" +
                                "You need to choose a least on lender1 before to submit your RFQ form." +
                                "</span>" ;
            }

            message +=
                    "<form id=\"receive-quote-form\">" +
//                    "<div style=\"display:none\">" +
                    "<text-field name=\"user_name\" required=\"false\">" + userName + "</text-field>" +
                    "<text-field name=\"bot_id\" required=\"false\">" + botId + "</text-field>" +
                    "<text-field name=\"counterparty_borrower\" required=\"false\">" + borrowerName + "</text-field>" +
                    "<text-field name=\"counterparty_lender\" required=\"false\">" + lenderName + "</text-field>" +
                    "<textarea name=\"quote_data\" required=\"false\">" + quoteData + "</textarea>";
//                    "</div>";

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
    
    
    
    
    
    
    
    
    
    
    
    
//    ======================= templates =========================
//    ======================= templates =========================
//    ======================= templates =========================
//    ======================= templates =========================
//    ======================= templates =========================
//    ======================= templates =========================

    public OutboundMessage template(String requestId, String borrowerName, String lenderName) {
        MessageManager messageManager = MessageManager.getInstance();
        String message;

        message =
                "<h3 class=\"tempo-text-color-white tempo-bg-color--yellow\">RFQへの回答：" + borrowerName + "[<hash tag=\"" + requestId + "\"/>]" + " ← " + lenderName + "</h3>" +
                        "<h4>回答者： " + messageManager.getborrowerName() + "</h4>" +
                        "<br/>" +
                        "<div style=\"display: flex;\">";

        message +=
                "<div style=\"width:50%;\">" +
                        "<form id=\"create-rfq-form\">" +
                        "</form>" +
                        "</div>";

        message +=
                "<div style=\"width:50%;\">" +
                        "<form id=\"create-rfq-form\">" +
                        "</form>" +
                        "</div>";

        message +=
                "</div>";

        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage(message);
        LOGGER.debug("buildDoubtMessage returned");
        return messageOut;

    }
}

