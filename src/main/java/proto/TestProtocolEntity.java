package proto;

import java.time.LocalDate;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/18 20:15
 */

public class TestProtocolEntity {
    public static void main(String[] args) {
        TestEntityOuterClass.TestEntity testEntity = TestEntityOuterClass.TestEntity.newBuilder()
                .setDoubleNum(1.1)
                .setDoubleNum2(2.2)
                .setIntNum(1)
                .setIntNum2(111111111)
                .setLongNum(1L)
                .setLongNum(1111111111111111111L)
                .setName("name")
                .setName2("name22222222222222222222222222222222222")
                .setLocalDate(System.currentTimeMillis())
                .setLocalDateTime(System.currentTimeMillis())
                .build();
        System.out.println(testEntity.toByteArray().length);
    }
}
