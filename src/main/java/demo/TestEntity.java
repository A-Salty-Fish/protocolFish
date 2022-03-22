package demo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import proto.TestEntityOuterClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

/**
 * @author 13090
 * @version 1.0
 * @description: test message entity
 * @date 2022/3/15 16:19
 */
@Data
@NoArgsConstructor
public class TestEntity {

    public String name;

    public String name2;

    public long longNum;

    public Long longNum2;

    public int intNum;

    public Integer intNum2;

    public double doubleNum;

    public Double doubleNum2;

//    public LocalDate localDate;

    public Long localDateTime;

    public static TestEntity getRandomTestEntity() {
        Random random = new Random();
        TestEntity testEntity = new TestEntity();
        testEntity.setDoubleNum(random.nextDouble() * random.nextInt());
        testEntity.setDoubleNum2(random.nextDouble() * random.nextLong());
        testEntity.setIntNum(random.nextInt(256));
        testEntity.setIntNum2(random.nextInt(256 * 128) + 256);
        testEntity.setLongNum(random.nextLong());
        testEntity.setLongNum2((long) random.nextInt(256 * 256 * 256));
        testEntity.setName("test" + random.nextLong());
        testEntity.setName2("111111111111111111111111" + random.nextLong() + " " + random.nextLong());
//        testEntity.setLocalDate(LocalDate.now());
        testEntity.setLocalDateTime(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        return testEntity;
    }

    public static TestEntity getRandomTestEntity(double maxDouble) {
        Random random = new Random();
        TestEntity testEntity = getRandomTestEntity();
        testEntity.setDoubleNum(maxDouble * random.nextDouble());
        testEntity.setDoubleNum(maxDouble * random.nextDouble());
        return testEntity;
    }

    public static TestEntity getRandomTestEntity(double maxDouble, int stringLength) {
        Random random = new Random();
        TestEntity testEntity = getRandomTestEntity();
        testEntity.setDoubleNum(maxDouble * random.nextDouble());
        testEntity.setDoubleNum(maxDouble * random.nextDouble());
        testEntity.setName(testEntity.getName().substring(0, stringLength));
        testEntity.setName2(testEntity.getName().substring(0, stringLength));
        return testEntity;
    }


    public static TestEntity getNextNearRandomTestEntity(TestEntity testEntity) {
        TestEntity nextTestEntity = getRandomTestEntity(255,7);
        Random random = new Random();
        nextTestEntity.setIntNum(testEntity.getIntNum() + random.nextInt(3) - 2);
        nextTestEntity.setIntNum2(testEntity.getIntNum2() + random.nextInt(3) - 2);
        nextTestEntity.setLongNum(testEntity.getLongNum() + random.nextInt(5) - 2);
        nextTestEntity.setLongNum2(testEntity.getLongNum2() + random.nextInt(5) - 2);
        nextTestEntity.setDoubleNum(testEntity.getDoubleNum() + random.nextDouble() - 0.5);
        nextTestEntity.setDoubleNum2(testEntity.getDoubleNum2() + random.nextDouble() - 0.5);
        nextTestEntity.setLocalDateTime(testEntity.getLocalDateTime() + random.nextInt(2));
        return nextTestEntity;
    }

    public static TestEntityOuterClass.TestEntity getProtocolEntityFromTestEntity(TestEntity testEntity) {
        return TestEntityOuterClass.TestEntity.newBuilder()
                .setDoubleNum(testEntity.getDoubleNum())
                .setDoubleNum2(testEntity.getDoubleNum2())
                .setIntNum(testEntity.getIntNum())
                .setIntNum2(testEntity.getIntNum2())
                .setLongNum(testEntity.getLongNum())
                .setLongNum2(testEntity.getLongNum2())
                .setName(testEntity.getName())
                .setName2(testEntity.getName2())
//                .setLocalDate(testEntity.getLocalDate().toEpochDay())
                .setLocalDateTime(testEntity.getLocalDateTime())
                .build();
    }
}
