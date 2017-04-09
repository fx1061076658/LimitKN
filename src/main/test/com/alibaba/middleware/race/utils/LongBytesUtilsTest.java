package com.alibaba.middleware.race.utils;

/**
 * long, bytes转换测试
 *
 * @author xionghui
 * @since 1.0.0
 */
public class LongBytesUtilsTest {

  public static void main(String... args) {
    long[] array = new long[] {Long.MIN_VALUE, 0L, 1L, Long.MAX_VALUE};
    byte[] hb = new byte[8];
    for (long a : array) {
      LongBytesUtils.longToBytes(a, hb);
      System.out.print("bytes=");
      for (byte b : hb) {
        System.out.print(b + " ");
      }
      System.out.println();

      long long2 = LongBytesUtils.bytesToLong(hb);
      System.out.println("long=" + long2);
      System.out.println(a == long2);

      System.out.println();
    }
  }
}
