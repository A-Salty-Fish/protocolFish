package demo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public LocalDateTime localDateTime;

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
        testEntity.setLocalDateTime(LocalDateTime.now());
        return testEntity;
    }
}
