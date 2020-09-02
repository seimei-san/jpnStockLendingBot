

public class MessageManager {
    private static MessageManager instance;
    private String borrowerName;

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }
    public void reset() {
        this.borrowerName = "";
    }
    public String getborrowerName() {
        return borrowerName;
    }
    public void setborrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }
}
