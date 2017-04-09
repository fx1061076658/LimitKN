package com.alibaba.middleware.race;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.middleware.race.bean.Constants;

/**
 * 构建文件内容
 *
 * @author xionghui
 * @since 1.0.0
 */
public class BuildFileTest {
  private static final Random RANDOM = new Random();

  private static final int FILE_COUNT = 10;
  private static final int SIZE = 100_000_000;
  // 每次并发写的long数量
  private static final int COUNT = 100;

  public static void main(String... numList) throws Exception {
    // singleWriteFile(FILE_COUNT, SIZE);
    multiplyWriteFile(FILE_COUNT, SIZE);
  }

  static void singleWriteFile(int fileCount, int size) throws Exception {
    System.out.println("singleWriteFile");
    long bg = System.currentTimeMillis();
    String preName = Constants.DATA_DIR + Constants.FILE_PREFIX;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fileCount; i++) {
      sb.append(preName).append(i).append(Constants.FILE_SUFFIX);
      String fileName = sb.toString();
      sb.setLength(0);
      File file = new File(fileName);
      file.delete();
      RandomAccessFile rf = new RandomAccessFile(file, "rw");
      try {
        for (int k = 0, len = size / COUNT; k < len; k++) {
          byte[] by = buildBytes(COUNT);
          rf.write(by);
        }
      } finally {
        rf.close();
      }
    }
    System.out.println(System.currentTimeMillis() - bg);
  }

  /**
   * 构建100个long的随机数字
   */
  private static byte[] buildBytes(int count) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < count; j++) {
      long r = RANDOM.nextLong();
      if (r < 0) {
        // 去掉负数
        r += Long.MAX_VALUE;
      }
      sb.append(r).append('\n');
    }
    String data = sb.toString();
    byte[] by = data.getBytes();
    return by;
  }

  private static void multiplyWriteFile(int fileCount, int size) throws Exception {
    System.out.println("multiplyWriteFile");
    long bg = System.currentTimeMillis();
    CountDownLatch writeLatch = new CountDownLatch(fileCount);
    ExecutorService writeExecutor = Executors.newFixedThreadPool(fileCount);
    String preName = Constants.DATA_DIR + Constants.FILE_PREFIX;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fileCount; i++) {
      sb.append(preName).append(i).append(Constants.FILE_SUFFIX);
      String fileName = sb.toString();
      sb.setLength(0);
      Runnable command = new WriteFileRunnable(fileName, size, writeLatch);
      writeExecutor.execute(command);
    }
    try {
      writeLatch.await();
    } catch (InterruptedException e) {
      throw new LimitKNException(e);
    }
    System.out.println(System.currentTimeMillis() - bg);
  }

  /**
   * 写文件线程
   *
   * @author xionghui
   * @since 1.0.0
   */
  private static class WriteFileRunnable implements Runnable {
    private final String fileName;
    private final int size;
    private final CountDownLatch writeLatch;

    private WriteFileRunnable(String fileName, int size, CountDownLatch writeLatch) {
      this.fileName = fileName;
      this.size = size;
      this.writeLatch = writeLatch;
    }

    @Override
    public void run() {
      try {
        File file = new File(this.fileName);
        file.delete();
        RandomAccessFile rf = new RandomAccessFile(file, "rw");
        try {
          for (int k = 0, len = this.size / COUNT; k < len; k++) {
            byte[] by = buildBytes(COUNT);
            rf.write(by);
          }
        } finally {
          rf.close();
        }
      } catch (Throwable t) {
        System.exit(1);
      } finally {
        this.writeLatch.countDown();
      }
    }
  }
}
