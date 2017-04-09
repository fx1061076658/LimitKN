package com.alibaba.middleware.race.array;

import java.util.Arrays;

/**
 * 自定义long数组，为了节约内存
 *
 * @author xionghui
 * @since 1.0.0
 */
public class LongArray {
  private long[] array;

  private final int capacity;

  private int size;

  public LongArray(int capacity) {
    this.capacity = capacity;
    this.array = new long[capacity];
  }

  public void add(long l) {
    this.array[this.size++] = l;
  }

  /**
   * 截取数组
   */
  public long[] subArray() {
    long[] newArray;
    if (this.size == this.capacity) {
      newArray = this.array;
      this.array = new long[this.capacity];
    } else {
      newArray = Arrays.copyOf(this.array, this.size);
      this.array = new long[this.capacity];
    }
    this.size = 0;
    return newArray;
  }

  /**
   * 排序数组
   */
  public void sort() {
    if (this.size != this.capacity) {
      this.array = Arrays.copyOf(this.array, this.size);
    }
    Arrays.sort(this.array);
  }

  /**
   * 还原数组
   */
  public void reset() {
    if (this.size != this.capacity) {
      this.array = new long[this.capacity];
    }
    this.size = 0;
  }

  public long[] getArray() {
    return this.array;
  }

  public int size() {
    return this.size;
  }

  @Override
  public String toString() {
    return "LongArray [array=" + Arrays.toString(this.array) + ", capacity=" + this.capacity
        + ", size=" + this.size + "]";
  }
}
