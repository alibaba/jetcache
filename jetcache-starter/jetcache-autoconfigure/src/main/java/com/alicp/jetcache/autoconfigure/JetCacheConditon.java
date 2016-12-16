package com.alicp.jetcache.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 2016/11/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class JetCacheConditon extends SpringBootCondition {

    private String cacheArea;

    protected JetCacheConditon(String cacheArea) {
        this.cacheArea = cacheArea;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
                conditionContext.getEnvironment(), "jetcache.");
        if (match(resolver, "local.") || match(resolver, "remote.")) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("no match " + cacheArea);
        }
    }

    private boolean match(RelaxedPropertyResolver resolver, String prefix) {
        Map<String, Object> m = resolver.getSubProperties(prefix);
        Set<String> cacheAreaNames = m.keySet().stream().map((s) -> s.substring(0, s.indexOf('.'))).collect(Collectors.toSet());
        return cacheAreaNames.stream().anyMatch((s) -> cacheArea.equals(m.get(s + ".type")));
    }
}
