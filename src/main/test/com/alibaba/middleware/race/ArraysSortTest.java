package com.alibaba.middleware.race;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试排序性能 <br />
 *
 * 单线程排序8M 10次需要1800ms左右 <br />
 * 10个线程同时排序8M需要600ms左右 <br />
 *
 * @author xionghui
 * @since 1.0.0
 */
public class ArraysSortTest {
  private static final Random RANDOM = new Random();

  private static final int M1 = 1024 * 1024;
  private static final int TOTAL = 10;

  public static void main(String... args) {
    long[][] arrays = createArrays();
    singleThreadSort(arrays);
    // help gc
    arrays = null;
    Runtime.getRuntime().gc();
    arrays = createArrays();
    multifyThreadSort(arrays);
  }

  private static long[][] createArrays() {
    long[][] arrays = new long[TOTAL][M1];
    for (int i = 0; i < TOTAL; i++) {
      long[] array = new long[M1];
      arrays[i] = array;
      for (int j = 0; j < M1; j++) {
        long r = RANDOM.nextLong();
        if (r < 0) {
          // 去掉负数
          r += Long.MAX_VALUE;
        }
        array[j] = r;
      }
    }
    return arrays;
  }

  private static void singleThreadSort(long[][] arrays) {
    System.out.println("singleThreadSort");
    long bg = System.currentTimeMillis();
    for (long[] array : arrays) {
      Arrays.sort(array);
    }
    long end = System.currentTimeMillis();
    System.out.println(end - bg);
    System.out.println();
  }

  private static void multifyThreadSort(long[][] arrays) {
    System.out.println("multifyThreadSort, thread: " + arrays.length);
    long bg = System.currentTimeMillis();
    CountDownLatch latch = new CountDownLatch(arrays.length);
    ExecutorService executor = Executors.newFixedThreadPool(arrays.length);
    for (long[] array : arrays) {
      Runnable command = new ArraySortRunnable(array, latch);
      executor.execute(command);
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new LimitKNException(e);
    }
    executor.shutdownNow();
    long end = System.currentTimeMillis();
    System.out.println(end - bg);
    System.out.println();
  }

  /**
   * Array排序
   *
   * @author xionghui
   * @since 1.0.0
   */
  private static class ArraySortRunnable implements Runnable {
    private final long[] array;
    private final CountDownLatch latch;

    private ArraySortRunnable(long[] array, CountDownLatch latch) {
      this.array = array;
      this.latch = latch;
    }

    @Override
    public void run() {
      Arrays.sort(this.array);
      this.latch.countDown();
    }
  }
}
