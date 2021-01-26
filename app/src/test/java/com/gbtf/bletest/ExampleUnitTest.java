package com.gbtf.bletest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void testBuild() {
        StringBuilder builder = new StringBuilder();
        System.out.println("===== before" + builder.toString());

        builder.append("loglogklllllll");
        builder.append("\n");
        builder.append("loglogklllllll");
        System.out.println("=====after" + builder.toString());
    }

}