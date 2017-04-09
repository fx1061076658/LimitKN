package com.alibaba.middleware.race.runnable;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.middleware.race.TopKN;
import com.alibaba.middleware.race.array.LongArray;
import com.alibaba.middleware.race.bean.Constants;

/**
 * 读取文件数据
 *
 * @author xionghui
 * @since 1.0.0
 */
public class SeperateFileRunnable implements Runnable {
  private static final Logger LOGGER = LogManager.getLogger(SeperateFileRunnable.class);

  // 文件个数，从0~9
  private final static int FILE_COUNT = 10;
  // 并发争文件名
  private final static AtomicInteger ATOMIC_COUNT = new AtomicInteger();
  // 每次读取文件大小
  private final static int BUFFER_SIZE = 8192;

  private final CountDownLatch latch;

  // 数组占用内存少
  private LongArray longArray = null;

  private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
  private long takeBegin;
  private long takeTotalTime;
  private long putBegin;
  private long putTotalTime;

  public SeperateFileRunnable(CountDownLatch latch) {
    this.latch = latch;
  }

  @Override
  public void run() {
    try {
      LongCarry carry = new LongCarry();
      byte[] by = new byte[BUFFER_SIZE];

      String preName = Constants.DATA_DIR + Constants.FILE_PREFIX;
      StringBuilder sb = new StringBuilder();
      while (true) {
        int count = ATOMIC_COUNT.getAndIncrement();
        if (count >= FILE_COUNT) {
          break;
        }
        LOGGER.debug("SeperateFileRunnable run begin: {}", count);
        sb.append(preName).append(count).append(Constants.FILE_SUFFIX);
        String name = sb.toString();
        sb.setLength(0);
        File file = new File(name);
        if (!file.exists() || !file.isFile()) {
          LOGGER.error("{} is not a illegal file", name);
          continue;
        }
        RandomAccessFile rf = new RandomAccessFile(file, "r");
        try {
          int len;
          do {
            len = rf.read(by);
            this.changeBytesToLong(carry, by, len);
          } while (len != -1);
          // 防止最后一行没有换行
          if (carry.isNew) {
            this.fillDataArray(carry);
          }
        } finally {
          rf.close();
        }

        if (SeperateFileRunnable.isDebugEnabled) {
          LOGGER.debug("SeperateFileRunnable queue take cost all: {}", this.takeTotalTime);
          this.takeTotalTime = 0L;
          LOGGER.debug("SeperateFileRunnable queue put cost all: {}", this.putTotalTime);
          this.putTotalTime = 0L;
        }
        LOGGER.debug("SeperateFileRunnable run end: {}", count);
      }

      // 其实takeDataArray不为null时size一定大于0
      if (this.longArray != null && this.longArray.size() > 0) {
        this.fillQueue(this.longArray);
      }
    } catch (Throwable t) {
      LOGGER.error("SeperateFileRunnable run error: ", t);
      System.exit(1);
    } finally {
      this.latch.countDown();
    }
  }

  /**
   * 按\n分割long
   */
  private void changeBytesToLong(LongCarry carry, byte[] by, int len) throws InterruptedException {
    for (int i = 0; i < len; i++) {
      int b = by[i];
      if (b > 47 && b < 58) {
        carry.value = ((carry.value * 10L) + (b - 48));
        carry.isNew = true;
        continue;
      }
      if (b == 10) {
        if (carry.isNew) {
          this.fillDataArray(carry);
        }
      }
    }
  }

  /**
   * 写数据
   */
  private void fillDataArray(LongCarry carry) throws InterruptedException {
    if (this.longArray == null) {
      if (SeperateFileRunnable.isDebugEnabled) {
        this.takeBegin = System.currentTimeMillis();
      }
      this.longArray = TopKN.ORIGIN_DATA_QUEUE.take();
      if (SeperateFileRunnable.isDebugEnabled) {
        long time = System.currentTimeMillis() - this.takeBegin;
        this.takeTotalTime += time;
      }
    }

    carry.isNew = false;
    long data = carry.value;
    carry.value = 0L;
    this.longArray.add(data);
    if (this.longArray.size() >= Constants.M1) {
      this.fillQueue(this.longArray);
    }
  }

  /**
   * 排序数据并提交队列
   */
  private void fillQueue(LongArray dataArray) throws InterruptedException {
    if (SeperateFileRunnable.isDebugEnabled) {
      this.putBegin = System.currentTimeMillis();
    }
    // LongArray交给DATA_QUEUE
    TopKN.DATA_QUEUE.put(dataArray);
    // 置空数组，防止并发引用和操作
    this.longArray = null;
    if (SeperateFileRunnable.isDebugEnabled) {
      long time = System.currentTimeMillis() - this.putBegin;
      this.putTotalTime += time;
    }
  }

  /**
   * 保存读取的long
   *
   * @author xionghui
   * @since 1.0.0
   */
  private class LongCarry {
    long value = 0L;
    boolean isNew = false;
  }
}
