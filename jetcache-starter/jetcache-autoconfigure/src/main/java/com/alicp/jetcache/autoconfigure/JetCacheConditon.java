package com.alicp.jetcache.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 2016/11/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class JetCacheConditon extends SpringBootCondition {

    private String cacheType;

    protected JetCacheConditon(String cacheType) {
        this.cacheType = cacheType;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        ConfigTree ct = new ConfigTree((ConfigurableEnvironment) conditionContext.getEnvironment(), "jetcache.");
        if (match(ct, "local.") || match(ct, "remote.")) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("no match " + cacheType);
        }
    }

    private boolean match(ConfigTree ct, String prefix) {
        Map<String, Object> m = ct.subTree(prefix).getProperties();
        Set<String> cacheAreaNames = m.keySet().stream().map((s) -> s.substring(0, s.indexOf('.'))).collect(Collectors.toSet());
        return cacheAreaNames.stream().anyMatch((s) -> cacheType.equals(m.get(s + ".type")));
    }
}
