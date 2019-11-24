package utils;

import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class RedisCache {

    private static final String AZURE_CACHE_HOSTNAME = "sccredis4770147967.redis.cache.windows.net";

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
                    6380, 1000, Secrets.AZURE_CACHE_PRIMARY_KEY_EU_WEST, true);
        return jedisPool;
    }

    public static String get(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.get(entryKey);
        }
    }

    public static String set(String entryKey, String entry) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.set(entryKey, entry);
        }
    }

    public static Long lpush(String entryKey, String... strings) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.lpush(entryKey, strings);
        }
    }

    public static Long rpush(String entryKey, String... strings) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.rpush(entryKey, strings);
        }
    }

    public static List<String> lrange(String entryKey, int start, int end) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(entryKey, start, end);
        }
    }

    // TODO what to return
    public static Long lpush(String entryKey, Document doc, long limit) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            Long count = jedis.lpush(entryKey, doc.toJson());
            if(count > limit)
                jedis.ltrim(entryKey, 0, limit);
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

    public static Long incrBy(String entryKey, long by) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(entryKey);
            if(value == null)
                return null;
            return jedis.incrBy(entryKey, by);
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

    public static Long decrBy(String entryKey, long by) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(entryKey);
            if(value == null)
                return null;
            return jedis.decrBy(entryKey, by);
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

    public static boolean newCounter(String entryKey, Long initValue) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            if(jedis.get(entryKey) != null)
                return false;
            jedis.set(entryKey, initValue.toString());
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

    public static SortedSet<Tuple> getSortedSet(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            if(jedis.get(entryKey) == null)
                return null;

            Set<Tuple> set = jedis.zrangeWithScores(entryKey, 0, -1);
            return new TreeSet<>(set);
        }
    }

    public static boolean addToSortedSet(String entryKey, double score, String member) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            boolean created = false;
            if(jedis.get(entryKey) != null)
                created = true;
            jedis.zadd(entryKey, score, member);
            return created;
        }
    }

    public static boolean addToSortedSet(String entryKey, double score, String member, int timeout) {
        boolean added = addToSortedSet(entryKey, score, member);
        if(added)
            setExpireTimeout(entryKey, timeout);
        return added;
    }

    public static String popLastFromSortedSet(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            if(jedis.get(entryKey) != null) {
                String lastMember = jedis.zrange(entryKey, -1, -1).toArray(new String[0])[0];
                jedis.zrem(entryKey, lastMember);
                return lastMember;
            }

            return null;
        }
    }

    public static double sortedSetIncr(String entryKey, double incr, String member) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.zincrby(entryKey, incr, member);
        }
    }

    public static boolean entryExists(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.get(entryKey) != null;
        }
    }

    public static boolean removeEntry(String entryKey) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.del(entryKey) > 0;
        }
    }

    public static void setExpireTimeout(String entryKey, int timeout) {
        initializeRedis();

        try(Jedis jedis = jedisPool.getResource()) {
            jedis.expire(entryKey, timeout);
        }
    }
}
