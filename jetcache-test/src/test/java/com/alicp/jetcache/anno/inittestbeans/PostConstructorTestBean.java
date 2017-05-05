package com.alicp.jetcache.anno.inittestbeans;

import com.alicp.jetcache.test.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 2017/5/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component
public class PostConstructorTestBean extends InitTestBean {

    @PostConstruct
    public void doTest() {
        super.doTest();
    }


}
