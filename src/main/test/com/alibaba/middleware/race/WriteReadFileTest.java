package com.alibaba.middleware.race;

import java.io.File;
import java.io.RandomAccessFile;

import com.alibaba.middleware.race.utils.LongBytesUtils;

/**
 * 写文件性能测试 <br />
 *
 * 单线程写100M耗时600ms~800ms <br />
 * 单线程写10M耗时90ms左右 <br />
 * 单线程写1M耗时30ms左右 <br />
 *
 * 单线程读1M耗1ms左右 <br />
 *
 * @author xionghui
 * @since 1.0.0
 */
public class WriteReadFileTest {
  private static final int M100 = 100 * 1024 * 1024 / 8;
  private static final int M1 = 1024 * 1024 / 8;

  public static void main(String... args) throws Exception {
    File file = new File("/tmp/1");
    if (file.exists()) {
      file.delete();
    }
    writeFileTest(file);
    readFileTest(file);
  }

  private static void writeFileTest(File file) throws Exception {
    System.out.println("writeFileTest: " + file.length());
    long bg = System.currentTimeMillis();
    long[] array = createArray();
    RandomAccessFile rf = new RandomAccessFile(file, "rw");
    try {
      int count = 1024;
      byte[] bs = new byte[count << 3];
      int i = 0;
      byte[] hb = new byte[8];
      for (int p = 0, len = array.length; p < len; p++) {
        long data = array[p];
        LongBytesUtils.longToBytes(data, hb);
        int position = i << 3;
        for (int q = 0; q < 8; q++) {
          bs[position + q] = hb[q];
        }
        if (++i == count) {
          rf.write(bs);
          i = 0;
        }
      }
    } finally {
      rf.close();
    }
    System.out.println(System.currentTimeMillis() - bg);
    System.out.println();
  }

  private static void readFileTest(File file) throws Exception {
    System.out.println("readFileTest: " + file.length());
    long bg = System.currentTimeMillis();
    RandomAccessFile rf = new RandomAccessFile(file, "rw");
    try {
      rf.seek(0);
      byte[] bs = new byte[M1 << 3];
      rf.read(bs);
    } finally {
      rf.close();
    }
    System.out.println(System.currentTimeMillis() - bg);
    System.out.println();
  }

  private static long[] createArray() {
    long[] array = new long[M100];
    for (int i = 0; i < M100; i++) {
      array[i] = i;
    }
    return array;
  }
}
