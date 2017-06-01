package com.GuavaRedisHbase;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Guava{

    private static LoadingCache<Object, Object> cache = CacheBuilder.newBuilder()
             .maximumSize(2)
             .expireAfterAccess(24, TimeUnit.HOURS)
             .recordStats()
             .build(new CacheLoader<Object, Object>() {
 
                 @Override
                 public Object load(Object key) throws Exception {
                     System.out.println("cache not hit");
                     return key;
                 }
             });
 
     public static Object get(Object key) throws ExecutionException {
         Object var = cache.get(key);
 
         if (var.equals(key)) {
 
             System.out.println("-----");
             /**执行其他操作，获取值**/
             Object object = "Google.com.hk";
             put(key, object);
         } else {
             System.out.println("-----");
         }
         return cache.get(key);
     }
 
     public static void put(Object key, Object value) {
         cache.put(key, value);
     }

}

 


