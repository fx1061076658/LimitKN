package com.alibaba.middleware.race.bean;

/**
 * 文件描述bean
 *
 * @author xionghui
 * @since 1.0.0
 */
public class FileDescBean {
  private String filePath;

  private long position;
  private long value;
  private int size;

  public FileDescBean(long position, long value, int size) {
    this.position = position;
    this.value = value;
    this.size = size;
  }

  public String getFilePath() {
    return this.filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public long getPosition() {
    return this.position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  public long getValue() {
    return this.value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public int getSize() {
    return this.size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public String toString() {
    return "FileDescBean [filePath=" + this.filePath + ", position=" + this.position + ", value="
        + this.value + ", size=" + this.size + "]";
  }
}
