/**
 * Created on  13-09-17 11:14
 */
package com.taobao.geek.jetcache.spring;

import com.taobao.geek.jetcache.spring.beans.Service;
import com.taobao.geek.jetcache.spring.beans.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author yeli.hl
 */
public class SpringTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        TestBean tb = context.getBean(TestBean.class);
        System.out.println(tb.getClass());
        System.out.println(tb.foo());
        System.out.println(tb.foo());

        Service service = context.getBean(Service.class);
        System.out.println(service.getClass());
        service.bar();
        service.bar();
    }
}
