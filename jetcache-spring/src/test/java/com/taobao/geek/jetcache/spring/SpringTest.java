/**
 * Created on  13-09-17 11:14
 */
package com.taobao.geek.jetcache.spring;

import com.taobao.geek.jetcache.CacheContext;
import com.taobao.geek.jetcache.EnableCache;
import com.taobao.geek.jetcache.spring.beans.DynamicQuery;
import com.taobao.geek.jetcache.spring.beans.Service;
import com.taobao.geek.jetcache.spring.beans.TestBean;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringTest {

    private static ClassPathXmlApplicationContext context;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("beans.xml");
    }

    @Test
    public void test1() {
        int x1, x2, x3;

        Service service = context.getBean(Service.class);
        x1 = service.count();
        x2 = service.count();
        x3 = service.count();
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        x1 = service.countWithAnnoOnInterface();
        x2 = service.countWithAnnoOnInterface();
        Assert.assertEquals(x1, x2);

        TestBean bean = context.getBean(TestBean.class);
        x1 = bean.count();
        x2 = bean.count();
        Assert.assertEquals(x1, x2);

        x1 = bean.countWithDisabledCache();
        x2 = bean.countWithDisabledCache();
        Assert.assertNotEquals(x1, x2);

        C c = context.getBean(C.class);
        x1 = c.count(bean);
        x2 = c.count(bean);
        Assert.assertEquals(x1, x2);

        DynamicQuery q1 = new DynamicQuery();
        q1.setId(1000);
        q1.setName("N1");
        DynamicQuery q2 = new DynamicQuery();
        q2.setId(1000);
        q2.setName("N2");
        DynamicQuery q3 = new DynamicQuery();
        q3.setId(1000);
        q3.setName("N1");
        x1 = bean.count(q1);
        x2 = bean.count(q2);
        x3 = bean.count(q3);
        Assert.assertNotEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        x1 = bean.countEnabledWithConfigBean();
        x2 = bean.countEnabledWithConfigBean();
        Assert.assertEquals(x1, x2);

        x1 = bean.countDisabledWithConfigBean();
        x2 = bean.countDisabledWithConfigBean();
        Assert.assertNotEquals(x1, x2);

        Assert.assertEquals(bean.count(true), bean.count(true));
        Assert.assertNotEquals(bean.count(false), bean.count(false));
    }

    @Component
    public static class C{
        @EnableCache
        public int count(TestBean bean){
            return bean.countWithDisabledCache();
        }
    }

}
