package com.alibaba.middleware.race;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.middleware.race.array.LongArray;
import com.alibaba.middleware.race.bean.Constants;
import com.alibaba.middleware.race.bean.DataIndexArrayBean;
import com.alibaba.middleware.race.bean.FileDescBean;
import com.alibaba.middleware.race.bean.NumBeanWap;
import com.alibaba.middleware.race.heap.DataIndexBinaryHeap;
import com.alibaba.middleware.race.heap.FileDescBinaryHeap;
import com.alibaba.middleware.race.runnable.MergeDataRunnable;
import com.alibaba.middleware.race.runnable.SeperateFileRunnable;
import com.alibaba.middleware.race.runnable.SortDataRunnable;
import com.alibaba.middleware.race.utils.DirectoryUtils;
import com.alibaba.middleware.race.utils.LongBytesUtils;
import com.alibaba.middleware.race.utils.MetaDataUtils;
import com.alibaba.middleware.race.utils.ParamsUtils;

/**
 * https://code.aliyun.com/middlewarerace2017/LimitKNDemo
 *
 * @author xionghui
 * @since 1.0.0
 */
public class TopKN implements KNLimit {
  private static final Logger LOGGER = LogManager.getLogger(TopKN.class);

  private static final Lock LOCK = new ReentrantLock();
  // 存储最终数据的map
  private static final Map<String, long[]> DATA_MAP = new HashMap<>();

  // 原始的long数组,可以限制long数组占有内存大小
  public static BlockingQueue<LongArray> ORIGIN_DATA_QUEUE = new LinkedBlockingQueue<>();
  // long数组
  public static BlockingQueue<LongArray> DATA_QUEUE = new LinkedBlockingQueue<>();
  // 排好序的long数组
  public static BlockingQueue<LongArray> SORT_DATA_QUEUE = new LinkedBlockingQueue<>();

