package com.GuavaRedisHbase;

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
import java.util.Random;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
/**
 * Hello world!
 *
 */
public class App 
{
    //setup the hbase configure
    void createTable(String tableName) 
    {
      try{
        Configuration config = new Configuration();
        HBaseAdmin admin = new HBaseAdmin(config);
        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        if(admin.tableExists(tableName) == true) {
          admin.disableTable(tableName);
          admin.deleteTable(tableName);
        }
        tableDesc.addFamily(new HColumnDescriptor("c1")); //add column family
        tableDesc.addCoprocessor("com.GuavaRedisHbase.coprocessor.RedisHBaseObserver");
        tableDesc.addCoprocessor("com.GuavaRedisHbase.coprocessor.RedisHbaseEndPoint");
        admin.createTable(tableDesc);
      
      }
      catch(Exception e) {e.printStackTrace();}
    }
 
    void populateTenRows(Guava guava , String tableName, int datanum)
    {
      try{
        Configuration config = new Configuration();
        HConnection conn = HConnectionManager.createConnection(config);
        HTableInterface tbl = conn.getTable(tableName);
        //insert 1000
        for(int i=0; i< datanum; i++)
        {
          String rowkey = "r" + Integer.toString(i);
          Put put = new Put(rowkey.getBytes());
          put.add("c1".getBytes(),"col1".getBytes(),rowkey.getBytes());
          guava.put("r"+i+"_c1_col1","r"+i);
          tbl.put(put);           
        }
        /*
        for(int i=0; i< 1000 - rowCount; i++)
        {
          String rowkey = "r" + Integer.toString(i);
          Delete d = new Delete(rowkey.getBytes());
          tbl.delete(d);
        }
        */

      }
      catch(Exception e) {e.printStackTrace();}
    }

    


    public static void main(String[] args ) throws Exception
    {
        /*
        String tblName = args[0];
        String rowKey = args[1];
        String family = args[2];
        String column = args[3];
        String vaule  = args[4];
        String Key = rowKey+"_"+family+"_"+column;

        int datanum = 50;

        System.out.println( "UserSearchKey "+rowKey);
        App app = new App();
        app.createTable(tblName);
        app.populateTenRows(tblName,500);

        Guava guava = new Guava();
        String result = guava.get(tblName,Key);
        System.out.println("Result = " + result);
        guava.Userput("test1" , "100_c1_col1","lsw is boy0");
        guava.Userput("test1" , "101_c1_col1","lsw is boy1");
        guava.Userput("test1" , "102_c1_col1","lsw is boy2");
        String result1 = guava.get(tblName,"100_c1_col1");
        String result2 = guava.get(tblName,"102_c1_col1");

        System.out.println("Result1 = " + result1);
        System.out.println("Result2 = " + result2);
        */
        
        FileOutputStream fs = new FileOutputStream(new File("guavaredishbase_time.txt"));
        PrintStream p = new PrintStream(fs);
         Guava guava = new Guava();

        

        String tblName = args[0];
        App app = new App();
        app.createTable(tblName);
        app.populateTenRows(guava ,tblName,50000);

        //String datanum = args[1]; 
        //int datanum = 50000;
         Configuration config = new Configuration();

        HConnection connection = HConnectionManager.createConnection(config);
        TableName tableName = TableName.valueOf(tblName);
        HTableInterface table = connection.getTable(tableName);

        
       
        

        //guava.invalidateAll(); 
        long startTime1 = System.currentTimeMillis();
        for(int i = 0;i < 45000;i++){
            Random r = new Random();
            int num = r.nextInt(50000);
            
            //p.println("result is　a " + guava.get(table , tblName,"r"+num+"_c1_col1"));
            guava.get(table , tblName,"r"+num+"_c1_col1");
        }
        long endTime1 = System.currentTimeMillis();
        System.out.println("2500 data time : "+(endTime1-startTime1)+"ms");
        p.println("2500 data time : "+(endTime1-startTime1)+"ms");

        System.out.println(guava.reportStatus());
        /*long startTime2 = System.currentTimeMillis();
        for(int i = 0;i < datanum / 2;i++){
            Random r = new Random(600L);
            int num = r.nextInt(600);
            guava.get(tblName,"r"+num+"_c1_col1");
        }
        long endTime2 = System.currentTimeMillis();
        System.out.println("600 data time : "+(endTime2-startTime2)+"ms");
        p.println("600 data time : "+(endTime2-startTime2)+"ms");


        
        long startTime3 = System.currentTimeMillis();
        for(int i = 0;i < datanum * 3/4 ;i++){
            Random r = new Random(900L);
            int num = r.nextInt(900);
            guava.get(tblName,"r"+num+"_c1_col1");
        }
        long endTime3 = System.currentTimeMillis();
        System.out.println("900 data time : "+(endTime3-startTime3)+"ms");
        p.println("900 data time : "+(endTime3-startTime3)+"ms");


        guava.invalidateAll(); 
        long startTime4 = System.currentTimeMillis();
        for(int i = 0;i < datanum ;i++){
            Random r = new Random(1200L);
            int num = r.nextInt(1200);
            guava.get(tblName,"r"+num+"_c1_col1");
        }
        long endTime4 = System.currentTimeMillis();
        System.out.println("1200 data time : "+(endTime4-startTime4)+"ms");
        p.println("1200 data time : "+(endTime4-startTime4)+"ms");

        /*Guava guava = new Guava();
        System.out.println( "Hello World!" );
        System.out.println(guava.get("test1","r811_c1_col1"));
        System.out.println(guava.get("test1","r911_c1_col1"));
        guava.put("r932_c1_col1" , "good boy");
        System.out.println(guava.get("test1","r911_c1_col1"));

        System.out.println( "HBase Endpoint Test: Count from RegionServer" );
        
        if (args.length < 4) {
            System.err.println("Usage: CountEndpointTest <Table Name>");
            System.exit(1);
        }
        
        
        String result = app.UserGet(tblName,rowKey,family,column);
        System.out.println("Result = " + result);

        
       /* try {
            Configuration config = new Configuration();

            HConnection connection = HConnectionManager.createConnection(config);
            TableName tableName = TableName.valueOf(args[0]);
            HTableInterface table = connection.getTable(tableName);
            //final getValueRequest request = getValueRequest.newBuilder().build();
            final com.GuavaRedisHbase.RedisHbasePro.getValueRequest.Builder builder = getValueRequest.newBuilder();

            Map<byte[], String> results = table.coprocessorService(RedisHbaseProService.class, null, null, new Batch.Call<RedisHbaseProService, String>() {
                @Override
                public String call(RedisHbaseProService instance) throws IOException {
                    BlockingRpcCallback rpcCallback = new BlockingRpcCallback();
                    builder.setRowKey(UserSearchKey).setFamily("c1").setColumn("col1");
                    instance.getVauleFromCo(null, builder.build(), rpcCallback);
                    getBackResultResponse response = (getBackResultResponse)rpcCallback.get();
                    return response.hasBackResult()?response.getBackResult():"00";
                }
            });

            for (String cnt : results.values()) {
                System.out.println("Value = " + cnt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }*/

        System.exit(0);
        
    } 
}
