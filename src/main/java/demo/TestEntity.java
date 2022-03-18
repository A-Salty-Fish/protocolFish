package demo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author 13090
 * @version 1.0
 * @description: test message entity
 * @date 2022/3/15 16:19
 */
@Data
public class TestEntity {

    private String name;

    private String name2;

    public long longNum;

    public Long longNum2;

    public int intNum;

    public Integer intNum2;

    public double doubleNum;

    public Double doubleNum2;

    public LocalDate localDate;

    public LocalDateTime localDateTime;
}
