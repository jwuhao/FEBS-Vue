package cc.mrbird.febs.common.service.impl;

import cc.mrbird.febs.common.domain.RedisInfo;
import cc.mrbird.febs.common.exception.RedisConnectException;
import cc.mrbird.febs.common.function.JedisExecutor;
import cc.mrbird.febs.common.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Redis 工具类，只封装了几个常用的 redis 命令，
 * 可根据实际需要按类似的方式扩展即可。
 *
 * @author MrBird
 */
@Service("redisService")
public class RedisServiceImpl implements RedisService {

    @Autowired
    JedisPool jedisPool;

    private static String separator = System.getProperty("line.separator");

    /**
     * 处理 jedis请求
     *
     * @param j 处理逻辑，通过 lambda行为参数化
     * @return 处理结果
     */
    private <T> T excuteByJedis(JedisExecutor<Jedis, T> j) throws RedisConnectException {
        try (Jedis jedis = jedisPool.getResource()) {
            return j.excute(jedis);
        } catch (Exception e) {
            throw new RedisConnectException(e.getMessage());
        }
    }

    @Override
    public List<RedisInfo> getRedisInfo() throws RedisConnectException {
        String info = this.excuteByJedis(
                j -> {
                    Client client = j.getClient();
                    client.info();
                    return client.getBulkReply();
                }
        );
        List<RedisInfo> infoList = new ArrayList<>();
        String[] strs = Objects.requireNonNull(info).split(separator);
        RedisInfo redisInfo;
        if (strs.length > 0) {
            for (String str1 : strs) {
                redisInfo = new RedisInfo();
                String[] str = str1.split(":");
                if (str.length > 1) {
                    String key = str[0];
                    String value = str[1];
                    redisInfo.setKey(key);
                    redisInfo.setValue(value);
                    infoList.add(redisInfo);
                }
            }
        }
        return infoList;
    }

    @Override
    public Map<String, Object> getKeysSize() throws RedisConnectException {
        Long dbSize = this.excuteByJedis(
                j -> {
                    Client client = j.getClient();
                    client.dbSize();
                    return client.getIntegerReply();
                }
        );
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", System.currentTimeMillis());
        map.put("dbSize", dbSize);
        return map;
    }

    @Override
    public Map<String, Object> getMemoryInfo() throws RedisConnectException {
        String info = this.excuteByJedis(
                j -> {
                    Client client = j.getClient();
                    client.info();
                    return client.getBulkReply();
                }
        );
        String[] strs = Objects.requireNonNull(info).split(separator);
        Map<String, Object> map = null;
        for (String s : strs) {
            String[] detail = s.split(":");
            if ("used_memory".equals(detail[0])) {
                map = new HashMap<>();
                map.put("used_memory", detail[1].substring(0, detail[1].length() - 1));
                map.put("create_time", System.currentTimeMillis());
                break;
            }
        }
        return map;
    }

    /**
     * 查找所有符合给定模式 pattern 的 key
     * @param pattern 正则
     */
    @Override
    public Set<String> getKeys(String pattern) throws RedisConnectException {
        return this.excuteByJedis(j -> j.keys(pattern));
    }

    /**
     * 返回 key 所关联的字符串值
     * @param key key
     * @return 当 key 不存在时，返回 nil ，否则，返回 key 的值。
     * @throws RedisConnectException 如果 key 不是字符串类型，那么返回一个错误。
     */
    @Override
    public String get(String key) throws RedisConnectException {
        return this.excuteByJedis(j -> j.get(key.toLowerCase()));
    }

    /**
     * 将字符串值 value 关联到 key
     * @param key   key
     * @param value value
     */
    @Override
    public String set(String key, String value) throws RedisConnectException {
        return this.excuteByJedis(j -> j.set(key.toLowerCase(), value));
    }

    /**
     * 将字符串值 value 关联到 key
     * @param key         key
     * @param value       value
     * @param milliscends 毫秒
     */
    @Override
    public String set(String key, String value, Long milliscends) throws RedisConnectException {
        String result = this.set(key.toLowerCase(), value);
        this.pexpire(key, milliscends);
        return result;
    }

    /**
     * 删除给定的一个或多个 key
     * @param key key
     * @return 被删除 key 的数量
     */
    @Override
    public Long del(String... key) throws RedisConnectException {
        return this.excuteByJedis(j -> j.del(key));
    }

    /**
     * 检查给定 key 是否存在
     * @param key key
     * @return 若 key 存在，返回 1 ，否则返回 0 。
     */
    @Override
    public Boolean exists(String key) throws RedisConnectException {
        return this.excuteByJedis(j -> j.exists(key));
    }

    /**
     * 这个命令类似于 TTL 命令，但它以毫秒为单位返回 key 的剩余生存时间，而不是像 TTL 命令那样，以秒为单位
     * @param key key
     * @return 当 key 不存在时，返回 -2 。
     * 当 key 存在但没有设置剩余生存时间时，返回 -1 。
     * 否则，以毫秒为单位，返回 key 的剩余生存时间。
     * @throws RedisConnectException
     */
    @Override
    public Long pttl(String key) throws RedisConnectException {
        return this.excuteByJedis(j -> j.pttl(key));
    }

    /**
     * redis中重新设置时间
     * @param key         key 数据
     * @param milliseconds 毫秒
     * @return 0失败 1成功
     */
    @Override
    public Long pexpire(String key, Long milliseconds) throws RedisConnectException {
        return this.excuteByJedis(j -> j.pexpire(key, milliseconds));
    }

    /**
     * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中
     * @param key    key key
     * @param score  score score
     * @param member value member
     */
    @Override
    public Long zadd(String key, Double score, String member) throws RedisConnectException {
        return this.excuteByJedis(j -> j.zadd(key, score, member));
    }

    /**
     * redis获取指定返回的数据
     * @param key key 数据
     * @param min min 最小
     * @param max max 最大
     */
    @Override
    public Set<String> zrangeByScore(String key, String min, String max) throws RedisConnectException {
        return this.excuteByJedis(j -> j.zrangeByScore(key, min, max));
    }

    /**
     * redis中移除一定范围的数据
     * @param key   key 数据
     * @param start start 开始
     * @param end   end  结束
     */
    @Override
    public Long zremrangeByScore(String key, String start, String end) throws RedisConnectException {
        return this.excuteByJedis(j -> j.zremrangeByScore(key, start, end));
    }

    /**
     * redis中移除数据
     * @param key     key
     * @param members members
     */
    @Override
    public Long zrem(String key, String... members) throws RedisConnectException {
        return this.excuteByJedis(j -> j.zrem(key, members));
    }

}
