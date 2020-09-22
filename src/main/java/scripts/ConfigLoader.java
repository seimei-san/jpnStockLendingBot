package scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String JOB_NAME = "Configuration Loader: ";

    public static final int MAX_REQID_LEN = 9;

    public static String env = "";
    public static int maxCounterParty = 0;
    public static int maxRfqDay = 0;
    public static int maxIoiDay = 0;
    public static int maxRollDay = 0;
    public static String intChatRoomId = "";
    public static String owner = "";
    public static String rfqHashTag = "";
    public static String ioiHashTag = "";
    public static String rolHashTag = "";
    public static String myCounterPartyName = "";
    public static String databasePath = "";
    public static String tableBackUpPath = "";
    public static String importCsvPath = "";
    public static String uploadCsvPath = "";
    public static String sendRfqCsvPath = "";
    public static String database = "";
    public static String transactionSchema = "";
    public static String counterPartySchema = "";
    public static String transactionTable = "";
    public static String counterPartyTable = "";
    public static String transactionTableSql = "";
    public static String counterPartyTableSql = "";
    public static boolean isInitialized = false;


//    public static String[] counterPartiesList; <- relocated to DataServices


    public static String loadConfig() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File("src/main/resources/cfg-general.json"));
            env = node.get("env").asText();
            maxCounterParty = node.get("maxCounterParty").asInt();
            maxRfqDay = node.get("maxRfqDay").asInt();
            maxIoiDay = node.get("maxIoiDay").asInt();
            maxRollDay = node.get("maxRollDay").asInt();
            intChatRoomId = node.get("intChatRoomId").asText();
            owner = node.get("owner").asText();
            rfqHashTag = node.get("rfqHashTag").asText();
            ioiHashTag = node.get("ioiHashTag").asText();
            rolHashTag = node.get("rolHashTag").asText();
            myCounterPartyName = node.get("myCounterPartyName").asText();
            databasePath = node.get("databasePath").asText();
            tableBackUpPath = node.get("tableBackUpPath").asText();
            importCsvPath = node.get("importCsvPath").asText();
            uploadCsvPath = node.get("uploadCsvPath").asText();
            sendRfqCsvPath = node.get("sendRfqCsvPath").asText();
            database = node.get("database").asText();
            transactionSchema = node.get("transactionSchema").asText();
            counterPartySchema = node.get("counterPartySchema").asText();
            transactionTable = node.get("transactionTable").asText();
            counterPartyTable = node.get("counterPartyTable").asText();
            transactionTableSql = node.get("transactionTableSql").asText();
            counterPartyTableSql = node.get("counterPartyTableSql").asText();
//            DataServices.getCounterPartyList();

            isInitialized = true;



        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return JOB_NAME + "Failed (JSON File error)";
        } catch (IOException e) {
            e.printStackTrace();
            return JOB_NAME + "Failed (IO Error)";

        }
        return JOB_NAME + "Successful";

    }

}
