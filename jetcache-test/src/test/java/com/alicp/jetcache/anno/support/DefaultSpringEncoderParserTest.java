/**
 * Created on 2018/3/28.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.SerialPolicy;
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
public class DefaultSpringEncoderParserTest {
    private StaticApplicationContext context;
    private DefaultListableBeanFactory beanFactory;
    private DefaultSpringEncoderParser parser;


    @BeforeEach
    public void setup() {
        context = new StaticApplicationContext();
        beanFactory = context.getDefaultListableBeanFactory();
        parser = new DefaultSpringEncoderParser();
        parser.setApplicationContext(context);
    }

    @Test
    public void testParseValueEncoder() {
        assertEquals(JavaValueEncoder.class, parser.parseEncoder("java").getClass());

        Function<Object, byte[]> func = o -> null;
        beanFactory.registerSingleton("myBean", func);
        assertSame(func, parser.parseEncoder("bean:myBean"));

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
        assertSame(func, parser.parseEncoder("bean:sp"));

        assertThrows(NoSuchBeanDefinitionException.class, () -> parser.parseEncoder("bean:not_exists"));
    }

    @Test
    public void testParseValueDecoder() {
        assertEquals(KryoValueDecoder.class, parser.parseDecoder("kryo").getClass());

        Function<byte[], Object> func = o -> null;
        beanFactory.registerSingleton("myBean", func);
        assertSame(func, parser.parseDecoder("bean:myBean"));

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
        assertSame(func, parser.parseDecoder("bean:sp"));

        assertThrows(NoSuchBeanDefinitionException.class, () -> parser.parseDecoder("bean:not_exists"));
    }

}
