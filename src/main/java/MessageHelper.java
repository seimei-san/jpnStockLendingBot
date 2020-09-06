import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionProcessor.class);

    public static String[] clean(String message) {
        Document doc = Jsoup.parse(message);
        Elements entities = doc.getElementsByClass("entity");
        entities.html("");

        String cleanMessage = doc.body().text();

        cleanMessage = cleanMessage.trim();

        String[] commandMessage = new String[6];

        // 1st work expects command and rest of text expects the data
        commandMessage = cleanMessage.split(" ", 7);

//        return commandMessage;
        LOGGER.debug("MessageHelp.clean returned clean command and data");
        return commandMessage;
    }
}

