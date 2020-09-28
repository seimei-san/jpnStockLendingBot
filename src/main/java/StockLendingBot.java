import clients.SymBotClient;
import dataservices.DataImport;
import dataservices.DataInitialize;
import dataservices.DataServices;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.ConfigLoader;


public class StockLendingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockLendingBot.class);

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

            ConfigLoader.loadConfig(); LOGGER.debug("ConfigLoader.loadConfig executed");
            DataInitialize.freshCounterPartyTable(); LOGGER.debug("DataInitialize.freshCounterPartyTable executed");
            DataImport.importCsv(ConfigLoader.counterPartyTable); LOGGER.debug("DataImport.importCsv executed");
            DataServices.getCounterPartyList(); LOGGER.debug("DataServices.getCounterPartyList executed");
            DataServices.getExtRoomIdList(); LOGGER.debug("DataServices.getExtRoomIdList executed");
            AutoLoader.inputWatcher(botClient.getBotUsername());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("StockLendingBotException thrown on StockLendingBOt", e);


        }
    }
}

