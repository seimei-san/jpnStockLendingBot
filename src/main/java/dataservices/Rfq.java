package dataservices;

public class Rfq {
    private String requestId;
    private String type;
    private int lineNo;
    private String stockCode;
    private int requesterQty;
    private String requesterStart;
    private String requesterEnd;


    public Rfq(String requestId, String type, int lineNo, String stockCode, int requesterQty, String requesterStart, String requesterEnd) {
        this.requestId = requestId;
        this.type = type;
        this.lineNo = lineNo;
        this.stockCode = stockCode;
        this.requesterQty = requesterQty;
        this.requesterStart = requesterStart;
        this.requesterEnd = requesterEnd;

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
    public int getRequesterQty() {
        return this.requesterQty;
    }
    public String getRequesterStart() {
        return this.requesterStart;
    }
    public String getRequesterEnd() {
        return this.requesterEnd;
    }




}
