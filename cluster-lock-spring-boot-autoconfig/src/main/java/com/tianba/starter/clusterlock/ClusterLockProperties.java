package com.tianba.starter.clusterlock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = ClusterLockProperties.CLUSTER_LOCK_PREFIX)
public class ClusterLockProperties {

    public static final String DEFAULT_LOCK_KEY = "DEFAULT_CLUSTER_LOCK_";

    public static final String CLUSTER_LOCK_PREFIX = "cluster.lock";

    private int order = 100;

    private String prefixLockKey;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getPrefixLockKey() {
        if (StringUtils.isEmpty(prefixLockKey)) {
            return DEFAULT_LOCK_KEY;
        }
        return prefixLockKey;
    }

    public void setPrefixLockKey(String prefixLockKey) {
        this.prefixLockKey = prefixLockKey;
    }
}
