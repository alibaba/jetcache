/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.support.FastjsonKeyConvertor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.StaticApplicationContext;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultSpringKeyConvertorTest {
    private StaticApplicationContext context;
    private DefaultListableBeanFactory beanFactory;
    private DefaultSpringKeyConvertorParser parser;


    @BeforeEach
    public void setup() {
        context = new StaticApplicationContext();
        beanFactory = context.getDefaultListableBeanFactory();
        parser = new DefaultSpringKeyConvertorParser();
        parser.setApplicationContext(context);
    }

    @Test
    public void testParseKeyConvertor() {
        assertSame(FastjsonKeyConvertor.INSTANCE, parser.parseKeyConvertor("fastjson"));
        Function<Object, Object> func = o -> null;
        beanFactory.registerSingleton("cvt", func);
        assertSame(func, parser.parseKeyConvertor("bean:cvt"));
        assertThrows(NoSuchBeanDefinitionException.class, () -> parser.parseKeyConvertor("bean:not_exists"));
    }
}
