package demo;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/20 19:57
 */
@XmlRootElement
public class TestXmlEntity {

    public String name;

    public String name2;

    public long longNum;

    public Long longNum2;

    public int intNum;

    public Integer intNum2;

    public double doubleNum;

    public Double doubleNum2;

    public Long localDateTime;

    public static TestXmlEntity TestXmlEntityFromTestEntity(TestEntity testEntity) {
        TestXmlEntity testXmlEntity = new TestXmlEntity();
        testXmlEntity.name = testEntity.name;
        testXmlEntity.name2 = testEntity.name2;
        testXmlEntity.longNum = testEntity.longNum;
        testXmlEntity.longNum2 = testEntity.longNum2;
        testXmlEntity.intNum = testEntity.intNum;
        testXmlEntity.intNum2 = testEntity.intNum2;
        testXmlEntity.doubleNum = testEntity.doubleNum;
        testXmlEntity.doubleNum2 = testEntity.doubleNum2;
        testXmlEntity.localDateTime = testEntity.localDateTime;;
        return testXmlEntity;
    }
}
