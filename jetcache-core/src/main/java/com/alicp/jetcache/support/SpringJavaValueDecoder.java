/**
 * Created on 2018/6/7.
 */
package com.alicp.jetcache.support;

import org.springframework.core.ConfigurableObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringJavaValueDecoder extends JavaValueDecoder {

    public static final SpringJavaValueDecoder INSTANCE = new SpringJavaValueDecoder(true);

    public SpringJavaValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    protected ObjectInputStream buildObjectInputStream(ByteArrayInputStream in) throws IOException {
        return new ConfigurableObjectInputStream(in, Thread.currentThread().getContextClassLoader());
    }
}
