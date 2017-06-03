package com.GuavaRedisHbase;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

//add from App
import java.util.concurrent.TimeUnit;  
import java.util.concurrent.ExecutionException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Long;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.ipc.CoprocessorRpcChannel;

import com.GuavaRedisHbase.RedisHbasePro;
import com.GuavaRedisHbase.RedisHbasePro.getValueRequest;
import com.GuavaRedisHbase.RedisHbasePro.getBackResultResponse;
import com.GuavaRedisHbase.RedisHbasePro.RedisHbaseProService;

public class Guava{

    private static LoadingCache<String, String> cache = CacheBuilder.newBuilder()
             .maximumSize(2)
             .expireAfterAccess(24, TimeUnit.HOURS)
             .recordStats()
             .build(new CacheLoader<String, String>() {
 
                 @Override
                 public String load(String key) throws Exception {
                     System.out.println("cache not hit");
                     return key;
                 }
             });
 
     public static String get(String usertable,String key) throws ExecutionException {
         String var = cache.get(key);
         System.out.println(var);
         System.out.println(key);
         if (var.equals(key)) {
 
             System.out.println("-----");
             String[] temp = key.split("_");
             System.out.println(temp[0]);
             System.out.println(temp[1]);
             System.out.println(temp[2]);
             String result = ValueFromCoprocessor(usertable,temp[0],temp[1],temp[2]);
             cache.put(key, result);
         } else {
             System.out.println("++++++");
         }
         return cache.get(key);
     }
 
     public static void put(String key, String value) {
         cache.put(key, value);
     }
    public static void Userput(String tableName,String key, String value) {
         cache.put(key, value);
         String[] temp = key.split("_");
         PutHbase(tableName,temp[0],temp[1],temp[2],value);
     }

    static void PutHbase(String tableName, String rowkey, String family, String column , String value)
    {
      try{
        Configuration config = new Configuration();
        HConnection conn = HConnectionManager.createConnection(config);
        HTableInterface tbl = conn.getTable(tableName);
        //insert 1000
          Put put = new Put(rowkey.getBytes());
          put.add(family.getBytes(),column.getBytes(),value.getBytes());
          tbl.put(put);           
        }
      catch(Exception e) {e.printStackTrace();}
    }

    static String ValueFromCoprocessor(String usertable,final String rowkey,final String family,final String column)
    {
          String valueFromCoprocessor = null;
          try {
            Configuration config = new Configuration();

            HConnection connection = HConnectionManager.createConnection(config);
            TableName tableName = TableName.valueOf(usertable);
            HTableInterface table = connection.getTable(tableName);
            //final getValueRequest request = getValueRequest.newBuilder().build();
            final com.GuavaRedisHbase.RedisHbasePro.getValueRequest.Builder builder = getValueRequest.newBuilder();

            Map<byte[], String> results = table.coprocessorService(RedisHbaseProService.class, null, null, new Batch.Call<RedisHbaseProService, String>() {
                @Override
                public String call(RedisHbaseProService instance) throws IOException {
                    BlockingRpcCallback rpcCallback = new BlockingRpcCallback();
                    builder.setRowKey(rowkey).setFamily(family).setColumn(column);
                    instance.getVauleFromCo(null, builder.build(), rpcCallback);
                    getBackResultResponse response = (getBackResultResponse)rpcCallback.get();
                    return response.hasBackResult()?response.getBackResult():"00";
                }
            });

            for (String cnt : results.values()) {
                System.out.println("Value = " + cnt);
                valueFromCoprocessor = cnt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return valueFromCoprocessor;
    }

}

 


