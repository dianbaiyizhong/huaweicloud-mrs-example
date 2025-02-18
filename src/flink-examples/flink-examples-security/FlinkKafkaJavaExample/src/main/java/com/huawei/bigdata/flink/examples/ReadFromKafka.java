/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.bigdata.flink.examples;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

/**
 * @since 8.0.2
 */
public class ReadFromKafka {
    public static void main(String[] args) throws Exception {
        System.out.println("use command as: ");
        System.out.println(
                "./bin/flink run --class com.huawei.bigdata.flink.examples.ReadFromKafka"
                        + " /opt/test.jar --topic topic-test -bootstrap.servers xxx.xxx.xxx.xxx:21005");
        System.out.println(
                "./bin/flink run --class com.huawei.bigdata.flink.examples.ReadFromKafka /opt/test.jar --topic"
                    + " topic-test -bootstrap.servers xxx.xxx.xxx.xxx:21007 --security.protocol SASL_PLAINTEXT"
                    + " --sasl.kerberos.service.name kafka");
        System.out.println(
                "./bin/flink run --class com.huawei.bigdata.flink.examples.ReadFromKafka /opt/test.jar --topic"
                    + " topic-test -bootstrap.servers xxx.xxx.xxx.xxx:21008 --security.protocol SSL"
                    + " --ssl.truststore.location /home/truststore.jks --ssl.truststore.password huawei");
        System.out.println(
                "./bin/flink run --class com.huawei.bigdata.flink.examples.ReadFromKafka /opt/test.jar --topic"
                    + " topic-test -bootstrap.servers xxx.xxx.xxx.xxx:21009 --security.protocol SASL_SSL"
                    + " --sasl.kerberos.service.name kafka --ssl.truststore.location /home/truststore.jks"
                    + " --ssl.truststore.password huawei");
        System.out.println(
                "******************************************************************************************");
        System.out.println("<topic> is the kafka topic name");
        System.out.println("<bootstrap.servers> is the ip:port list of brokers");
        System.out.println(
                "******************************************************************************************");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ParameterTool paraTool = ParameterTool.fromArgs(args);
        DataStream<String> messageStream =
                env.addSource(
                        new FlinkKafkaConsumer<>(
                                paraTool.get("topic"), new SimpleStringSchema(), paraTool.getProperties()));
        messageStream
                .rebalance()
                .map(
                        new MapFunction<String, String>() {
                            @Override
                            public String map(String s) throws Exception {
                                return "Flink says " + s + System.getProperty("line.separator");
                            }
                        })
                .print();
        env.execute();
    }
}
