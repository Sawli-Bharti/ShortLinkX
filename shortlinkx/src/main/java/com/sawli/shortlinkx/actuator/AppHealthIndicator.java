package com.sawli.shortlinkx.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Custom Actuator Health Indicator checking MySQL database and Redis server health.
 */
@Component
public class AppHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Constructor injection with optional RedisConnectionFactory to support profiles without Redis active (like tests).
     */
    public AppHealthIndicator(
            DataSource dataSource,
            @Autowired(required = false) RedisConnectionFactory redisConnectionFactory
    ) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        boolean dbHealthy = checkDatabase();
        boolean redisHealthy = checkRedis();

        Health.Builder builder = dbHealthy && redisHealthy ? Health.up() : Health.down();
        return builder
                .withDetail("MySQL Database", dbHealthy ? "UP" : "DOWN")
                .withDetail("Redis Server", redisConnectionFactory == null ? "DISABLED" : (redisHealthy ? "UP" : "DOWN"))
                .build();
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        if (redisConnectionFactory == null) {
            return true; // Gracefully bypass if Redis autoconfiguration is excluded
        }
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String ping = connection.ping();
            return "PONG".equalsIgnoreCase(ping);
        } catch (Exception e) {
            return false;
        }
    }
}
