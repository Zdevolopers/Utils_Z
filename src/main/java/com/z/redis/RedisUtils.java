package com.z.redis;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * redis工具类
 * @author z
 * @version V1.0
 */
public class RedisUtils {

    private static Logger logger = LogManager.getLogger(RedisUtils.class);
    private static JedisPool pool = null;
    private static Jedis jedis = null;
    private static String host = "192.168.3.147";
    private static Integer port = 6379;
    static {
        if(pool == null){
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            //设置最大连接数
            poolConfig.setMaxTotal(500);
            //设置最大空闲数量
            poolConfig.setMaxIdle(20);
            //设置最小空闲数量
            poolConfig.setMinIdle(8);
            //设置连接超时时间
            poolConfig.setMaxWaitMillis(3000);
            //Idle时进行连接扫描
            //config.setTestWhileIdle(true);
            //表示idle object evitor两次扫描之间要sleep的毫秒数
            //config.setTimeBetweenEvictionRunsMillis(30000);
            //表示idle object evitor每次扫描的最多的对象数
            //config.setNumTestsPerEvictionRun(10);
            //表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
            //config.setMinEvictableIdleTimeMillis(60000);
            //初始化连接池
            pool = new JedisPool(poolConfig,host,port);
        }
    }

    /**
     *  获取 jedis 实例对象
     * @return
     */
    public static Jedis getJedisInstance(){
        try {
            if(jedis == null){
                jedis = pool.getResource();
                //jedis.auth(password);
            }
        } catch (Exception e) {
            logger.error("实例化jedis失败.........", e);
        }
        return jedis;
    }

    /**
     *  向缓存中设置字符串内容,并设置 过期时间
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public static boolean set(String key,String value,int seconds){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                if(seconds > 0){
                    jedis.set(key,value);
                    jedis.expire(key,seconds);
                    return true;
                }else {
                    logger.error("redis seconds参数异常 seconds: "+seconds);
                    return false;
                }
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
            }
        }catch (Exception e){
            logger.error("redis set方法失败...key="+key+"  value="+value, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  根据key 获取内容
     * @param key
     * @return
     */
    public static Object get(String key){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                Object value = jedis.get(key);
                return value;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis get方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     *  根据key 获取 对象
     * @param key
     * @return
     */
    public static <T> T get(String key,Class<T> clazz){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                String value = jedis.get(key);
                return JSON.parseObject(value,clazz);
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis get方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     *  删除缓存中得对象，根据key
     * @param keys
     * @return
     */
    public static boolean del(String... keys){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null && keys != null && keys.length > 0){
                jedis.del(keys);
                return true;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis del方法失败...key="+keys, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  设置key过期
     * @param key
     * @param seconds
     * @return
     */
    public static boolean expire(String key,int seconds){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                if(seconds > 0){
                    jedis.expire(key,seconds);
                    return true;
                }else {
                    logger.error("redis seconds参数异常 seconds: "+seconds);
                    return false;
                }
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis expire方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  list push
     * @param key
     * @param values
     * @param redirection
     * @return
     */
    public static boolean push(String key,RedisEnum redirection,String... values){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                if("left".equals(redirection.valueof())){
                    jedis.lpush(key,values);
                    return true;
                }else if("right".equals(redirection.valueof())){
                    jedis.rpush(key, values);
                    return true;
                }else {
                    logger.error("redis redirection参数异常 redirection: "+redirection);
                    return false;
                }
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
            }
        }catch (Exception e){
            logger.error("redis push方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  list pop
     * @param key
     * @param redirection :枚举类型
     * @return
     */
    public static String pop(String key,RedisEnum redirection){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                if("left".equals(redirection.valueof())){
                    return jedis.lpop(key);
                }else if("right".equals(redirection.valueof())){
                    return jedis.rpop(key);
                }else {
                    logger.error("redis redirection参数异常 redirection: "+redirection);
                    return null;
                }
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis pop方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     * list lrange ：获取集合数据
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static List<String> lrange(String key,int start,int end){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                return jedis.lrange(key,start,end);
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis pop方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     *  散列添加 hset
     * @param key
     * @param name
     * @param value
     * @return
     */
    public static boolean hset(String key,String name,String value){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                jedis.hset(key,name,value);
                return true;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis hset方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  散列获取 hget
     * @param key
     * @param name
     * @return
     */
    public static String hget(String key,String name){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                String value = jedis.hget(key, name);
                return value;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis hget方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     *  用于将键的整数值增加count
     * @param key
     * @param count
     * @return :返回 -100L表示增加失败
     */
    public static Long incr(String key,long count){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                if(count > 0){
                    return jedis.incrBy(key,count);
                }else{
                    logger.error("redis count参数异常 count: "+count);
                    return -100L;
                }
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis incr方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return -100L;
    }

    /**
     *  判断是否存在key
     * @param key
     * @return
     */
    public static boolean exists(String key){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                return jedis.exists(key);
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis exists方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  用锁的方式 添加缓存字符串
     * @param key
     * @param value
     * @return
     */
    public static boolean setnx(String key,String value){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                jedis.setnx(key,value);
                return true;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis setnx方法失败...key="+key, e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  缓存lua脚本，并返回 evalSha字符串标识
     * @param lua ： lua脚本
     * @return
     */
    public static String  scriptLoad(String lua){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                String value = jedis.scriptLoad(lua);
                return value;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis hget方法失败...lua="+lua, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     *  执行lua脚本语言的标识，并返回结果
     * @param evalSha ：lua脚本标识
     * @return
     */
    public static Object evalSha(String evalSha){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                Object value = jedis.evalsha(evalSha.getBytes("utf-8"));
                return value;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return null;
            }
        }catch (Exception e){
            logger.error("redis hget方法失败...evalSha="+evalSha, e);
        }finally {
            pool.close();
        }
        return null;
    }

    /**
     *  清空缓存的 lua脚本
     *  注意  scriptFlush() ：返回的size是 清空的大小
     * @return
     */
    public static boolean scriptFlush(){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                String size = jedis.scriptFlush();
                logger.info("清空脚本 size："+size);
                return true;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis scriptFlush方法失败..", e);
        }finally {
            pool.close();
        }
        return false;
    }

    /**
     *  清空 redis缓存
     *  注意  flushDB() ：返回的size是 清空的大小
     * @return
     */
    public static boolean flushDB(){
        Jedis jedis = null;
        try {
            jedis = getJedisInstance();
            if(jedis != null){
                String size = jedis.flushDB();
                logger.info("清空缓存 size："+size);
                return true;
            }else {
                logger.error("redis 初始化jedis失败 jedis为null ");
                return false;
            }
        }catch (Exception e){
            logger.error("redis flushDB方法失败..", e);
        }finally {
            pool.close();
        }
        return false;
    }


    /**
     * redis方向的枚举
     *  left、right
     */
    public enum RedisEnum {
        RIGHT("r"),LEFT("l");
        private String redirection;
        RedisEnum(String redirection) {
            this.redirection = redirection;
        }
        public String valueof(){
            switch (redirection){
                case "l" : return "left";
                case "r" : return "right";
                default: return null;
            }
        }
    }
}
