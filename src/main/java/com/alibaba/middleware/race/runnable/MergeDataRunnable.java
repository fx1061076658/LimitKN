package com.alibaba.middleware.race.runnable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.middleware.race.TopKN;
import com.alibaba.middleware.race.array.LongArray;
import com.alibaba.middleware.race.bean.Constants;
import com.alibaba.middleware.race.bean.FileDescBean;
import com.alibaba.middleware.race.utils.LongBytesUtils;

/**
 * 排序并写文件
 *
 * @author xionghui
 * @since 1.0.0
 */
public class MergeDataRunnable implements Runnable {
  private static final Logger LOGGER = LogManager.getLogger(MergeDataRunnable.class);

  // 中间文件名
  private static final AtomicInteger MIDDLE_FILE_NUM = new AtomicInteger();
  // 每次写入文件大小
  private final static int BUFFER_SIZE = 8192;

  private final CountDownLatch latch;

  // 缓存节约gc时间
  private final long[] originArray = new long[BUFFER_SIZE >> 3];
  private final byte[] originHb = new byte[8];
  private final byte[] originBy = new byte[BUFFER_SIZE];

  private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
  private static final int getTotal = 100;
  private int getCount;
  private long queueGetBegin;
  private long queueGetTotalTime;

  private static final int putTotal = 100;
  private int putCount;
  private long queuePutBegin;
  private long queuePutTotalTime;

  public MergeDataRunnable(CountDownLatch latch) {
    this.latch = latch;
  }

  @Override
  public void run() {
    try {
      LOGGER.debug("MergeDataRunnable begin");
      while (true) {
        if (isDebugEnabled) {
          this.queueGetBegin = System.currentTimeMillis();
        }
        LongArray longArray = TopKN.SORT_DATA_QUEUE.take();
        if (isDebugEnabled) {
          long time = System.currentTimeMillis() - this.queueGetBegin;
          this.queueGetTotalTime += time;
          if (++this.getCount == getTotal) {
            LOGGER.debug("MergeDataRunnable queue get cost: {}, {}", this.getCount,
                this.queueGetTotalTime);
            this.getCount = 0;
            this.queueGetTotalTime = 0;
          }
        }

        if (longArray.size() == 0) {
          break;
        }

        this.dealData(longArray);

        if (isDebugEnabled) {
          this.queuePutBegin = System.currentTimeMillis();
        }
        longArray.reset();
        // 使用完还给ORIGIN_DATA_QUEUE
        TopKN.ORIGIN_DATA_QUEUE.put(longArray);
        if (isDebugEnabled) {
          long time = System.currentTimeMillis() - this.queuePutBegin;
          this.queuePutTotalTime += time;
          if (++this.putCount == putTotal) {
            LOGGER.debug("MergeDataRunnable queue put cost: {}, {}", this.putCount,
                this.queuePutTotalTime);
            this.putCount = 0;
            this.queuePutTotalTime = 0;
          }
        }
      }

      LOGGER.debug("MergeDataRunnable end");
    } catch (Throwable t) {
      LOGGER.error("MergeDataRunnable run error: ", t);
      System.exit(1);
    } finally {
      this.latch.countDown();
    }
  }

  /**
   * 写文件
   */
  private void dealData(LongArray longArray) throws IOException {
    List<FileDescBean> metadataList = new LinkedList<>();

    // 文件名
    int num = MIDDLE_FILE_NUM.getAndIncrement();
    String filePath = Constants.MIDDLE_DIR + "/" + num;
    File file = new File(filePath);
    RandomAccessFile rf = new RandomAccessFile(file, "rw");
    try {
      long position = 0L;
      int size = BUFFER_SIZE >> 3;
      long[] array = this.originArray;
      byte[] hb = this.originHb;
      byte[] by = this.originBy;

      long[] dataArray = longArray.getArray();
      int total = dataArray.length;
      int index = 0;
      while (total > 0) {
        if (size > total) {
          size = total;
          array = new long[size];
          by = new byte[size << 3];
        }
        for (int i = 0; i < size; i++) {
          long value = dataArray[index++];
          array[i] = value;
        }

        int pos = 0;
        for (long data : array) {
          LongBytesUtils.longToBytes(data, hb);
          int offset = pos << 3;
          for (int j = 0; j < 8; j++) {
            by[offset + j] = hb[j];
          }
          pos++;
        }
        // 写文件
        rf.write(by);

        // 记录元数据
        FileDescBean metadata = new FileDescBean(position, array[size - 1], size);
        metadataList.add(metadata);

        position += size;
        total -= size;
      }
    } finally {
      rf.close();
    }

    // 保存元数据
    TopKN.METADATA_MAP.put(num, metadataList);
  }
}
