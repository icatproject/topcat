package org.icatproject.topcat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;
import java.net.URLDecoder;
import java.util.Map;
import java.util.HashMap;

public class Utils {

    public static String bytesToHumanReadable(long bytes) {
        int unit = 1000;
        
        if (bytes < unit) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = ("kMGTPE").charAt(exp-1);

        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static JsonObject parseJsonObject(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }

    public static JsonArray parseJsonArray(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonArray out = jsonReader.readArray();
        jsonReader.close();
        return out;
    }

    public static Map<String, String> parseQueryString(String queryString) throws Exception {
        Map<String, String> out = new HashMap<String, String>();
        String[] pairs = queryString.split("&"); 
        for (String pair : pairs){
            String[] splitPair = pair.split("=");
            String name = URLDecoder.decode(splitPair[0], "UTF-8");  
            String value = URLDecoder.decode(splitPair[1], "UTF-8");  
            out.put(name, value);  
        }  
        return out;
    }


    public static String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder out = new StringBuilder();
        int currentChar;
        while ((currentChar = bufferedReader.read()) > -1) {
            out.append(Character.toChars(currentChar));
        }
        bufferedReader.close();
        return out.toString();
    }

}