/**
 * Created on 2026-05-21.
 */
package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputFilter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Deserialization filter to prevent arbitrary class deserialization attacks.
 * <p>
 * By default, only classes in commonly used Java packages and the JetCache package are allowed.
 * Users can add additional patterns via {@link #addAllowPatterns(String...)}.
 * <p>
 * A shared default instance is available via {@link #getDefault()}. Custom instances can be
 * created via the public constructor if needed (e.g. for per-context isolation).
 * <p>
 * Pattern syntax (applies to both allow and deny patterns):
 * <ul>
 *   <li>Prefix (ends with "."): matches all classes in the package and subpackages, e.g. "java.util." matches
 *       java.util.HashMap and java.util.concurrent.ConcurrentHashMap</li>
 *   <li>Exact class name: matches a single class exactly, e.g. "org.myapp.dto.UserDTO" matches only that class</li>
 *   <li>Package-only (no trailing ".", not a class name): matches only classes directly in the package,
 *       e.g. "java.lang" matches java.lang.String but not java.lang.reflect.Proxy</li>
 * </ul>
 *
 * @author huangli
 */
public class DecodeFilter {

    private static final Logger logger = LoggerFactory.getLogger(DecodeFilter.class);

    /**
     * Default allow patterns: commonly used Java packages for cache values.
     * <p>
     * "java.lang" uses package-only matching so subpackages like reflect/invoke are automatically excluded.
     * "java.util." / "java.time." use prefix matching to include subpackages.
     * "java.math" uses package-only matching (no subpackages exist under java.math).
     * "java.net" uses package-only matching (direct classes only, excluding subpackages like spi).
     * <p>
     * java.io is NOT included by default; users can add it via configuration if needed.
     */
    public static final Set<String> DEFAULT_ALLOW_PATTERNS = Set.of(
            "java.lang",
            "java.util.",
            "java.time.",
            "java.math",
            "java.net",
            "com.alicp.jetcache."
    );

    /**
     * Default deny patterns: explicitly blocked packages, subpackages and exact class names.
     * <p>
     * Deny patterns have highest priority and cannot be overridden by allow patterns.
     */
    public static final Set<String> DEFAULT_DENY_PATTERNS = Set.of(
            // java.lang dangerous subpackages (defense-in-depth for "java.lang." prefix)
            "java.lang.reflect.",
            "java.lang.invoke.",
            "java.lang.management.",
            "java.lang.instrument.",
            "java.lang.module.",
            "java.lang.constant.",
            // java.lang dangerous classes
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.ProcessImpl",
            "java.lang.UNIXProcess",
            "java.lang.Shutdown",
            "java.lang.Thread",
            "java.lang.ThreadGroup",
            "java.lang.ClassLoader",
            "java.lang.System",
            "java.lang.SecurityManager",
            "java.lang.StackWalker",
            // java.beans EventHandler (classic deserialization gadget)
            "java.beans.EventHandler",
            // JNDI/RMI (high risk gadget chains)
            "javax.naming.",
            "java.rmi.",
            // javax.script (ScriptEngineManager gadget)
            "javax.script.",
            // javax.management (JMX MLet/remote gadget chains, defense-in-depth: ClassLoader/RMI already blocked)
            "javax.management.",
            // JDK internal classes (com.sun/sun are JDK internal APIs, no business classes exist here)
            "com.sun.",
            "sun.",
            // Apache Commons Collections gadget chains (InvokerTransformer, ChainedTransformer, LazyMap etc.)
            "org.apache.commons.collections.functors.",
            "org.apache.commons.collections.map.LazyMap",
            "org.apache.commons.collections4.functors.",
            "org.apache.commons.collections4.map.LazyMap",
            // Apache Commons BeanUtils (BeanComparator gadget)
            "org.apache.commons.beanutils.",
            // Groovy runtime (MethodClosure, ConvertedClosure gadgets)
            "org.codehaus.groovy.runtime.",
            "org.codehaus.groovy.reflection.",
            // C3P0 (PoolBackedDataSource gadget)
            "com.mchange.v2.",
            // Spring expression language (SpEL injection)
            "org.springframework.expression.",
            // Spring framework gadget chains (defense-in-depth: sink classes blocked by com.sun.)
            "org.springframework.aop.framework.JdkDynamicAopProxy",
            "org.springframework.core.SerializableTypeWrapper$MethodInvokeTypeProvider",
            // AspectJ Weaver (defense-in-depth: chain also requires Commons Collections which is already blocked)
            "org.aspectj.weaver.",
            // Hibernate gadget chains (TypedValue hashCode trigger → ComponentType → TemplatesImpl/JNDI)
            "org.hibernate.engine.spi.TypedValue",
            "org.hibernate.type.ComponentType",
            "org.hibernate.tuple.component.AbstractComponentTuplizer",
            "org.hibernate.property.access.spi.GetterMethodImpl",
            // Hibernate 4.x getter (ysoserial uses this instead of GetterMethodImpl for 4.x)
            "org.hibernate.property.BasicPropertyAccessor$BasicGetter",
            "org.hibernate.internal.util.ValueHolder",
            // Hessian (gadget chains)
            "com.caucho.",
            // javassist (bytecode manipulation used in gadget chains)
            "javassist.",
            // Jython (Python script execution)
            "org.python.",
            // Mozilla Rhino (JavaScript execution)
            "org.mozilla.javascript.",
            // BeanShell (script execution)
            "bsh.",
            // Clojure (script execution)
            "clojure.",
            // ROME (ToStringBean/ObjectBean gadget chains)
            "com.rometools.",
            // Vaadin (NestedMethodProperty gadget chain)
            "com.vaadin.",
            // Apache Click (Column$ColumnComparator gadget chain)
            "org.apache.click.",
            // Apache Wicket (DiskFileItem file write gadget)
            "org.apache.wicket."
    );

    private static final DecodeFilter INSTANCE = new DecodeFilter();

    private volatile boolean enabled = true;
    private final CopyOnWriteArraySet<String> allowPatterns;
    private final CopyOnWriteArraySet<String> denyPatterns;
    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    public DecodeFilter() {
        this(DEFAULT_ALLOW_PATTERNS, DEFAULT_DENY_PATTERNS);
    }

    public DecodeFilter(Set<String> allowPatterns, Set<String> denyPatterns) {
        this.allowPatterns = new CopyOnWriteArraySet<>(allowPatterns);
        this.denyPatterns = new CopyOnWriteArraySet<>(denyPatterns);
    }

    public static DecodeFilter getDefault() {
        return INSTANCE;
    }

    /**
     * Check if a class name is allowed for deserialization.
     * <p>
     * When this method returns {@code false}, an error log is automatically emitted
     * with the specific block reason (e.g. matched deny pattern, no matching allow pattern).
     *
     * @param className the fully qualified class name (may include array type notation)
     * @return true if allowed, false if blocked
     */
    public boolean isAllowed(String className) {
        if (!enabled) {
            return true;
        }
        if (className == null || className.isEmpty()) {
            return true;
        }

        // Check cache first (only positive results are cached)
        Boolean cached = cache.get(className);
        if (cached != null) {
            return cached;
        }

        String blockReason = checkBlockedReason(className);
        if (blockReason != null) {
            logBlocked(className, blockReason);
            return false;
        }

        // Only cache positive results to prevent unbounded growth from attack payloads
        cache.put(className, true);
        return true;
    }

    private String checkBlockedReason(String className) {
        String componentType = extractComponentType(className);
        if (componentType == null) {
            return null;
        }
        if (componentType.isEmpty() && className.startsWith("[")) {
            return "invalid array type notation";
        }

        String nameToCheck = componentType.isEmpty() ? className : componentType;

        // Deny list has highest priority (allow patterns cannot bypass deny patterns)
        // Deny uses the same matching logic as allow: prefix, exact, and package-only.
        // WARNING: deny patterns can be removed via removeDenyPatterns/clearDenyPatterns,
        // which is a high-risk operation that weakens the security baseline
        for (String deny : denyPatterns) {
            if (matches(deny, nameToCheck)) {
                return "matched deny pattern: '" + deny + "'";
            }
        }

        for (String pattern : allowPatterns) {
            if (matches(pattern, nameToCheck)) {
                return null;
            }
        }

        return "no matching allow pattern found";
    }

    private String extractComponentType(String type) {
        if (!type.startsWith("[")) {
            return "";
        }

        int i = 0;
        while (i < type.length() && type.charAt(i) == '[') {
            i++;
        }

        if (i >= type.length()) {
            return "";
        }

        char c = type.charAt(i);
        if (c == 'L') {
            int start = i + 1;
            int end = type.length() - 1;
            if (end <= start || type.charAt(type.length() - 1) != ';') {
                return "";
            }
            return type.substring(start, end);
        } else if (c == 'B' || c == 'C' || c == 'D' || c == 'F' || c == 'I' || c == 'J' || c == 'S' || c == 'Z') {
            return null;
        } else {
            return "";
        }
    }

    private boolean matches(String pattern, String className) {
        if (pattern.endsWith(".")) {
            // Prefix matching (recursive into subpackages)
            return className.startsWith(pattern);
        }
        // Exact class name matching
        if (className.equals(pattern)) {
            return true;
        }
        // Package-only matching: class is directly in this package, not subpackages
        String prefix = pattern + ".";
        return className.startsWith(prefix)
                && className.indexOf('.', prefix.length()) == -1;
    }

    /**
     * Enable or disable the filter check.
     * When toggled, the cache is cleared.
     *
     * @param enabled true to enable, false to disable
     */
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        cache.clear();
    }

    /**
     * @return true if filter is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Add allow patterns.
     * Cache is cleared once after all patterns are added.
     *
     * @param patterns patterns to add
     */
    public synchronized void addAllowPatterns(String... patterns) {
        for (String pattern : patterns) {
            if (pattern != null && !pattern.isEmpty()) {
                allowPatterns.add(pattern);
            }
        }
        cache.clear();
    }

    /**
     * Remove allow patterns.
     * Cache is cleared once after all patterns are removed.
     *
     * @param patterns patterns to remove
     */
    public synchronized void removeAllowPatterns(String... patterns) {
        boolean changed = false;
        for (String pattern : patterns) {
            if (pattern != null && allowPatterns.remove(pattern)) {
                changed = true;
            }
        }
        if (changed) {
            cache.clear();
        }
    }

    /**
     * Clear all allow patterns.
     * Cache is cleared after this operation.
     */
    public synchronized void clearAllowPatterns() {
        allowPatterns.clear();
        cache.clear();
    }

    /**
     * Add deny patterns.
     * Cache is cleared once after all patterns are added.
     *
     * @param patterns patterns to add
     */
    public synchronized void addDenyPatterns(String... patterns) {
        for (String pattern : patterns) {
            if (pattern != null && !pattern.isEmpty()) {
                denyPatterns.add(pattern);
            }
        }
        cache.clear();
    }

    /**
     * Remove deny patterns.
     * Cache is cleared once after all patterns are removed.
     * <p>
     * <b>WARNING:</b> Removing default deny patterns (such as {@code java.rmi.},
     * {@code javax.naming.}, {@code java.lang.Runtime}, etc.) weakens the security
     * baseline and may expose the application to deserialization attacks.
     * Only remove deny patterns if you fully understand the security implications.
     *
     * @param patterns patterns to remove
     */
    public synchronized void removeDenyPatterns(String... patterns) {
        boolean changed = false;
        for (String pattern : patterns) {
            if (pattern != null && denyPatterns.remove(pattern)) {
                changed = true;
            }
        }
        if (changed) {
            cache.clear();
        }
    }

    /**
     * Clear all deny patterns.
     * Cache is cleared after this operation.
     * <p>
     * <b>WARNING:</b> This removes the entire security deny list, including built-in
     * protections against known deserialization gadget chains (JNDI/RMI, {@code Runtime},
     * {@code ProcessBuilder}, etc.). Only use this if you have an alternative security
     * mechanism in place. Calling {@link #reset()} is safer if you want to restore defaults.
     */
    public synchronized void clearDenyPatterns() {
        denyPatterns.clear();
        cache.clear();
    }

    /**
     * Clear the internal class name cache.
     */
    public synchronized void clearCache() {
        cache.clear();
    }

    /**
     * Reset to default state (for testing purposes).
     */
    synchronized void reset() {
        enabled = true;
        allowPatterns.clear();
        allowPatterns.addAll(DEFAULT_ALLOW_PATTERNS);
        denyPatterns.clear();
        denyPatterns.addAll(DEFAULT_DENY_PATTERNS);
        cache.clear();
    }

    /**
     * ObjectInputFilter implementation for use with ObjectInputStream.setObjectInputFilter.
     * <p>
     * Returns {@link ObjectInputFilter.Status#REJECTED} for classes not allowed by the filter,
     * which causes the JDK to throw {@link java.io.InvalidClassException}.
     * An error log with configuration guidance is also emitted.
     *
     * @param filterInfo the filter info provided by ObjectInputStream
     * @return ALLOWED if class is allowed by the filter, REJECTED if blocked, UNDECIDED if filter disabled
     */
    public static ObjectInputFilter.Status javaFilter(ObjectInputFilter.FilterInfo filterInfo) {
        if (filterInfo.serialClass() == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        if (!getDefault().isEnabled()) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        String className = filterInfo.serialClass().getName();
        if (getDefault().isAllowed(className)) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    }

    private static void logBlocked(String className, String reason) {
        logger.error("Class '{}' is not allowed by the deserialization filter and has been blocked for security.\n" +
                "\nReason: {}\n" +
                "\nTo allow this class, add a pattern to your configuration:\n" +
                "\nYAML:\n" +
                "\n  jetcache:\n" +
                "    decodeFilterAllowPatterns:\n" +
                "      - com.example.\n" +
                "\nOr programmatically:\n" +
                "\n  DecodeFilter.getDefault().addAllowPatterns(\"com.example.\");\n" +
                "\nIf you have configured deny patterns, this class may also be blocked by them.\n" +
                "Check your 'decodeFilterDenyPatterns' configuration if applicable.\n" +
                "\nYou can also disable the filter (NOT RECOMMENDED):\n" +
                "\n  jetcache.decodeFilterEnabled: false",
                className, reason);
    }

}
