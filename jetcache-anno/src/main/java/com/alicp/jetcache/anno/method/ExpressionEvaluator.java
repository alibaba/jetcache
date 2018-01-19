/**
 * Created on 2018/1/19.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.anno.CacheConsts;
import org.mvel2.MVEL;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ExpressionEvaluator implements Function<Object, Object> {
    private static final Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*\\{(.+)\\}\\s*");
    private Function<Object, Object> target;

    public ExpressionEvaluator(String script){
        Object rt[] = parseEL(script);
        EL el = (EL) rt[0];
        String realScript = (String) rt[1];
        if (el == EL.MVEL) {
            target = new MvelEvaluator(realScript);
        } else if (el == EL.SPRING_EL) {
            target = new SpelEvaluator(realScript);
        } else {
            throw new CacheException("not support yet:" + script);
        }
    }

    private Object[] parseEL(String script) {
        if (script == null || script.trim().equals("")) {
            return null;
        }
        Object[] rt = new Object[2];
        Matcher matcher = pattern.matcher(script);
        if (!matcher.matches()) {
            if(CacheConsts.UNDEFINED_STRING.equals(script)){
                return null;
            } else {
                rt[0] = EL.SPRING_EL; // default spel since 2.4
                rt[1] = script;
                return rt;
            }
        } else {
            String s = matcher.group(1);
            if ("spel".equals(s)) {
                rt[0] = EL.SPRING_EL;
            } else if ("mvel".equals(s)) {
                rt[0] = EL.MVEL;
            }/* else if ("buildin".equals(s)) {
                rt[0] = EL.BUILD_IN;
            } */else {
                throw new CacheConfigException("Can't parse \"" + script + "\"");
            }
            rt[1] = matcher.group(2);
            return rt;
        }
    }

    @Override
    public Object apply(Object o) {
        return target.apply(o);
    }
}

class MvelEvaluator implements Function<Object, Object> {
    private String script;

    public MvelEvaluator(String script){
        this.script = script;
    }

    @Override
    public Object apply(Object context) {
        return MVEL.eval(script, context);
    }
}

class SpelEvaluator implements Function<Object, Object> {

    private static ExpressionParser parser = new SpelExpressionParser();
    private final Expression expression;

    public SpelEvaluator(String script){
        expression = parser.parseExpression(script);
    }

    @Override
    public Object apply(Object rootObject) {
        EvaluationContext context = new StandardEvaluationContext(rootObject);
        return expression.getValue(context);
    }
}
