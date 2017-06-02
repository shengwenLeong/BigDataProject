    package com.GuavaRedisHbase.coprocessor;  
      
    import java.io.IOException;  
    import java.util.ArrayList;  
    import java.util.List;  
      
    import org.apache.hadoop.hbase.Coprocessor;  
    import org.apache.hadoop.hbase.CoprocessorEnvironment;  
    import org.apache.hadoop.hbase.client.Scan;  
    import org.apache.hadoop.hbase.coprocessor.CoprocessorException;  
    import org.apache.hadoop.hbase.coprocessor.CoprocessorService;  
    import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;  
    import org.apache.hadoop.hbase.filter.CompareFilter;  
    import org.apache.hadoop.hbase.filter.Filter;  
    import org.apache.hadoop.hbase.filter.FilterList;  
    import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;  
    import org.apache.hadoop.hbase.protobuf.ResponseConverter;  
    import org.apache.hadoop.hbase.regionserver.InternalScanner;  
    import org.apache.hadoop.hbase.util.Bytes;  
    import org.apache.hadoop.hbase.Cell;  
    import org.apache.hadoop.hbase.CellUtil;  
      
    import com.google.protobuf.RpcCallback;  
    import com.google.protobuf.RpcController;  
    import com.google.protobuf.Service;  
   
    import com.GuavaRedisHbase.RedisHbasePro;

      
    public class RedisHbaseEndPoint extends RedisHbasePro.RedisHbaseProService  
            implements Coprocessor, CoprocessorService {  
       
        private RegionCoprocessorEnvironment env;  
      
        @Override  
        public void start(CoprocessorEnvironment env) throws IOException {  
            if (env instanceof RegionCoprocessorEnvironment) {  
                this.env = (RegionCoprocessorEnvironment) env;  
            } else {  
                throw new CoprocessorException("Must be loaded on a table region!");  
            }  
        }  
      
        @Override  
        public void stop(CoprocessorEnvironment arg0) throws IOException {  
      
        }  
      
        @Override  
        public Service getService() {  
            return this;  
        }  
      
        @Override  
        public void getVauleFromCo(RpcController controller, RedisHbasePro.getValueRequest request, RpcCallback<RedisHbasePro.getBackResultResponse> done) {  
             
            // String userkey = request.getUserKey();

             String rowKey = request.getRowKey();
             String family = request.getFamily();
             String column = request.getColumn();
             String key = rowKey+family+column;
             System.out.println("-----"+rowKey);
             System.out.println("-----"+family);
             System.out.println("-----"+column);
             RedisHbasePro.getBackResultResponse.Builder responseBuilder = RedisHbasePro.getBackResultResponse.newBuilder(); 
             responseBuilder.setBackResult(key);
             done.run(responseBuilder.build());
            
        }  
      
    }  