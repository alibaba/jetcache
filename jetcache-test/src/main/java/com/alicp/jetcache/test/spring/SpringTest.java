package com.alicp.jetcache.test.spring;

import com.alicp.jetcache.test.beans.FactoryBeanTarget;
import com.alicp.jetcache.test.beans.Service;
import com.alicp.jetcache.test.beans.TestBean;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.junit.Assert;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringTest extends SpringTestBase {

    protected void doTest() throws Exception {
        Service service = (Service) context.getBean("service");
        TestBean bean = (TestBean) context.getBean("testBean");
        testService(service, bean);
        testTestBean(bean);

        FactoryBeanTarget target = (FactoryBeanTarget) context.getBean("factoryBeanTarget");
        Assert.assertEquals(target.count(), target.count());
    }

    private void testService(Service service, TestBean bean) throws Exception {
        Assert.assertNotEquals(service.notCachedCount(), service.notCachedCount());
        Assert.assertEquals(service.countWithAnnoOnClass(), service.countWithAnnoOnClass());
        Assert.assertEquals(service.countWithAnnoOnInterface(), service.countWithAnnoOnInterface());
        Assert.assertNotEquals(service.enableCacheWithNoCacheCount(bean), service.enableCacheWithNoCacheCount(bean));
        Assert.assertEquals(service.enableCacheWithAnnoOnClass(bean), service.enableCacheWithAnnoOnClass(bean));
        Assert.assertEquals(service.enableCacheWithAnnoOnInterface(bean), service.enableCacheWithAnnoOnInterface(bean));

        int v1 = service.count("K1");
        Assert.assertEquals(v1, service.count("K1"));

        service.delete("K1");
        int v2 = service.count("K1");
        Assert.assertNotEquals(v1, v2);
        service.delete2("K1");
        Assert.assertEquals(v2, service.count("K1"));

        service.update("K1", 200);
        Assert.assertEquals(200, service.count("K1"));
        service.update2("K1", 300);
        Assert.assertEquals(200, service.count("K1"));

        Assert.assertEquals(service.count("K1"), service.count("K1"));
        Assert.assertNotEquals(service.count("K1"), service.count("K2"));

    }

    @SuppressWarnings("AliAccessStaticViaInstance")
    private void testTestBean(TestBean bean) throws Exception {
        Assert.assertNotEquals(bean.noCacheCount(), bean.noCacheCount());
        //noinspection AliAccessStaticViaInstance
        Assert.assertEquals(bean.staticCount(), bean.staticCount());
        Assert.assertEquals(bean.count(), bean.count());
        Assert.assertEquals(bean.countWithLocalCache(), bean.countWithLocalCache());
        Assert.assertEquals(bean.countWithBoth(), bean.countWithBoth());
        Assert.assertNotEquals(bean.countWithDisabledCache(), bean.countWithDisabledCache());

        int x = bean.countWithExpire50();
        Assert.assertEquals(x, bean.countWithExpire50());
        Thread.sleep(50);
        Assert.assertNotEquals(x, bean.countWithExpire50());

        DynamicQuery q1 = new DynamicQuery();
        q1.setId(1000);
        q1.setName("N1");
        DynamicQuery q2 = new DynamicQuery();
        q2.setId(1000);
        q2.setName("N2");
        DynamicQuery q3 = new DynamicQuery();
        q3.setId(1000);
        q3.setName("N1");
        int x1 = bean.countLocalWithDynamicQuery(q1);
        int x2 = bean.countLocalWithDynamicQuery(q2);
        int x3 = bean.countLocalWithDynamicQuery(q3);
        Assert.assertNotEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        x1 = bean.countRemoteWithDynamicQuery(q1);
        x2 = bean.countRemoteWithDynamicQuery(q2);
        x3 = bean.countRemoteWithDynamicQuery(q3);
        Assert.assertNotEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        x1 = bean.countLocalWithDynamicQueryAndKeyConvertor(q1);
        x2 = bean.countLocalWithDynamicQueryAndKeyConvertor(q2);
        x3 = bean.countLocalWithDynamicQueryAndKeyConvertor(q3);
        Assert.assertNotEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        DynamicQueryWithEquals dqwe1 = new DynamicQueryWithEquals();
        dqwe1.setId(1000);
        dqwe1.setName("N1");
        DynamicQueryWithEquals dqwe2 = new DynamicQueryWithEquals();
        dqwe2.setId(1001);
        dqwe2.setName("N2");
        DynamicQueryWithEquals dqwe3 = new DynamicQueryWithEquals();
        dqwe3.setId(1000);
        dqwe3.setName("N1");
        x1 = bean.countLocalWithDynamicQueryWithEquals(dqwe1);
        x2 = bean.countLocalWithDynamicQueryWithEquals(dqwe2);
        x3 = bean.countLocalWithDynamicQueryWithEquals(dqwe3);
        Assert.assertNotEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        Assert.assertEquals(bean.countEnabledWithConfigBean(), bean.countEnabledWithConfigBean());
        Assert.assertNotEquals(bean.countDisabledWithConfigBean(), bean.countDisabledWithConfigBean());
        Assert.assertNotEquals(bean.countWithWrongCondition(), bean.countWithWrongCondition());

        Assert.assertEquals(bean.count(true), bean.count(true));
        Assert.assertNotEquals(bean.count(false), bean.count(false));

        Assert.assertNotEquals(bean.count(), bean.count1());
        Assert.assertEquals(bean.namedCount1_WithNameN1(), bean.namedCount1_WithNameN1());
        Assert.assertEquals(bean.namedCount1_WithNameN1(), bean.namedCount2_WithNameN1());
        Assert.assertNotEquals(bean.namedCount1_WithNameN1(), bean.namedCount_WithNameN2());
        Assert.assertEquals(bean.namedCount_WithNameN2(), bean.namedCount_WithNameN2());


        int v1 = bean.count("K1");
        Assert.assertEquals(v1, bean.count("K1"));

        bean.delete("K1");
        int v2 = bean.count("K1");
        Assert.assertNotEquals(v1, v2);
        bean.delete2("K1");
        Assert.assertEquals(v2, bean.count("K1"));

        bean.update("K1", 200);
        Assert.assertEquals(200, bean.count("K1"));
        bean.update2("K1", 300);
        Assert.assertEquals(200, bean.count("K1"));

        Assert.assertEquals(bean.count("K1"), bean.count("K1"));
        Assert.assertNotEquals(bean.count("K1"), bean.count("K2"));
    }

}
