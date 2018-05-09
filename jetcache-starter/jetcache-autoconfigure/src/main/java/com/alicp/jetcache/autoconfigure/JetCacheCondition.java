package com.alicp.jetcache.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 2016/11/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class JetCacheCondition extends SpringBootCondition {

    private String[] cacheTypes;

    protected JetCacheCondition(String... cacheTypes) {
        Objects.requireNonNull(cacheTypes, "cacheTypes can't be null");
        Assert.isTrue(cacheTypes.length > 0, "cacheTypes length is 0");
        this.cacheTypes = cacheTypes;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        ConfigTree ct = new ConfigTree((ConfigurableEnvironment) conditionContext.getEnvironment(), "jetcache.");
        if (match(ct, "local.") || match(ct, "remote.")) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("no match for " + cacheTypes[0]);
        }
    }

    private boolean match(ConfigTree ct, String prefix) {
        Map<String, Object> m = ct.subTree(prefix).getProperties();
        Set<String> cacheAreaNames = m.keySet().stream().map((s) -> s.substring(0, s.indexOf('.'))).collect(Collectors.toSet());
        final List<String> cacheTypesList = Arrays.asList(cacheTypes);
        return cacheAreaNames.stream().anyMatch((s) -> cacheTypesList.contains(m.get(s + ".type")));
    }
}
