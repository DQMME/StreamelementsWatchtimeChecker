import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("deprecation")
public class StreamelementsWatchtimeChecker {

    private final OkHttpClient httpClient = new OkHttpClient();

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Gebe den Twitch-Streamer Namen ein.");

        String channel = scanner.nextLine();

        System.out.println("Gebe den Usernamen ein.");

        String user = scanner.nextLine();

        StreamelementsWatchtimeChecker obj = new StreamelementsWatchtimeChecker();

        obj.sendGet(channel, user);
    }

    private void sendGet(String channel, String user) throws Exception {

        Request request0 = new Request.Builder()
                .url("https://api.streamelements.com/kappa/v2/channels/" + channel)
                .addHeader("accept", "application/json")
                .build();

        String channelID = null;

        try (Response response = httpClient.newCall(request0).execute()) {
            if(!response.isSuccessful()) throw new IOException("Unexcepted code " + response);

            JsonParser parser = new JsonParser();

            JsonElement jsonTree = parser.parse(response.body().string());

            if(jsonTree.isJsonObject()){
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                channelID = jsonObject.get("_id").getAsString();
            }
        }

        int limit = 1;

        Request request1 = new Request.Builder()
                .url("https://api.streamelements.com/kappa/v2/points/" + channelID + "/watchtime")
                .addHeader("accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request1).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            JsonParser parser = new JsonParser();

            JsonElement jsonTree = parser.parse(response.body().string());

            if(jsonTree.isJsonObject()){
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                limit = jsonObject.get("_total").getAsInt();

            }
        }

        Request request2 = new Request.Builder()
                .url("https://api.streamelements.com/kappa/v2/points/" + channelID + "/watchtime?limit=" + limit)
                .addHeader("accept", "application/json")  // add request headers
                .build();

        try (Response response = httpClient.newCall(request2).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            JsonParser parser = new JsonParser();

            JsonElement jsonTree = parser.parse(response.body().string());

            if(jsonTree.isJsonObject()){
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                JsonElement total = jsonObject.get("_total");

                JsonElement users = jsonObject.get("users");
                JsonArray array = users.getAsJsonArray();
                try {
                    for (int i = 0; i < array.size(); ++i) {
                        JsonObject object = array.get(i).getAsJsonObject();
                        String filter = object.get("username").getAsString();

                        if(filter.equals(user.toLowerCase())) {
                            int place = i+1;
                            int minutes = object.get("minutes").getAsInt();
                            int hours = minutes/60;
                            System.out.println(user.toLowerCase() + " Hat " + hours + " Stunden Watchtime.");
                            System.out.println("Damit ist er auf Platz " + place + " von " + total + ".");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}