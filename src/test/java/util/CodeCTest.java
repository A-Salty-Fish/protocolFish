package util;

import com.sun.org.apache.bcel.internal.classfile.Code;
import demo.TestEntity;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/18 19:27
 */

public class CodeCTest {

    public static void main(String[] args) {
        CodecUtil.registerClass(TestEntity.class);
    }
}
