package com.delay.queue.redisson.config;

import com.delay.queue.redisson.consts.ServerType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@Data
@ConfigurationProperties(prefix = "spring.delay.queue.redisson")
public class RedissonProperties {

    private ServerType type = ServerType.SINGLE;

    private String address = "redis://localhost:6379";

    private String password = "";

    private int database = 0;

    public void setDatabase(int database) {
        Assert.isTrue(database >= 0, "database must be equal or grater than 0");
        this.database = database;
    }

}
