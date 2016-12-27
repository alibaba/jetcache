package com.alicp.jetcache.test.spring;

import com.alicp.jetcache.test.beans.FactoryBeanTarget;
import com.alicp.jetcache.test.beans.Service;
import com.alicp.jetcache.test.beans.TestBean;
import com.alicp.jetcache.test.support.DynamicQuery;
import org.junit.Assert;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringTest implements ApplicationContextAware {

    protected ApplicationContext context;

    protected void doTest() {
        Service service = (Service) context.getBean("service");
        TestBean bean = (TestBean)context.getBean("testBean");
        testService(service, bean);
        testTestBean(bean);

        FactoryBeanTarget target = (FactoryBeanTarget) context.getBean("factoryBeanTarget");
        Assert.assertEquals(target.count(), target.count());
    }

    private void testService(Service service, TestBean bean) {
        int x1;
        int x2;
        int x3;
        Assert.assertNotEquals(service.notCachedCount(), service.notCachedCount());

        x1 = service.countWithAnnoOnClass();
        x2 = service.countWithAnnoOnClass();
        x3 = service.countWithAnnoOnClass();
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        x1 = service.countWithAnnoOnInterface();
        x2 = service.countWithAnnoOnInterface();
        Assert.assertEquals(x1, x2);

        Assert.assertNotEquals(service.enableCacheWithNoCacheCount(bean), service.enableCacheWithNoCacheCount(bean));
        Assert.assertEquals(service.enableCacheWithAnnoOnClass(bean), service.enableCacheWithAnnoOnClass(bean));
        Assert.assertEquals(service.enableCacheWithAnnoOnInterface(bean), service.enableCacheWithAnnoOnInterface(bean));
    }

    private void testTestBean(TestBean bean) {
        Assert.assertNotEquals(bean.noCacheCount(), bean.noCacheCount());
        Assert.assertEquals(bean.count(), bean.count());
        Assert.assertEquals(bean.countWithLocalCache(), bean.countWithLocalCache());
        Assert.assertEquals(bean.countWithBoth(), bean.countWithBoth());
        Assert.assertNotEquals(bean.countWithDisabledCache(), bean.countWithDisabledCache());


        DynamicQuery q1 = new DynamicQuery();
        q1.setId(1000);
        q1.setName("N1");
        DynamicQuery q2 = new DynamicQuery();
        q2.setId(1000);
        q2.setName("N2");
        DynamicQuery q3 = new DynamicQuery();
        q3.setId(1000);
        q3.setName("N1");
        int x1;
        int x2;
        int x3;
        x1 = bean.count(q1);
        x2 = bean.count(q2);
        x3 = bean.count(q3);
        Assert.assertNotEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        Assert.assertEquals(bean.countEnabledWithConfigBean(), bean.countEnabledWithConfigBean());
        Assert.assertNotEquals(bean.countDisabledWithConfigBean(), bean.countDisabledWithConfigBean());
        Assert.assertNotEquals(bean.countWithWrongCondition(), bean.countWithWrongCondition());

        Assert.assertEquals(bean.count(true), bean.count(true));
        Assert.assertNotEquals(bean.count(false), bean.count(false));

        Assert.assertEquals(bean.namedCount1_WithNameN1(),bean.namedCount1_WithNameN1());
        Assert.assertEquals(bean.namedCount1_WithNameN1(),bean.namedCount2_WithNameN1());
        Assert.assertNotEquals(bean.namedCount1_WithNameN1(),bean.namedCount_WithNameN2());
        Assert.assertEquals(bean.namedCount_WithNameN2(),bean.namedCount_WithNameN2());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
