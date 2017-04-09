package com.alibaba.middleware.race.bean;

/**
 * 带下标的列表
 *
 * @author xionghui
 * @since 1.0.0
 */
public class DataIndexArrayBean {
  private long[] dataArray;
  private int index;
  private int next;

  public DataIndexArrayBean(long[] dataArray) {
    this.dataArray = dataArray;
  }

  public long[] getDataArray() {
    return this.dataArray;
  }

  public void setDataArray(long[] dataArray) {
    this.dataArray = dataArray;
  }

  public int getIndex() {
    return this.index;
  }

  public void setIndex(int pre) {
    this.index = pre;
  }

  public int getNext() {
    return this.next;
  }

  public void setNext(int next) {
    this.next = next;
  }

  @Override
  public String toString() {
    return "DataIndexListBean [dataArray=" + this.dataArray + ", index=" + this.index + ", next="
        + this.next + "]";
  }
}
