import clients.SymBotClient;
import dataservices.DataServices;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;


public class StockLendingBot {
    private static final Logger log = LoggerFactory.getLogger(StockLendingBot.class);

    public static void main(String[] args) {
        new StockLendingBot();

    }
    public StockLendingBot() {
        BasicConfigurator.configure();

        try {
            SymBotClient botClient = SymBotClient.initBotRsa("config.json");
            MessageSender.createInstance(botClient);
            MessageProcessor messageProcessor = new MessageProcessor(botClient);
            ActionProcessor actionProcessor = new ActionProcessor(botClient);
            botClient.getDatafeedEventsService().addListeners(
                    new IMListenerImpl(messageProcessor),
                    new RoomListenerImpl(messageProcessor),
                    new ElementsListenerImpl(actionProcessor)
            );

            ConfigLoader.loadConfig();
            DataServices.getCounterPartyList();
        } catch (Exception e) {
            e.printStackTrace();


        }
    }
}

