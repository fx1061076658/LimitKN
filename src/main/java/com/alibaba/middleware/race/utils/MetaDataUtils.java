package com.alibaba.middleware.race.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.middleware.race.LimitKNException;
import com.alibaba.middleware.race.bean.Constants;
import com.alibaba.middleware.race.bean.FileDescBean;

/**
 * 元数据工具类(分割符号都已经去掉了) <br />
 *
 * demo: 5,0,2,0,100,10,100,200,10... <br />
 * 列表个数,文件名,列表长度,位置,值,长度,位置,值,长度...
 *
 * @author xionghui
 * @since 1.0.0
 */
public class MetaDataUtils {
  private static final Logger LOGGER = LogManager.getLogger(MetaDataUtils.class);

  // 每次读取文件大小
  private final static int READ_BUFFER_SIZE = 8192;
  // 每次写文件大小
  private final static int WRITE_BUFFER_SIZE = 8192;

  /**
   * 尝试读取元数据
   */
  public static List<List<FileDescBean>> tryReadMetadata(File metadataFile) throws Exception {
    LOGGER.debug("tryReadMetadata begin");
    List<List<FileDescBean>> resultList = new LinkedList<>();
    RandomAccessFile rf = new RandomAccessFile(metadataFile, "rw");
    try {
      long dataSize = metadataFile.length();
      long[] dataArray = new long[(int) (dataSize >> 3)];
      int pos = 0;
      byte[] tmp = new byte[8];

      int count = READ_BUFFER_SIZE;
      byte[] by = new byte[READ_BUFFER_SIZE];
      while (dataSize > 0L) {
        count = dataSize < count ? (int) dataSize : count;
        if (count != READ_BUFFER_SIZE) {
          by = new byte[count];
        }
        rf.read(by);
        dataSize -= count;

        for (int i = 0, len = by.length; i < len; i++) {
          int index = i & 7;
          tmp[index] = by[i];
          if (index == 7) {
            long data = LongBytesUtils.bytesToLong(tmp);
            dataArray[pos++] = data;
          }
        }
      }

      LOGGER.debug("tryReadMetadata middle");
      // 第一个数字为总长度
      int totalSize = (int) dataArray[0];
      int index = 1;
      String filePath = null;
      List<FileDescBean> beanList = null;
      StringBuilder sb = new StringBuilder();
      for (int i = 1, len = dataArray.length; i < len; i++) {
        long data = dataArray[i];
        if (index == 1) {
          index++;
          // 第一个是文件名
          int fileName = (int) data;
          sb.append(Constants.MIDDLE_DIR).append("/").append(fileName);
          filePath = sb.toString();
          sb.setLength(0);

          beanList = new LinkedList<>();
          resultList.add(beanList);

          continue;
        }
        // 第二个是列表长度
        int beanListSize = (int) data;
        for (int j = 0; j < beanListSize; j++) {
          long position = dataArray[++i];
          long value = dataArray[++i];
          int size = (int) dataArray[++i];
          FileDescBean bean = new FileDescBean(position, value, size);
          bean.setFilePath(filePath);
          beanList.add(bean);
        }
        index = 1;
      }
      // 数据不合法
      if (totalSize != resultList.size()) {
        LOGGER.error("tryReadMetadata　data illegal: {}, {}", totalSize, resultList.size());
        resultList.clear();
      }
      for (List<FileDescBean> list : resultList) {
        // 保证list不是null且长度不为0
        list.get(0);
      }
    } catch (Throwable t) {
      LOGGER.error("tryReadMetadata error: ", t);
      // 清楚无效数据
      resultList.clear();
    } finally {
      rf.close();
    }
    LOGGER.debug("tryReadMetadata end");
    return resultList;
  }

  /**
   * 尝试写元数据
   */
  public static List<List<FileDescBean>> tryWriteMetadata(File metadataFile,
      Map<Integer, List<FileDescBean>> beanMap) throws Exception {
    LOGGER.debug("tryWriteMetadata begin");
    List<List<FileDescBean>> resultList = new LinkedList<>();
    RandomAccessFile rf = new RandomAccessFile(metadataFile, "rw");
    try {
      final int bufferSize = WRITE_BUFFER_SIZE >> 3;
      byte[] hb = new byte[8];
      byte[] by = new byte[WRITE_BUFFER_SIZE];
      int totalSize = beanMap.size();
      int pos = 0;
      // 写入总长度
      pos = writeLong(totalSize, pos, hb, by, bufferSize, rf);
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<Integer, List<FileDescBean>> entry : beanMap.entrySet()) {
        int key = entry.getKey();
        // 写入文件名
        pos = writeLong(key, pos, hb, by, bufferSize, rf);
        List<FileDescBean> beanList = entry.getValue();
        // 保存数据
        resultList.add(beanList);
        int beanListSize = beanList.size();
        // 写入每个文件的beanList长度
        pos = writeLong(beanListSize, pos, hb, by, bufferSize, rf);
        sb.append(Constants.MIDDLE_DIR).append("/").append(key);
        String filePath = sb.toString();
        sb.setLength(0);
        for (FileDescBean bean : beanList) {
          // 保存路径
          bean.setFilePath(filePath);

          long position = bean.getPosition();
          pos = writeLong(position, pos, hb, by, bufferSize, rf);
          long value = bean.getValue();
          pos = writeLong(value, pos, hb, by, bufferSize, rf);
          int size = bean.getSize();
          pos = writeLong(size, pos, hb, by, bufferSize, rf);
        }
      }

      // 写余数
      if (pos != 0) {
        int len = pos << 3;
        // 去掉by前一批数据的缓存
        byte[] sby = new byte[len];
        for (int j = 0; j < len; j++) {
          sby[j] = by[j];
        }
        rf.write(sby);
      }
    } catch (Exception e) {
      throw new LimitKNException(e);
    } finally {
      rf.close();
    }
    LOGGER.debug("tryWriteMetadata end");
    return resultList;
  }

  /**
   * 有的value(比如size)是int型的，但是因为数量不多，统一用long记录 <br />
   * 不会浪费很多空间，但是处理起来会方便很多
   */
  private static int writeLong(long value, int pos, byte[] hb, byte[] by, int bufferSize,
      RandomAccessFile rf) throws IOException {
    LongBytesUtils.longToBytes(value, hb);
    int offset = pos << 3;
    for (int i = 0; i < 8; i++) {
      by[offset + i] = hb[i];
    }
    pos++;
    if (pos == bufferSize) {
      // 写文件
      rf.write(by);
      pos = 0;
    }
    return pos;
  }
}
