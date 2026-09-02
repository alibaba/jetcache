# JetCache Agent Instructions

## Project Overview

JetCache is a Java cache abstraction library (groupId `com.alicp.jetcache`) providing uniform API over Redis (Jedis/Lettuce/Spring Data/Redisson), Caffeine, and LinkedHashMap. Supports annotations (`@Cached`, `@CacheUpdate`, `@CacheInvalidate`) and programmatic `Cache` API with two-level caching, TTL, auto-refresh, and distributed lock.

- Source/target: Java 17 (compiled with `-parameters`, enforcer requires JDK 17+)
- CI JDK: 17 (Temurin)
- Version: 2.8.x-SNAPSHOT

## Module Structure

```
jetcache-anno-api/     Annotations and constants only (no dependencies)
jetcache-core/         Core Cache API, CacheManager, embedded/external cache implementations
jetcache-anno/         Spring AOP-based annotation processing (depends on core)
jetcache-support/      Redis driver adapters:
  jetcache-redis/            Jedis 7.x
  jetcache-redis-lettuce/    Lettuce
  jetcache-redis-springdata/ Spring Data Redis 4.x (uses Lettuce, not Jedis)
  jetcache-redisson/         Redisson 4.x
jetcache-starter/      Spring Boot autoconfigure and starters:
  jetcache-autoconfigure/         Pulls all support modules (optional)
  jetcache-starter-redis/         etc.
jetcache-test/         All tests live here (aggregates all modules); uses JaCoCo for coverage
jetcache-parent/       Shared build config, PMD (Alibaba p3c rules), surefire settings
jetcache-bom/          Bill of Materials, dependency version management
samples/               NOT part of root build (separate pom, different groupId)
```

Dependency chain: `anno-api <- core <- anno <- autoconfigure`, `core <- support/* <- starter/*`.

## Build & Test Commands

```bash
# Full build (no tests)
mvn -DskipTests clean install

# Run all tests (requires Redis, see below)
mvn clean test

# Run a single test class
mvn -pl jetcache-test test -Dtest=CacheHandlerTest

# Run a single test method
mvn -pl jetcache-test test -Dtest=CacheHandlerTest#testExpressionEvaluator
```

Surefire includes: `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`, `**/*TestCase.java`. Surefire excludes `@Tag("slow")` tests (currently none tagged).

### macOS: Tests Needing Redis

The `docker-compose.yml` uses `network_mode: "host"`, which **does not work on macOS Docker**. Redis cluster tests skip themselves on Mac/Windows (runtime check in `RedisLettuceCacheTest.checkOS()`). To run the full suite on macOS, use the Docker-in-Docker approach from `Building.txt`:

```bash
docker compose up -d   # Linux only
# macOS alternative:
docker run -it --rm --network host -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/mymaven -w /usr/src/mymaven maven:3.9-eclipse-temurin-17 mvn clean test
```

### Quick local Redis for partial testing (Windows/Mac)

```bash
docker run --rm -it -p 6379-6381:6379-6381 -p 26379-26381:26379-26381 areyouok/redis-sentinel
```

This gives standalone + sentinel but **not** cluster mode. Cluster tests will skip.

## Verification

```bash
mvn clean test           # after `docker compose up` on Linux
mvn verify               # also runs PMD check (Alibaba p3c ruleset)
```

## Conventions

- JUnit Jupiter only (JUnit 4 removed; no vintage engine). JUnit BOM version: 6.x
- Test dependencies: JUnit Jupiter 6.x, Mockito 5.x, Spring Test
- PMD uses Alibaba p3c rules (`p3c-pmd:1.3.6`) during `verify` phase
- The `-parameters` javac flag is required for SpEL annotation key expressions to work (also must be set in IDE settings, not just pom)
- `samples/` has its own build chain with its own version of jetcache; it is not a module of the main build

## Test Caveats

- Tests can be flaky on busy machines: many tests use short `sleep()` to verify TTL/expiry behavior, so GC pauses or high load can cause false failures
- Full test suite takes a few minutes to run
- Tests needing Redis (e.g. `RedisCacheTest`, `RedisLettuceCacheTest`, `RedissonCacheTest`, starter tests) will fail or skip without a running Redis
- Redis cluster tests auto-skip on Mac/Windows via `RedisLettuceCacheTest.checkOS()` — only standalone and sentinel tests work there

## Compatibility Notes (see `docs/EN/Compatibility.md`)

- Spring/Spring Boot version support varies by jetcache version; current (2.8.0) BOM defaults to Spring Framework 7.0.7 / Spring Boot 4.0.6 / Spring Data Redis 4.0.5 / SLF4J 2.x, but Spring 6.x / Spring Boot 3.x (also Java 17+) is also supported by adjusting BOM properties
- `jetcache-redis` (Jedis 7.x) and `jetcache-redis-springdata` (Lettuce) use different Redis clients; `jetcache-redis` uses Jedis 7.x which may conflict with other Jedis versions on classpath
- Since 2.8.0: fastjson1 is **removed**; the `FASTJSON` keyConvertor now uses fastjson2 internally. Default keyConvertor is `fastjson2`
- Since 2.8.0: kryo4 is **dropped**. Both `KRYO` and `KRYO5` constants now use kryo5 implementation internally (`com.esotericsoftware:kryo` is 5.6.2, same as `com.esotericsoftware.kryo:kryo5`). Old kryo4 serialized data is **not compatible** with kryo5
- Since 2.8.0: Spring XML namespace support (`<jetcache:xxx>`) is **removed**
- Since 2.8.0: **Deserialization filter is enabled by default** — a breaking change. Cached values containing custom classes not in the default allow list will fail to deserialize. Add `decodeFilterAllowPatterns` (e.g. `com.yourcompany.`) or set `decodeFilterEnabled: false` to restore old behavior. See `docs/EN/Config.md` for default allow list
- Since 2.8.0: `JACKSON3` added as valueEncoder/valueDecoder and keyConvertor option (uses `tools.jackson` 3.x)
- `areaInCacheName` default changed to `false` in 2.7 (was `true` in 2.6 and earlier); since 2.8.0 this is also `false`
- JSON serializers (fastjson2/jackson/jackson3) are **not registered by default** as valueEncoder/valueDecoder due to type-safety concerns with `Object` fields. Only `java` and `kryo`/`kryo5` are registered
- `@Cached` uses Spring AOP (proxy-based); self-invocation within the same class bypasses the cache

## Key Convertor / Value Encoder-Decoder Choices (2.8)

- keyConvertor: `fastjson2` (default) / `fastjson` (same as fastjson2) / `jackson` / `jackson3` / `none` (local only)
- valueEncoder/valueDecoder: `java` (default) / `kryo` (kryo5 impl) / `kryo5` (kryo5 impl). `fastjson2` / `jackson` / `jackson3` available but not registered by default

## Code Reading Guide (see `docs/CN/DevNote.md`)

Key `Cache` interface implementations:
- `RedisCache` / `RedisLettuceCache` / `RedisSpringDataCache` / `RedissonCache`: external Redis backends
- `CaffeineCache` / `LinkedHashMapCache`: in-memory
- `LoadingCache`: decorator adding auto-load via `CacheLoader`
- `RefreshCache`: decorator adding auto-refresh
- `MultiLevelCache`: multi-level (supports N levels, annotation config only uses 2)

Annotation processing entry points: `EnableMethodCache`, `EnableCreateCacheAnnotation` (in `jetcache-anno`)
