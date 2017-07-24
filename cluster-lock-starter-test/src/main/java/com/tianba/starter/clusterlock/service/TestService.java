package com.tianba.starter.clusterlock.service;

import com.tianba.starter.clusterlock.ClusterLock;
import com.tianba.starter.clusterlock.domain.User;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private String value = "=value=";

    public String getValue() {
        return value;
    }

    @ClusterLock("@testService.value+#user.name+#user.loves[1]+#max")
    public String test(User user, int max) {
        System.out.println("service-test:" + Thread.currentThread().getName());
        return "test";
    }

}
