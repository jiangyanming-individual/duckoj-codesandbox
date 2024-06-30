package com.jiang.duckojcodesandbox.ErrorProcess;

import java.util.ArrayList;
import java.util.List;

public class MemoryError {

    public static void main(String[] args) {
        List<Byte[]> list = new ArrayList<>();
        while (true){
            list.add(new Byte[10000]);
        }
    }
}
