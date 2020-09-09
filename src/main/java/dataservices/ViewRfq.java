package dataservices;

public class ViewRfq {
    private String requestId;
    private String type;
    private int lineNo;
    private String stockCode;
    private int borrowerQty;
    private String borrowerStart;
    private String borrowerEnd;


    public ViewRfq(String requestId, String type, int lineNo, String stockCode, int borrowerQty, String borrowerStart, String borrowerEnd) {
        this.requestId = requestId;
        this.type = type;
        this.lineNo = lineNo;
        this.stockCode = stockCode;
        this.borrowerQty = borrowerQty;
        this.borrowerStart = borrowerStart;
        this.borrowerEnd = borrowerEnd;

    }

    public String getRequestId() {
        return this.requestId;
    }

    public String getType() {
        return this.type;
    }
    public int getLineNo() {
        return this.lineNo;
    }
    public String getStockCode() {
        return this.stockCode;
    }
    public int getBorrowerQty() {
        return this.borrowerQty;
    }
    public String getBorrowerStart() {
        return this.borrowerStart;
    }
    public String getBorrowerEnd() {
        return this.borrowerEnd;
    }




}