  // 元数据
  public static final Map<Integer, List<FileDescBean>> METADATA_MAP = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    LOGGER.debug("TopKN begin");
    long begin = System.currentTimeMillis();
    try {
      ParamsUtils.initParams(args);

      TopKN topKN = new TopKN();
      long k = Long.parseLong(args[0], 10);
      int n = Integer.parseInt(args[1], 10);
      topKN.processTopKN(k, n);
    } catch (Throwable t) {
      LOGGER.error("TopKN processTopKN error: ", t);
    }
    long time = System.currentTimeMillis() - begin;
    LOGGER.info("TopKN end, cost time: {}", time);
  }

  @Override
  public void processTopKN(long k, int n) {
    try {
      // 获取元数据
      List<List<FileDescBean>> metadataList = this.getMetadata();
      if (metadataList == null || metadataList.size() == 0) {
        throw new LimitKNException("illegal metadata");
      }

      LOGGER.debug("check k n begin");
      long total = 0;
      for (List<FileDescBean> beanList : metadataList) {
        // LinkedList get最后一个元素的时间复杂度为1
        FileDescBean bean = beanList.get(beanList.size() - 1);
        long len = bean.getPosition() + bean.getSize();
        total += len;
      }
      // k超出total范围，不用计算了
      if (k >= total) {
        LOGGER.error("k >= total error: {}, {}", k, total);
        return;
      }
      if ((total - k) < n) {
        LOGGER.error("(total - k) < n error: {}, {}, {}", total, k, n);
        // 防止超出范围
        n = (int) (total - k);
      }
      LOGGER.debug("check k n end");

      // 将k降到metadataList第一个元素长度总和以下
      k = this.filterMetadata(metadataList, k);

      // 读出全部数据
      List<DataIndexArrayBean> resultList = this.readFileData(metadataList, n);
      // 获取最终数据
      String nStr = this.getFinalData(k, n, resultList);
      // help gc
      resultList = null;

      byte[] bys = nStr.getBytes();
      RandomAccessFile rf =
          new RandomAccessFile(Constants.RESULT_DIR + Constants.RESULT_NAME, "rw");
      try {
        rf.write(bys);
      } finally {
        rf.close();
      }
    } catch (Throwable t) {
      throw new LimitKNException(t);
    }
  }

  /**
   * 获取最终数据
   */
  private String getFinalData(long k, int n, List<DataIndexArrayBean> resultList) {
    LOGGER.debug("getFinalData begin");
    DataIndexBinaryHeap heap = new DataIndexBinaryHeap();
    int skip = (int) (k / resultList.size());
    if (skip == 0) {
      skip = 1;
    }
    for (DataIndexArrayBean bean : resultList) {
      int len = bean.getDataArray().length;
      bean.setNext(skip > len ? len : skip);
      heap.add(bean);
    }
    LOGGER.debug("getFinalData middle1");
    NumBeanWap<DataIndexArrayBean> dataIndexArrayBeanWap = new NumBeanWap<DataIndexArrayBean>();
    // 将k降到0
    while (k > 0L) {
      int heapSize = heap.size();
      // 当k >= (1L * skip * heapSize)时才能删除skip
      // 或者skip == 1时一个一个地删除
      while (k > 0 && heapSize == heap.size() && (skip == 1 || (k >= (1L * skip * heapSize)))) {
        heap.removeFirst(dataIndexArrayBeanWap, skip);
        int num = dataIndexArrayBeanWap.getNum();
        k -= num;
      }

      if (k == 0L) {
        break;
      }

      DataIndexBinaryHeap nextHeap = new DataIndexBinaryHeap();
      List<DataIndexArrayBean> beanList = heap.getBeanList();
      skip = (int) (k / beanList.size());
      if (skip == 0) {
        skip = 1;
      }
      heap = nextHeap;
      for (DataIndexArrayBean bean : beanList) {
        int len = bean.getDataArray().length;
        int index = bean.getIndex();
        int next = skip + index;
        bean.setNext(next > len ? len : next);
        nextHeap.add(bean);
      }
    }
    LOGGER.debug("getFinalData middle2");

    List<DataIndexArrayBean> beanList = heap.getBeanList();
    heap = new DataIndexBinaryHeap();
    for (DataIndexArrayBean bean : beanList) {
      int index = bean.getIndex();
      bean.setNext(index + 1);
      heap.add(bean);
    }
    StringBuilder sb = new StringBuilder();
    NumBeanWap<DataIndexArrayBean> dataBeanWap = new NumBeanWap<DataIndexArrayBean>();
    while (n > 0) {
      n--;
      heap.removeFirst(dataBeanWap, 1);
      DataIndexArrayBean bean = dataBeanWap.getBean();
      int index = bean.getIndex();
      long[] dataArray = bean.getDataArray();
      long data = dataArray[index - 1];
      sb.append(data).append("\n");
    }
    LOGGER.debug("getFinalData end");
    return sb.toString();
  }

  /**
   * 读取数据
   */
  private List<DataIndexArrayBean> readFileData(List<List<FileDescBean>> metadataList, int n)
      throws InterruptedException {
    LOGGER.debug("readFileData begin");
    int count = metadataList.size();
    // 控制线程数不大于1000
    count = count > 1000 ? 1000 : count;
    LOGGER.debug("readFileData thread count: {}", count);
    CountDownLatch readLatch = new CountDownLatch(count);
    ExecutorService readExecutor = Executors.newFixedThreadPool(count);
    // beanList是连续的
    for (List<FileDescBean> beanList : metadataList) {
      FileDescBean fileDescBean = beanList.get(0);
      long value = 0L;
      int size = 0;
      for (FileDescBean bean : beanList) {
        size += bean.getSize();
        value = bean.getValue();
      }
      // 多读n个数
      fileDescBean.setSize(size + n);
      fileDescBean.setValue(value);

      Runnable command = new ReadFileRunnable(fileDescBean, readLatch);
      readExecutor.execute(command);
    }
    readLatch.await();
    LOGGER.debug("readFileData read end");
    readExecutor.shutdownNow();

    List<DataIndexArrayBean> resultList = new LinkedList<>();
    LOCK.lock();
    try {
      for (long[] dataArray : DATA_MAP.values()) {
        DataIndexArrayBean bean = new DataIndexArrayBean(dataArray);
        resultList.add(bean);
      }
      // help gc
      DATA_MAP.clear();
    } finally {
      LOCK.unlock();
    }
    LOGGER.debug("readFileData end");
    return resultList;
  }

  /**
   * 过滤数据，把范围缩小到2*metadataList.size()
   */
  private long filterMetadata(List<List<FileDescBean>> metadataList, long k) {
    LOGGER.debug("filterMetadata begin");
    FileDescBinaryHeap heap = new FileDescBinaryHeap();
    // count用于防止有的队列比M1短
    long count = 0L;
    for (List<FileDescBean> beanList : metadataList) {
      count += beanList.get(0).getSize();
      heap.add(beanList);
    }
    // 后续需要用metadataList保存结果数据
    metadataList.clear();

    NumBeanWap<FileDescBean> fileDescBeanWap = new NumBeanWap<FileDescBean>();
    // 将k降到count以下
    while (k >= count) {
      heap.removeFirst(fileDescBeanWap);
      FileDescBean bean = fileDescBeanWap.getBean();
      int size = bean.getSize();
      k -= size;
      count -= size;
      count += fileDescBeanWap.getNum();
    }

    LOGGER.debug("filterMetadata middle1");

    Map<String, List<FileDescBean>> beanMap = new HashMap<>();
    long sign = k;
    NumBeanWap<FileDescBean> descBeanWap = new NumBeanWap<FileDescBean>();
    // 取出BinaryHeap最小的size个元素
    while (sign > 0 && heap.size() > 0) {
      heap.removeFirst(descBeanWap);
      FileDescBean bean = descBeanWap.getBean();
      int size = bean.getSize();
      sign -= size;
      String filePath = bean.getFilePath();
      List<FileDescBean> beanList = beanMap.get(filePath);
      if (beanList == null) {
        beanList = new LinkedList<>();
        beanMap.put(filePath, beanList);
      }
      beanList.add(bean);
    }

    LOGGER.debug("filterMetadata middle2");

    // k也有可能存在于BinaryHeap的每个列表的第一个元素
    if (heap.size() > 0) {
      List<FileDescBean> firstList = heap.getListFirst();
      for (FileDescBean bean : firstList) {
        String filePath = bean.getFilePath();
        List<FileDescBean> beanList = beanMap.get(filePath);
        if (beanList == null) {
          beanList = new LinkedList<>();
          beanMap.put(filePath, beanList);
        }
        beanList.add(bean);
      }
    }

    for (List<FileDescBean> beanList : beanMap.values()) {
      metadataList.add(beanList);
    }

    LOGGER.debug("filterMetadata end");

    return k;
  }

  /**
   * 获取元数据
   */
  private List<List<FileDescBean>> getMetadata() throws Exception {
    LOGGER.debug("getMetadata begin");
    List<List<FileDescBean>> resultList;
    File metadataFile = new File(Constants.MIDDLE_DIR + Constants.METADATA_FILE);
    if (metadataFile.exists()) {
      // 尝试读取数据
      resultList = MetaDataUtils.tryReadMetadata(metadataFile);
      if (resultList != null && resultList.size() > 0) {
        LOGGER.debug("getMetadata end　with cache");
        return resultList;
      }
    }

    File pFile = metadataFile.getParentFile();
    // 清空目录
    DirectoryUtils.deleteDirectory(pFile, false);

    // 分割文件
    this.seperateFile();

    resultList = MetaDataUtils.tryWriteMetadata(metadataFile, METADATA_MAP);
    METADATA_MAP.clear();

    LOGGER.debug("getMetadata end　without cache");

    return resultList;
  }

  /**
   * 将大文件分割为小文件
   */
  private void seperateFile() throws InterruptedException {
    LOGGER.debug("seperateFile begin");

    // 读取文件线程数,并发读会来回切换磁头, 单线程读更好
    int seperateThreadCount = 1;
    CountDownLatch seperateLatch = new CountDownLatch(seperateThreadCount);
    ExecutorService seperateExecutor = Executors.newFixedThreadPool(seperateThreadCount);
    for (int i = 0; i < seperateThreadCount; i++) {
      Runnable seperateCommand = new SeperateFileRunnable(seperateLatch);
      seperateExecutor.execute(seperateCommand);
    }

    // 排序线程数
    int sortThreadCount = 10;
    CountDownLatch sortLatch = new CountDownLatch(sortThreadCount);
    ExecutorService sortExecutor = Executors.newFixedThreadPool(sortThreadCount);
    for (int i = 0; i < sortThreadCount; i++) {
      Runnable sortCommand = new SortDataRunnable(sortLatch);
      sortExecutor.execute(sortCommand);
    }

    // 合并排序并写文件线程数
    int mergeThreadCount = 10;
    CountDownLatch mergeLatch = new CountDownLatch(mergeThreadCount);
    ExecutorService mergeExecutor = Executors.newFixedThreadPool(sortThreadCount);
    for (int i = 0; i < sortThreadCount; i++) {
      Runnable mergeCommand = new MergeDataRunnable(mergeLatch);
      mergeExecutor.execute(mergeCommand);
    }

    // 控制内存占有为1G,每个数组大小为8M
    for (int i = 0, len = (Constants.G1 / Constants.M1) >> 3; i < len; i++) {
      LongArray LongArray = new LongArray(Constants.M1);
      ORIGIN_DATA_QUEUE.put(LongArray);
    }

    // 文件拆分线程结束
    seperateLatch.await();
    LOGGER.debug("seperateLatch await end");
    seperateExecutor.shutdownNow();

    // 通知排序线程
    final LongArray empty = new LongArray(0);
    for (int i = 0; i < sortThreadCount; i++) {
      DATA_QUEUE.put(empty);
    }
    // 排序线程结束
    sortLatch.await();
    LOGGER.debug("sortLatch await end");
    sortExecutor.shutdownNow();

    // 通知合并并写文件线程
    for (int i = 0; i < mergeThreadCount; i++) {
      TopKN.SORT_DATA_QUEUE.put(empty);
    }
    // 合并并写文件线程结束
    mergeLatch.await();
    LOGGER.debug("mergeExecutor await end");
    mergeExecutor.shutdownNow();

    // help gc
    ORIGIN_DATA_QUEUE = new LinkedBlockingQueue<>();
    DATA_QUEUE = new LinkedBlockingQueue<>();
    SORT_DATA_QUEUE = new LinkedBlockingQueue<>();

    LOGGER.debug("seperateFile end");
  }

  /**
   * 读文件
   *
   * @author xionghui
   * @since 1.0.0
   */
  private static class ReadFileRunnable implements Runnable {
    private static final Logger READ_LOGGER = LogManager.getLogger(TopKN.class);

    // 每次读取文件大小
    private final static int BUFFER_SIZE = 8192;

    private final FileDescBean bean;
    private final CountDownLatch latch;

    private ReadFileRunnable(FileDescBean bean, CountDownLatch latch) {
      this.bean = bean;
      this.latch = latch;
    }

    @Override
    public void run() {
      try {
        READ_LOGGER.debug("ReadFileRunnable begin");
        String filePath = this.bean.getFilePath();
        File file = new File(filePath);
        long position = this.bean.getPosition();
        int size = this.bean.getSize();
        long total = position + size;
        long fileLen = file.length();
        fileLen >>= 3;
        if (fileLen < total) {
          size = (int) (fileLen - position);
        }
        RandomAccessFile rf = new RandomAccessFile(file, "rw");
        try {
          rf.seek(position << 3);

          long[] dataArray = new long[size];
          int pos = 0;
          byte[] tmp = new byte[8];

          int count = BUFFER_SIZE;
          long totalSize = size;
          totalSize <<= 3;
          byte[] by = new byte[BUFFER_SIZE];
          while (totalSize > 0L) {
            count = totalSize < count ? (int) totalSize : count;
            if (count != BUFFER_SIZE) {
              by = new byte[count];
            }
            rf.read(by);
            totalSize -= count;

            for (int i = 0, len = by.length; i < len; i++) {
              int index = i & 7;
              tmp[index] = by[i];
              if (index == 7) {
                long data = LongBytesUtils.bytesToLong(tmp);
                dataArray[pos++] = data;
              }
            }
          }

          LOCK.lock();
          try {
            DATA_MAP.put(filePath, dataArray);
          } finally {
            LOCK.unlock();
          }
        } finally {
          rf.close();
        }
        READ_LOGGER.debug("ReadFileRunnable end");
      } catch (Throwable t) {
        READ_LOGGER.error("ReadFileRunnable error: ", t);
        System.exit(1);
      } finally {
        this.latch.countDown();
      }
    }
  }
}
