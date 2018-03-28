/**
 * Created on 2018/3/28.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.alicp.jetcache.support.KryoValueDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.StaticApplicationContext;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringConfigProviderTest {
    private StaticApplicationContext context;
    private DefaultListableBeanFactory beanFactory;
    private SpringConfigProvider cp;


    @BeforeEach
    public void setup() {
        context = new StaticApplicationContext();
        beanFactory = context.getDefaultListableBeanFactory();
        cp = new SpringConfigProvider();
        cp.setApplicationContext(context);
    }

    @Test
    public void testParseValueEncoder() {
        assertEquals(JavaValueEncoder.class, cp.parseValueEncoder("java").getClass());

        Function<Object, byte[]> func = o -> null;
        beanFactory.registerSingleton("myBean", func);
        assertSame(func, cp.parseValueEncoder("bean:myBean"));

        SerialPolicy sp = new SerialPolicy() {
            @Override
            public Function<Object, byte[]> encoder() {
                return func;
            }

            @Override
            public Function<byte[], Object> decoder() {
                return null;
            }
        };
        beanFactory.registerSingleton("sp", sp);
        assertSame(func, cp.parseValueEncoder("bean:sp"));

        assertThrows(NoSuchBeanDefinitionException.class, () -> cp.parseValueEncoder("bean:not_exists"));
    }

    @Test
    public void testParseValueDecoder() {
        assertEquals(KryoValueDecoder.class, cp.parseValueDecoder("kryo").getClass());

        Function<byte[], Object> func = o -> null;
        beanFactory.registerSingleton("myBean", func);
        assertSame(func, cp.parseValueDecoder("bean:myBean"));

        SerialPolicy sp = new SerialPolicy() {
            @Override
            public Function<Object, byte[]> encoder() {
                return null;
            }

            @Override
            public Function<byte[], Object> decoder() {
                return func;
            }
        };
        beanFactory.registerSingleton("sp", sp);
        assertSame(func, cp.parseValueDecoder("bean:sp"));

        assertThrows(NoSuchBeanDefinitionException.class, () -> cp.parseValueDecoder("bean:not_exists"));
    }

    @Test
    public void testParseKeyConvertor() {
        assertSame(FastjsonKeyConvertor.INSTANCE, cp.parseKeyConvertor("fastjson"));
        Function<Object, Object> func = o -> null;
        beanFactory.registerSingleton("cvt", func);
        assertSame(func, cp.parseKeyConvertor("bean:cvt"));
        assertThrows(NoSuchBeanDefinitionException.class, () -> cp.parseKeyConvertor("bean:not_exists"));
    }

}
