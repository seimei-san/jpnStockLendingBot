package scripts;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Miscellaneous {
    private String personName;
    private static Miscellaneous instance;
    public static Miscellaneous getInstance() {
        if (instance == null) {
            instance = new Miscellaneous();
        }
        return instance;
    }

    public static String getNewRequestId(String type, int lotNo) {
        String newRequestId = "";
        String timeStamp = getTimeStamp("requestId");
        if (type == "RFQ") {
            newRequestId = "B" + timeStamp + String.format("%1$02d", lotNo);
        } else if (type == "IOI") {
            newRequestId = "L" + timeStamp + String.format("%1$02d", lotNo);

        } else {
            newRequestId = "ERROR";
        }
        return newRequestId;
    }


    public static String getTimeStamp(String type) {
        //get TimeStamp in Tokyo Time Zone

        //協定世界時のUTC 1970年1月1日深夜零時との差をミリ秒で取得
        long millis = System.currentTimeMillis();

        String pattern = "";

        //ミリ秒を引数としてTimestampオブジェクトを作成
        Timestamp timestamp = new Timestamp(millis);

        if (type == "backupFileName") {
            pattern = "yyyyMMddHHmm";
        } else if (type == "requestId") {
            pattern = "yyMMdd";
        } else if (type == "transaction") {
            pattern = "yyyy-MM-dd HH:mm:ss";
        } else {
            pattern = "yyyyMMddHHmm";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));  // get TimeZone
        String str = sdf.format(timestamp);

        return str;
    }


    public static String convertRoomId(String roomId) {
        // update RoomID (StreamID) for 64Base
        String convertedRoomId = StringUtils.remove(roomId, "=");
        convertedRoomId = StringUtils.replace(convertedRoomId, "/","_");
        return convertedRoomId;
    }
    public String getPersonName() {
        return  personName;
    }

    public boolean isNumber(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public String fourDigitDate(String strDate) {
        try {
            Integer.parseInt(strDate);
            return String.format("%04d",Integer.parseInt(strDate));
        } catch (NumberFormatException e) {
            return strDate;
        }
    }
}
