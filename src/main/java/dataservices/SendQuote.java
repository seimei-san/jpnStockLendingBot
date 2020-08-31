package dataservices;

public class SendQuote {
    private String type;
    private int lotNo;
    private String requestId;
    private int versionNo;
    private int lineNo;
    private String stockCode;
    private String borrowerName;
    private int borrowerQty;
    private String borrowerStart;
    private String borrowerEnd;
    private double borrowerRate;
    private String borrowerCondition;
    private String borrowerStatus;
    private int providerNo;
    private String lenderName;
    private int lenderQty;
    private String lenderStart;
    private String lenderEnd;
    private double lenderRate;
    private String lenderCondition;
    private String lenderStatus;
    private int price;
    private String timeStamp;
    private String updatedBy;


    public SendQuote(String type, int lotNo, String requestId, int versionNo, int lineNo, String stockCode, String borrowerName,
                     int borrowerQty, String borrowerStart, String borrowerEnd, double borrowerRate, String borrowerCondition,
                     String borrowerStatus, int providerNo, String lenderName, int lenderQty, String lenderStart, String lenderEnd,
                     double lenderRate, String lenderCondition, String lenderStatus, int price, String timeStamp, String updatedBy
    ) {

        this.type = type;
        this.lotNo = lotNo;
        this.requestId = requestId;
        this.versionNo = versionNo;
        this.lineNo = lineNo;
        this.stockCode = stockCode;
        this.borrowerName = borrowerName;
        this.borrowerQty = borrowerQty;
        this.borrowerStart = borrowerStart;
        this.borrowerEnd = borrowerEnd;
        this.borrowerRate = borrowerRate;
        this.borrowerCondition = borrowerCondition;
        this.borrowerStatus = borrowerStatus;
        this.providerNo = providerNo;
        this.lenderName = lenderName;
        this.lenderQty = lenderQty;
        this.lenderStart = lenderStart;
        this.lenderEnd = lenderEnd;
        this.lenderRate = lenderRate;
        this.lenderCondition = lenderCondition;
        this.lenderStatus = lenderStatus;
        this.price = price;
        this.timeStamp = timeStamp;
        this.updatedBy = updatedBy;

    }

    public String getType() {return this.type;}
    public int getLotNo() {return this.lotNo;}
    public String getRequestId() {return this.requestId;}
    public int getVersionNo() {return this.versionNo;}
    public int getLineNo() {return this.lineNo;}
    public String getStockCode() {return this.stockCode;}
    public String getBorrowerName() {return this.borrowerName;}
    public int getBorrowerQty() {return this.borrowerQty;}
    public String getBorrowerStart() {return this.borrowerStart;}
    public String getBorrowerEnd() {return this.borrowerEnd;}
    public double getBorrowerRate() {return this.borrowerRate;}
    public String getBorrowerCondition() {return this.borrowerCondition;}
    public String getBorrowerStatus() {return this.borrowerStatus;}
    public int getProviderNo() {return this.providerNo;}
    public String getLenderName() {return this.lenderName;}
    public int getLenderQty() {return this.lenderQty;}
    public String getLenderStart() {return this.lenderStart;}
    public String getLenderEnd() {return this.lenderEnd;}
    public double getLenderRate() {return this.lenderRate;}
    public String getLenderCondition() {return this.lenderCondition;}
    public String getLenderStatus() {return this.lenderStatus;}
    public int getPrice() {return this.price;}
    public String getTimeStamp() {return this.timeStamp;}
    public String updatedBy() {return this.updatedBy;}


}
