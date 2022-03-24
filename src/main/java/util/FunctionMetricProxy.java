package util;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/24 20:16
 */
@Slf4j
public class FunctionMetricProxy implements MethodInterceptor {

    public static ConcurrentHashMap<String, AtomicLong> functionMetric = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        long start = System.nanoTime();
        Object result = methodProxy.invokeSuper(obj, args);
        long duration = System.nanoTime() - start;
        functionMetric.computeIfPresent(method.getName(), (key, value) -> new AtomicLong(value.addAndGet(duration)));
        functionMetric.putIfAbsent(method.getName(), new AtomicLong(duration));
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new FunctionMetricProxy());
        return (T) enhancer.create();
    }

    public static void logDurations() {
        long sum = 0;
        for (AtomicLong duration : functionMetric.values()) {
            sum += duration.get();
        }
        log.info("function name\tduration\tpercent\t");
        for (String key : functionMetric.keySet()) {
            log.info("{}\t{} ns\t{}", key, functionMetric.get(key).get(), (double) functionMetric.get(key).get() * 100 / sum);
        }
    }
}
