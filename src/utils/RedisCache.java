package utils;

import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

// TODO try to abstract the different entries we have in the cache, so the application layer only has worry
// TODO about putting stuff and taking out stuff, and not how the cache itself manages the entry, unless explicitly
// TODO stated
public class RedisCache {

    private static final String AZURE_CACHE_HOSTNAME = "ccsbackendCache.redis.cache.windows.net";

    private static JedisPool jedisPool = null;

    private static JedisPoolConfig jedisPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public static JedisPool initializeRedis() {
        if(jedisPool == null)
            jedisPool = new JedisPool(jedisPoolConfig(), AZURE_CACHE_HOSTNAME,
                    6380, 1000, Secrets.AZURE_CACHE_PRIMARY_KEY, true);
        return jedisPool;
    }

    // TODO what to return
    public static long lpush(String listName, Document doc, long limit) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            long count = jedis.lpush(listName, doc.toJson());
            if(count > limit)
                jedis.ltrim(listName, 0, limit);
            return count;
        }
    }
}
