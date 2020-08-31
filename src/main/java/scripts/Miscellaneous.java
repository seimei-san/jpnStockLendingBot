package scripts;

import dataservices.DataServices;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Miscellaneous {
    private static final Logger LOGGER = LoggerFactory.getLogger(Miscellaneous.class);

    private String personName;
    private static Miscellaneous instance;
    public static Miscellaneous getInstance() {
        if (instance == null) {
            instance = new Miscellaneous();
        }
        return instance;
    }

    public static String getNewRequestId(String type, int lotNo) {
        String newRequestId;
        String timeStamp = getTimeStamp("requestId");
        if (type.equals("RFQ")) {
            newRequestId = "B" + timeStamp + String.format("%1$02d", lotNo);
        } else if (type.equals("IOI")) {
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

        String pattern;

        //ミリ秒を引数としてTimestampオブジェクトを作成
        Timestamp timestamp = new Timestamp(millis);

        switch (type) {
            case "fileName":
                pattern = "yyyyMMdd_HHmm";
                break;
            case "requestId":
                pattern = "yyMMdd";
                break;
            case "transaction":
                pattern = "yyyy-MM-dd HH:mm:ss";
                break;
            default:
                pattern = "yyyyMMddHHmm";
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));  // get TimeZone

        return sdf.format(timestamp);
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

    public static boolean isNumber(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static String fourDigitDate(String strDate) {
        try {
            Integer.parseInt(strDate);
            return String.format("%04d",Integer.parseInt(strDate));
        } catch (NumberFormatException e) {
            return strDate;
        }
    }
    public static boolean checkRoomId(String streamId) {
        boolean hitRoomId = false;
        for (int i = 0; i < DataServices.extRoomIdList.length; i++) {
            if (streamId.equals(DataServices.extRoomIdList[i])) {
                hitRoomId = true;
                break;
            }
        }
        return hitRoomId;
    }
}
