package com.delay.queue.redisson.config;

import com.delay.queue.redisson.consts.ServerType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass({RedissonClient.class, Redisson.class})
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonAutoConfiguration {

    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Bean(name = RedissonConfigUtils.REDISSON_QUEUE_BEAN_PROCESSOR_BEAN_NAME)
    public RedissonQueueBeanPostProcessor redissonQueueBeanPostProcessor() {
        return new RedissonQueueBeanPostProcessor();
    }

    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Bean(name = RedissonConfigUtils.REDISSON_QUEUE_REGISTRY_BEAN_NAME)
    public RedissonQueueRegistry redissonQueueRegistry() {
        return new RedissonQueueRegistry();
    }

    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Bean
    @ConditionalOnMissingBean
    public RedissonTemplate redissonTemplate() {
        return new RedissonTemplate();
    }

    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedissonProperties properties) {
        final ServerType serverType = properties.getType();
        final String serverAddress = properties.getAddress();
        Assert.hasText(serverAddress, "redis cluster nodes config error");
        final String password = properties.getPassword();
        Config config = new Config();
        if (serverType == ServerType.SINGLE) {
            SingleServerConfig singleServerConfig = config.useSingleServer();
            singleServerConfig.setAddress(this.checkAndFixAddress(serverAddress));
            if (StringUtils.hasText(password)) {
                singleServerConfig.setPassword(password);
            }
            singleServerConfig.setDatabase(properties.getDatabase());
            return Redisson.create(config);
        }
        if (serverType == ServerType.CLUSTER) {
            ClusterServersConfig clusterServersConfig = config.useClusterServers();
            String[] nodes = serverAddress.split("[,;]");
            for (String node : nodes) {
                clusterServersConfig.addNodeAddress(this.checkAndFixAddress(node));
            }
            if (StringUtils.hasText(password)) {
                clusterServersConfig.setPassword(password);
            }
            return Redisson.create(config);
        }
        throw new BeanCreationException("redis server type is illegal");
    }

    private String checkAndFixAddress(String address) {
        final String protocol = "redis://";
        if (address.startsWith(protocol)) {
            return address;
        }
        return protocol + address;
    }

}
