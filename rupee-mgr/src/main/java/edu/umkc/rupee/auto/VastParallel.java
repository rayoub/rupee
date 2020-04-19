package edu.umkc.rupee.auto;

import java.util.stream.IntStream;

public class VastParallel {

    public static int SPLIT_COUNT = 3;
    
    public static void go() {

        IntStream.range(0, SPLIT_COUNT)
            .boxed()
            .parallel()
            .forEach(splitIndex -> split(splitIndex));
    }

    public static void split(int splitIndex) {

        VastCombinedDriver driver = new VastCombinedDriver(SPLIT_COUNT, splitIndex);

        try {
            driver.setUp();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        driver.doSearchBatch();
        try {
            driver.tearDown();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
