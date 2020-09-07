package cc.mrbird.febs.common.function;

/**
 * 函数式接口 函数接口注解
 * @param <T>
 */
@FunctionalInterface
public interface CacheSelector<T> {
    T select() throws Exception;
}
