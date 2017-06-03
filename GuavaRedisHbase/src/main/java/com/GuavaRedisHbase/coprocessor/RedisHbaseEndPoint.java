    package com.GuavaRedisHbase.coprocessor;  
      
    import java.io.IOException;  
    import java.util.ArrayList;  
    import java.util.List;  
    
    import org.apache.commons.logging.Log;
    import org.apache.commons.logging.LogFactory;

    import org.apache.hadoop.hbase.KeyValue;
    import org.apache.hadoop.hbase.client.Result;
    import org.apache.hadoop.hbase.client.ResultScanner;

    import org.apache.hadoop.hbase.Coprocessor;  
    import org.apache.hadoop.hbase.CoprocessorEnvironment;  
    import org.apache.hadoop.hbase.client.Scan; 
    import org.apache.hadoop.hbase.client.Get; 
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
       
        private static final Log LOG = LogFactory.getLog(RedisHbaseEndPoint.class);
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
             System.out.println("-----"+rowKey);
             System.out.println("-----"+family);
             System.out.println("-----"+column);
             String BackResult = rowKey+family+column;
             RedisHbasePro.getBackResultResponse.Builder responseBuilder = RedisHbasePro.getBackResultResponse.newBuilder(); 
             InternalScanner scanner = null;
             Result hbaseresult = null;
             Get get = new Get(Bytes.toBytes(request.getRowKey()));
             
             //Scan scan = new Scan();
             get.addFamily(Bytes.toBytes(request.getFamily()));
             get.addColumn(Bytes.toBytes(request.getFamily()), Bytes.toBytes(request.getColumn()));
             try
             {
                //scanner = env.getRegion().getScanner(scan);
                hbaseresult = env.getRegion().get(get);
                /*List<Cell> results = new ArrayList<Cell>();
                boolean hasMore = false;
                System.out.println("lsw1-----"+hasMore);
                do {
                    hasMore = scanner.next(results);
                    //System.out.println("lsw2-----"+hasMore);
                   for (Cell cell : results) {
                        /*System.out.println("lsw3-----"+Bytes.toLong(CellUtil.cloneValue(cell)));
                        LOG.info("lsw------test"+Bytes.toLong(CellUtil.cloneValue(cell)));
                        System.out.println("lsw3-----"+hasMore);
                        System.out.println("+++-----"+Bytes.toString(CellUtil.cloneRow(cell))+"value"+Bytes.toString(CellUtil.cloneValue(cell)));

                    }
                    results.clear();
                    //System.out.println("lsw4-----"+hasMore);
                } while (hasMore);*/
                 if(hbaseresult.isEmpty())
                 {
                    responseBuilder.setBackResult("no data be hitted");
                 }
                 else
                 {
                    for (KeyValue kv : hbaseresult.list()) {
                        System.out.println("family:" + Bytes.toString(kv.getFamily()));
                        System.out.println("qualifier:" + Bytes.toString(kv.getQualifier()));
                        System.out.println("value:" + Bytes.toString(kv.getValue()));
                        System.out.println("Timestamp:" + kv.getTimestamp());
                        System.out.println("-------------------------------------------");
                        BackResult = Bytes.toString(kv.getValue());
                    }
                    responseBuilder.setBackResult(BackResult);
                 }
                 
             }
             catch(IOException ioe) {
                
                ResponseConverter.setControllerException(controller, ioe);
             }
             finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException ignored) {}
                }
             }
             
             done.run(responseBuilder.build());
            
        }  
      
    }  