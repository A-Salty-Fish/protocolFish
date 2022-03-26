package util;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import demo.HomeIotEntity;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author 13090
 * @version 1.0
 * @description: TODO
 * @date 2022/3/26 19:45
 */

public class CsvUtilTest {
    @Test
    public void testReadCsv() throws Exception {

        try (Reader reader = Files.newBufferedReader(Paths.get("./data/HomeC.csv"));
             CSVReader csvReader = new CSVReader(reader)) {
            String[] record;
            System.out.println(Arrays.toString(csvReader.readNext()));
            HomeIotEntity homeIotEntity = CsvUtil.convertCsvLineToObject(HomeIotEntity.class, csvReader.readNext());
            System.out.println(new Gson().toJson(homeIotEntity));
        } catch (IOException | CsvValidationException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testGetObjects() throws Exception {
        List<HomeIotEntity> homeIotEntities = CsvUtil.convertCsvFileToObjects(HomeIotEntity.class, "./data/HomeC.csv");
        System.out.println(homeIotEntities.size());
    }
}
