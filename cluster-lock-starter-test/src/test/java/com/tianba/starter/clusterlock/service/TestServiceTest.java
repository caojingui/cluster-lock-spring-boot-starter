package com.tianba.starter.clusterlock.service;

import com.tianba.starter.clusterlock.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.tianba.starter.clusterlock.domain.User;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestServiceTest {

    @Autowired
    private TestService testService;

    @Test
    public void test() throws Exception {
        User user = new User();
        user.setAge(12);
        user.setName("xxxx");
        List<String> loves = new ArrayList<>();
        loves.add("a");
        loves.add("b");
        user.setLoves(loves);
        String str = testService.test(user, 100);
        System.out.println(str);
    }

}