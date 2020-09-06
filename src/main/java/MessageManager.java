

public class MessageManager {
    private static MessageManager instance;
    private String userName;

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }
    public void reset() {
        this.userName = "";
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
