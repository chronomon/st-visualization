package com.jd.st.data.storage.hbase;

import com.jd.st.data.storage.model.BinTilePyramid;
import com.jd.st.data.storage.model.KVPair;
import com.jd.st.data.storage.model.Period;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseAdaptor {

    public static String rawTableName = "experiment_18_512_1_raw";

    public static String pixelTableName = "experiment_18_512_1_pixel";

    public static String dataTableName = "experiment_18_512_1_packet";
    public static String countTableName = "experiment_18_512_1_count";
    public static BinTilePyramid pyramid = new BinTilePyramid(new Period(1), 18, 512);

    //public static final String dataTableName = "lorry_data_day_18";
    //public static final String countTableName = "lorry_count_day_18";
    //public static final BinTilePyramid pyramid = new BinTilePyramid(ChronoUnit.DAYS, 18, 256);
    public static final String NAMESPACE = "tile";

    public static final byte[] CF = "f".getBytes();

    public static final byte[] CQ = "q".getBytes();


    private static final Connection conn;

    static {
        Configuration conf = HBaseConfiguration.create();
        conf.set("zookeeper.znode.parent", "/hbase-unsecure");
        try {
            conn = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String withNameSpace(String tableNameStr) {
        return NAMESPACE + ":" + tableNameStr;
    }

    public static void createTable(String tableNameStr) throws IOException {
        TableName tableName = TableName.valueOf(tableNameStr);
        try (Admin admin = conn.getAdmin()) {
            if (admin.tableExists(tableName)) {
                System.out.println(tableNameStr + " already exists");
            } else {
                HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(CF);
                hColumnDescriptor.setDFSReplication((short) 1);
                hTableDescriptor.addFamily(hColumnDescriptor);
                hTableDescriptor.setRegionReplication(1);
                admin.createTable(hTableDescriptor);
            }
        }
    }

    public static void deleteTable(String tableNameStr) throws IOException {
        TableName tableName = TableName.valueOf(tableNameStr);
        try (Admin admin = conn.getAdmin()) {
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
            }
        }
    }

    public static void putData(String tableNameStr, List<KVPair> kvPairs) throws IOException {
        try (Table table = conn.getTable(TableName.valueOf(withNameSpace(tableNameStr)))) {
            List<Put> putList = new ArrayList<>(kvPairs.size());
            for (KVPair kvPair : kvPairs) {
                Put put = new Put(kvPair.key);
                put.addColumn(CF, CQ, kvPair.value);
                putList.add(put);
            }
            table.put(putList);
        }
    }

    public static List<KVPair> multiGetData(String tableNameStr, byte[][] keys) {
        try (Table table = conn.getTable(TableName.valueOf(withNameSpace(tableNameStr)))) {
            List<Get> getList = new ArrayList<>(keys.length);
            for (byte[] key : keys) {
                getList.add(new Get(key));
            }

            List<KVPair> kvPairs = new ArrayList<>(keys.length);
            for (Result r : table.get(getList)) {
                if (!r.isEmpty()) {
                    kvPairs.add(new KVPair(r.getRow(), r.getValue(CF, CQ)));
                }
            }
            return kvPairs;
        } catch (Exception e) {
            System.out.println("MultiGet失败");
            return new ArrayList<>();
        }
    }


    public static List<KVPair> scanData(String tableNameStr, List<KVPair> startStopRowList) {
        try (Table table = conn.getTable(TableName.valueOf(withNameSpace(tableNameStr)))) {
            List<KVPair> kvPairs = new ArrayList<>();
            for (KVPair startStop : startStopRowList) {
                Scan scan = new Scan();
                scan.addColumn(CF, CQ);

                scan.withStartRow(startStop.key);
                scan.withStopRow(startStop.value);
                try (ResultScanner rs = table.getScanner(scan)) {
                    for (Result r = rs.next(); r != null; r = rs.next()) {
                        kvPairs.add(new KVPair(r.getRow(), r.getValue(CF, CQ)));
                    }
                }
            }
            return kvPairs;
        } catch (Exception e) {
            System.out.println("Scan数据失败");
            return new ArrayList<>();
        }
    }
}
