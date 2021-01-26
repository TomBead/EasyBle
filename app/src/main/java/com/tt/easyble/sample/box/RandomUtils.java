package com.tt.easyble.sample.box;

import java.util.Random;

public class RandomUtils {


    public static String getNNum(int num) {
        Random r = new Random();
        String data = "";
        for (int i = 0; i < num; i++) {
            int ran1 = r.nextInt(10);
            data += ran1;
        }
        return data;
    }

}
