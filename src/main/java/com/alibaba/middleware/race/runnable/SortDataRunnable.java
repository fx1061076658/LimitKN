package com.alibaba.middleware.race.runnable;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.middleware.race.TopKN;
import com.alibaba.middleware.race.array.LongArray;

/**
 * 排序文件
 *
 * @author xionghui
 * @since 1.0.0
 */
public class SortDataRunnable implements Runnable {
  private static final Logger LOGGER = LogManager.getLogger(SortDataRunnable.class);

  private final CountDownLatch latch;

  private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
  private static final int getTotal = 100;
  private int getCount;
  private long queueGetBegin;
  private long queueGetTotalTime;

  private static final int putTotal = 100;
  private long queuePutBegin;
  private long queuePutTotalTime;
  private int putCount;

  public SortDataRunnable(CountDownLatch latch) {
    this.latch = latch;
  }

  @Override
  public void run() {
    try {
      LOGGER.debug("SortDataRunnable begin");
      while (true) {
        if (SortDataRunnable.isDebugEnabled) {
          this.queueGetBegin = System.currentTimeMillis();
        }
        LongArray longArray = TopKN.DATA_QUEUE.take();
        if (SortDataRunnable.isDebugEnabled) {
          long time = System.currentTimeMillis() - this.queueGetBegin;
          this.queueGetTotalTime += time;
          if (++this.getCount == getTotal) {
            LOGGER.debug("SortDataRunnable queue get cost: {}, {}", this.getCount,
                this.queueGetTotalTime);
            this.getCount = 0;
            this.queueGetTotalTime = 0;
          }
        }

        if (longArray.size() == 0) {
          break;
        }

        // 排序数组
        longArray.sort();

        if (isDebugEnabled) {
          this.queuePutBegin = System.currentTimeMillis();
        }
        TopKN.SORT_DATA_QUEUE.put(longArray);
        if (SortDataRunnable.isDebugEnabled) {
          long time = System.currentTimeMillis() - this.queuePutBegin;
          this.queuePutTotalTime += time;
          if (++this.putCount == putTotal) {
            LOGGER.debug("SortDataRunnable queue put cost: {}, {}", this.putCount,
                this.queuePutTotalTime);
            this.putCount = 0;
            this.queuePutTotalTime = 0;
          }
        }
      }
      LOGGER.debug("SortDataRunnable end");
    } catch (Throwable t) {
      LOGGER.error("SortDataRunnable run error: ", t);
      System.exit(1);
    } finally {
      this.latch.countDown();
    }
  }
}
