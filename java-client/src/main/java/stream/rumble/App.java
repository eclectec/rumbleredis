package stream.rumble;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class App
{
    public static Gson gson = new Gson();

    public static void main( String[] args )
    {
        System.out.println( "Connecting to Redis" );

        JedisPool pool = new JedisPool("localhost", 6379);
        Jedis redis = new Jedis("localhost", 6379);

        Thread adsbThread = new Thread(() -> {
            Jedis pubRedis = new Jedis("localhost", 6379);
            System.out.println("Starting ADS-B Scraper");
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.airplanes.live/v2/point/36.1716/-115.1391/200"))
                .GET()
                .build();

            while(true) {
                try {
                    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

                    System.out.println("Response Body: " + response.body());
                    JsonObject adsbResponse = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray adsbArray = adsbResponse.getAsJsonArray("ac");

                    for(JsonElement plot : adsbArray) {
                        pubRedis.publish("traffic", gson.toJson(plot));
                    }

                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        adsbThread.start();

        JedisPubSub redisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println(String.format("%s : %s", channel, message));
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                System.out.println("Subscribed to channel: " + channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                System.out.println("Unsubscribed from channel: " + channel);
            }
        };

        redis.subscribe(redisPubSub, "traffic");

        redis.close();
        pool.close();
    }
}
