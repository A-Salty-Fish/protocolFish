package util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/19 17:00
 */
@Slf4j
public class ServerStoreConfig {

    private static final ConcurrentHashMap<String, ProtocolConfig> protocolConfigMap = new ConcurrentHashMap<>();

    public static void put(String key, ProtocolConfig config) {
        protocolConfigMap.put(key, config);
    }

    public static ProtocolConfig get(String key) {
        return protocolConfigMap.get(key);
    }
}
