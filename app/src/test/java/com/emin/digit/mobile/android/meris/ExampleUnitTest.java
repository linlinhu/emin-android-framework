package com.emin.digit.mobile.android.meris;

import com.emin.digit.mobile.android.meris.platform.utils.UUIDGenerator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void boolean_instanceof() throws Exception{
        Object yes = true;
        if(yes instanceof Boolean) {
            System.out.println("OK");
        } else {
            System.out.println("NOT OK");
        }
    }

    @Test
    public void null_toString() throws Exception{
        Object obj = true;
        System.out.println(obj.toString());
    }

    @Test
    public void modify_list() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        modify(list);
        System.out.println(list.toString());
    }

    private List modify(List<String> list) {
        if(list == null) return list;
        list.add("Z");
        return list;
    }

    @Test
    public void generateId() throws Exception {
//        String timestamp = IdUtil.dateTimestamp();
//        String nanoTime = IdUtil.nanoTime();
//        System.out.println("timestamp:" + timestamp + "\nnanoTime:" + nanoTime);
        long startNs = System.nanoTime();
        int count = 100;
        for(int i = 0 ; i < count; i ++) {
            String id= UUIDGenerator.genUUID();
            System.out.println("ss[" + i + "]:" + id);
        }
        long coastMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        System.out.println("Cost time:" + coastMs + "ms");
    }


    public enum ViewType {
        INIT,
        CONTENT,
        AD
    }

    @Test
    public void test_enum() throws Exception {
        ViewType type = ViewType.AD;
        handleEnum(type);
    }

    private void handleEnum(ViewType t) {
        System.out.println("enum value:" + t.ordinal());
        System.out.println("enum value:" + t.toString());
        System.out.println("enum value:" + t.name());

        System.out.println("enum ViewType.INIT.ordinal:" + ViewType.INIT.ordinal());

        switch (t) {
            case INIT:
                System.out.println("enum ViewType INIT");
                break;
            case CONTENT:
                System.out.println("enum ViewType CONTENT");
                break;
            case AD:
                System.out.println("enum ViewType AD");
                break;
        }
    }
}