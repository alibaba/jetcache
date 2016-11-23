/**
 * Created on  13-09-17 11:14
 */
package com.alicp.jetcache.anno.config.xml;

import com.alicp.jetcache.anno.config.SpringTest;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class XmlConfigTest extends SpringTest {

    @Test
    public void test3_X() {
        context = new ClassPathXmlApplicationContext("beans3.0.xml");
        doTest();
    }

}
