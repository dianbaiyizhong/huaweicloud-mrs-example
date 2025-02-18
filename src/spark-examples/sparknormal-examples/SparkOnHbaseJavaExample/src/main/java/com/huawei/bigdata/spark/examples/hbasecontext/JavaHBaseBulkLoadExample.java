/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.bigdata.spark.examples.hbasecontext;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.spark.FamilyHFileWriteOptions;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.spark.KeyFamilyQualifier;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Run this example using command below:
 *
 *  SPARK_HOME/bin/spark-submit --master local[2]
 *  --class org.apache.hadoop.hbase.spark.example.hbasecontext.JavaHBaseBulkLoadExample
 *  path/to/hbase-spark.jar {path/to/output/HFiles}
 *
 * This example will output put hfiles in {path/to/output/HFiles}, and user can run
 * 'hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles' to load the HFiles into table to
 * verify this example.
 * usage: hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles {path/to/output/HFiles} {tableName}
 */
public final class JavaHBaseBulkLoadExample {
    private JavaHBaseBulkLoadExample() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("JavaHBaseBulkLoadExample {outputPath} {tableName}");
            return;
        }
        String outputPath = args[0];
        String tableName = args[1];
        String columnFamily1 = "f1";
        String columnFamily2 = "f2";

        SparkConf sparkConf = new SparkConf().setAppName("JavaHBaseBulkLoadExample " + tableName);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        try {
            List<String> list = new ArrayList<String>();
            // row1
            list.add("1," + columnFamily1 + ",b,1");
            // row3
            list.add("3," + columnFamily1 + ",a,2");
            list.add("3," + columnFamily1 + ",b,1");
            list.add("3," + columnFamily2 + ",a,1");
            /* row2 */
            list.add("2," + columnFamily2 + ",a,3");
            list.add("2," + columnFamily2 + ",b,3");

            JavaRDD<String> rdd = jsc.parallelize(list);

            Configuration config = HBaseConfiguration.create();
            config.set("hfile.compression", Compression.Algorithm.SNAPPY.getName());
            JavaHBaseContext hbaseContext = new JavaHBaseContext(jsc, config);

            hbaseContext.bulkLoad(
                    rdd,
                    TableName.valueOf(tableName),
                    new BulkLoadFunction(),
                    outputPath,
                    new HashMap<byte[], FamilyHFileWriteOptions>(),
                    false,
                    HConstants.DEFAULT_MAX_FILE_SIZE);
        } finally {
            jsc.stop();
        }
    }

    public static class BulkLoadFunction implements Function<String, Pair<KeyFamilyQualifier, byte[]>> {
        public Pair<KeyFamilyQualifier, byte[]> call(String v1) throws Exception {
            if (v1 == null) {
                return null;
            }

            String[] strs = v1.split(",");
            if (strs.length != 4) {
                return null;
            }

            KeyFamilyQualifier kfq =
                    new KeyFamilyQualifier(Bytes.toBytes(strs[0]), Bytes.toBytes(strs[1]), Bytes.toBytes(strs[2]));
            return new Pair(kfq, Bytes.toBytes(strs[3]));
        }
    }
}
