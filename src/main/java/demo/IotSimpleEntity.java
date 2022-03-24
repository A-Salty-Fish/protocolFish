package demo;

import com.google.gson.Gson;

import java.util.Random;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/23 20:21
 */

public class IotSimpleEntity {

    public long deviceId;

    public long geoHash;

    public long timestamp;

    public int intValue1;

    public int intValue2;

    public long longValue1;

    public long longValue2;

    public double doubleValue1;

    public double doubleValue2;

    public String stringValue1;

    public static IotSimpleEntity randomIotSimpleEntity() {
        Random random = new Random();
        IotSimpleEntity iotSimpleEntity = new IotSimpleEntity();
        iotSimpleEntity.deviceId = random.nextInt(1<<8);
        iotSimpleEntity.geoHash = random.nextInt();
        iotSimpleEntity.timestamp = System.currentTimeMillis();
        iotSimpleEntity.intValue1 = random.nextInt(1<<8);
        iotSimpleEntity.intValue2 = random.nextInt();
        iotSimpleEntity.longValue1 = random.nextInt();
        iotSimpleEntity.longValue2 = random.nextLong();
        iotSimpleEntity.doubleValue1 = random.nextDouble() * iotSimpleEntity.longValue1;
        iotSimpleEntity.doubleValue2 = random.nextDouble() * iotSimpleEntity.longValue2;
        iotSimpleEntity.stringValue1 = new Gson().toJson(iotSimpleEntity);
//        iotSimpleEntity.stringValue2 = iotSimpleEntity.stringValue1.substring(random.nextInt(10) + 1);
        return iotSimpleEntity;
    }

    public static IotSimpleEntity randomIotSimpleEntity(int StrMaxLen) {
        Random random = new Random();
        IotSimpleEntity iotSimpleEntity = IotSimpleEntity.randomIotSimpleEntity();
        iotSimpleEntity.stringValue1 = iotSimpleEntity.stringValue1.substring(0, random.nextInt(StrMaxLen));
        return iotSimpleEntity;
    }


}
