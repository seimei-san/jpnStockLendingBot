

public class MessageManager {
    private static MessageManager instance;
    private String requesterName;

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }
    public void reset() {
        this.requesterName = "";
    }
    public String getRequesterName() {
        return requesterName;
    }
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }
}
