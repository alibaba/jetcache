/**
 * Created on  13-09-17 11:14
 */
package com.taobao.geek.jetcache.spring;

import com.taobao.geek.jetcache.spring.beans.Service;
import com.taobao.geek.jetcache.spring.beans.TestBean;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author yeli.hl
 */
public class SpringTest {

    private static ClassPathXmlApplicationContext context;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("beans.xml");
    }

    @Test
    public void test() {
        Service service = context.getBean(Service.class);
        int x1 = service.emptyMethod();
        int x2 = service.emptyMethod();
        int x3 = service.emptyMethod();
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);
    }

}
