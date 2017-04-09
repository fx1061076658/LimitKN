package com.alibaba.middleware.race.utils;

/**
 * long, bytes转换工具类
 *
 * @author xionghui
 * @since 1.0.0
 */
public class LongBytesUtils {

  /**
   * long转为bytes <br />
   *
   * hb的长度必须为8 <br />
   * 需要注意并发问题
   */
  public static void longToBytes(long x, byte[] hb) {
    if (hb.length != 8) {
      throw new IllegalArgumentException("hb.length is not 8 but " + hb.length);
    }
    hb[0] = long7(x);
    hb[1] = long6(x);
    hb[2] = long5(x);
    hb[3] = long4(x);
    hb[4] = long3(x);
    hb[5] = long2(x);
    hb[6] = long1(x);
    hb[7] = long0(x);
  }

  /**
   * bytes转为long
   */
  public static long bytesToLong(byte[] bytes) {
    return makeLong(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
  }

  private static byte long7(long x) {
    return (byte) (x >> 56);
  }

  private static byte long6(long x) {
    return (byte) (x >> 48);
  }

  private static byte long5(long x) {
    return (byte) (x >> 40);
  }

  private static byte long4(long x) {
    return (byte) (x >> 32);
  }

  private static byte long3(long x) {
    return (byte) (x >> 24);
  }

  private static byte long2(long x) {
    return (byte) (x >> 16);
  }

  private static byte long1(long x) {
    return (byte) (x >> 8);
  }

  private static byte long0(long x) {
    return (byte) (x);
  }

  private static long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1,
      byte b0) {
    return ((((long) b7) << 56) | (((long) b6 & 0xff) << 48) | (((long) b5 & 0xff) << 40)
        | (((long) b4 & 0xff) << 32) | (((long) b3 & 0xff) << 24) | (((long) b2 & 0xff) << 16)
        | (((long) b1 & 0xff) << 8) | (((long) b0 & 0xff)));
  }
}
