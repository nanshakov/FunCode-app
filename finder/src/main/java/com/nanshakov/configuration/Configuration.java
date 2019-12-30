package com.nanshakov.configuration;

import org.springframework.context.annotation.Import;

@org.springframework.context.annotation.Configuration

@Import({Jedis.class, Kafka.class})
public class Configuration {

}
