package utils;

import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

import java.time.Duration;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

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
    public static Long lpush(String listName, Document doc, long limit) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            Long count = jedis.lpush(listName, doc.toJson());
            if(count > limit)
                jedis.ltrim(listName, 0, limit);
            return count;
        }
    }

    public static Long incr(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(entryKey);
            if(value == null)
                return null;
            return jedis.incr(entryKey);
        }
    }

    public static Long decr(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(entryKey);
            if(value == null)
                return null;
            return jedis.decr(entryKey);
        }
    }

    public static Long getOrSetLong(String entryKey, Long valueIfNotExists) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(entryKey);
            if(value == null) {
                jedis.set(entryKey, valueIfNotExists.toString());
                return null;
            }
            return Long.parseLong(value);
        }
    }

    public static boolean newCounter(String counterName) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            if(jedis.get(counterName) != null)
                return false;
            jedis.set(counterName, "0");
            return true;
        }
    }

    public static boolean newCounter(String counterName, Long initValue) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            if(jedis.get(counterName) != null)
                return false;
            jedis.set(counterName, initValue.toString());
            return true;
        }
    }

    public static Long getLong(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(entryKey);
            if(value == null) {
                return null;
            }
            return Long.parseLong(value);
        }
    }

    public static Set<Tuple> getSortedSet(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            Set<Tuple> set = jedis.zrangeWithScores(entryKey, 0, -1);
            return set;
        }
    }
}
