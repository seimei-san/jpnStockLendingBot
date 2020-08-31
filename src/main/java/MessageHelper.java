import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MessageHelper {
    public static String[] clean(String message) {
        Document doc = Jsoup.parse(message);
        Elements entities = doc.getElementsByClass("entity");
        entities.html("");

        String cleanMessage = doc.body().text();
        String[] commandMessage = new String[3];

        cleanMessage = cleanMessage.trim();

        String[] items = new String[3];

        String[] tmpVal = cleanMessage.split(" ",0);
        if (tmpVal.length<=1) {
            items[0]=tmpVal[0];
            items[1]="";
            items[2]="";
        } else if (tmpVal.length==2) {
            items[0]=tmpVal[0];
            items[1]=tmpVal[1];
            items[2]="";
        } else {
            items[0]=tmpVal[0];
            items[1]=tmpVal[1];
            items[2]=tmpVal[2];
        }

        commandMessage[0] = items[0].trim();
        commandMessage[1] = items[1].trim();
        commandMessage[2] = items[2].trim();

//        return cleanMessage;
        return commandMessage;
    }
}

