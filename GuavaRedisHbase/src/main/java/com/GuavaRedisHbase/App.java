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
       // tableDesc.addCoprocessor("org.ibm.developerworks.coprocessor.RowCountObserver");
        tableDesc.addCoprocessor("com.GuavaRedisHbase.coprocessor.RedisHbaseEndPoint");
        admin.createTable(tableDesc);
      
      }
      catch(Exception e) {e.printStackTrace();}
    }
 
    void populateTenRows(String tableName, int rowCount)
    {
      try{
        Configuration config = new Configuration();
        HConnection conn = HConnectionManager.createConnection(config);
        HTableInterface tbl = conn.getTable(tableName);
        //insert 1000
        for(int i=0; i< 1000; i++)
        {
          String rowkey = "r" + Integer.toString(i);
          Put put = new Put(rowkey.getBytes());
          put.add("c1".getBytes(),"col1".getBytes(),"v".getBytes());
          tbl.put(put);           
        }
        for(int i=0; i< 1000 - rowCount; i++)
        {
          String rowkey = "r" + Integer.toString(i);
          Delete d = new Delete(rowkey.getBytes());
          tbl.delete(d);
        }

      }
      catch(Exception e) {e.printStackTrace();}
    }

    
    public static void main(String[] args ) throws Exception
    {
        Guava guava = new Guava();
        System.out.println( "Hello World!" );
        System.out.println(guava.get("man"));
        System.out.println(guava.get("lsw"));
        guava.put("lsw" , "good boy");
        System.out.println(guava.get("lsw"));

        System.out.println( "HBase Endpoint Test: Count from RegionServer" );
        
        if (args.length < 3) {
            System.err.println("Usage: CountEndpointTest <Table Name>");
            System.exit(1);
        }
        String tblName = args[0];
        App app = new App();
        app.createTable(tblName);
        try {
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
                    builder.setRowKey("abc").setFamily("123").setColumn("567");
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
        }

        System.exit(0);
        
    } 
}
