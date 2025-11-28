package stream.rumble;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Connecting to Redis" );

        JedisPool pool = new JedisPool("localhost", 6379);
        Jedis jedis = new Jedis("localhost", 6379);

        JedisPubSub jedisPubSub = new JedisPubSub() {
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

        // Subscribe to the "my_topic" channel
        jedis.subscribe(jedisPubSub, "traffic");

        jedis.close();
        pool.close();
    }
}
