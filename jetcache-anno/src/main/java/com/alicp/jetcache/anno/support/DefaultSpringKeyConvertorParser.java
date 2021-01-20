/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultSpringKeyConvertorParser extends DefaultKeyConvertorParser {


    @Override
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        String beanName = DefaultSpringEncoderParser.parseBeanName(convertor);
        if (beanName == null) {
            return super.parseKeyConvertor(convertor);
        } else {
            return (Function<Object, Object>) SpringBeanUtil.getBean(beanName);
        }
    }


}
