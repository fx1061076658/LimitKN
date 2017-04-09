package com.alibaba.middleware.race;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.middleware.race.array.LongArray;

/**
 * 内存占用测试<br />
 *
 * result: 数组占用内存最少
 *
 * @author xionghui
 * @since 1.0.0
 */
public class MemoryTest {
  private static final int M1 = 1024 * 1024;
  private static final int M100 = 100 * 1024 * 1024 / 8;

  static List<Long> linkedList = null;
  static List<Long> arrayList = null;
  static LongArray longArray = null;
  static long[] array = null;

  int index;

  public static void main(String... args) {
    printMemory();
    printLinkedList();
    printArrayList();
    printLongArray();
    printArray();
  }

  private static void printLinkedList() {
    linkedList = new LinkedList<>();
    for (int i = 0; i < M100; i++) {
      long data = i;
      linkedList.add(data);
    }
    System.out.println("linkedList");
    printMemory();
    linkedList = null;
  }

  private static void printArrayList() {
    arrayList = new ArrayList<>(M100);
    for (int i = 0; i < M100; i++) {
      long data = i;
      arrayList.add(data);
    }
    System.out.println("arrayList");
    printMemory();
    arrayList = null;
  }

  private static void printLongArray() {
    longArray = new LongArray(M100);
    for (int i = 0; i < M100; i++) {
      long data = i;
      longArray.add(data);
    }
    System.out.println("longArray");
    printMemory();
    longArray = null;
  }

  private static void printArray() {
    array = new long[M100];
    for (int i = 0; i < M100; i++) {
      long data = i;
      array[i] = data;
    }
    System.out.println("array");
    printMemory();
    array = null;
  }

  private static void printMemory() {
    System.gc();
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    System.out.println("totalMemory(M): " + totalMemory / M1);
    System.out.println(" freeMemory(M): " + freeMemory / M1);
    System.out.println(" usedMemory(M): " + (totalMemory - freeMemory) / M1);
    System.out.println();
  }
}
