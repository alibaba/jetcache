package jetcache.samples.springboot;

import com.alicp.jetcache.autoconfigure.AutoConfigureBeans;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author youjie_li
 */
@Configuration
public class JetCacheInterceptorConfig {

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    private ExternalCacheWriteInterceptor createAuditInterceptor() {
        return new AuditExternalCacheWriteInterceptor();
    }

    @PostConstruct
    public void registerInterceptors() {
        autoConfigureBeans.getCustomContainer().put("auditExternalCacheWriteInterceptor", createAuditInterceptor());
    }
}
