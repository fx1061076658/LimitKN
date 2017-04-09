package com.alibaba.middleware.race.bean;

import java.io.File;

/**
 * 分割文件bean
 *
 * @author xionghui
 * @since 1.0.0
 */
public class SeperateFileBean {

  private File file;

  private long begin;
  private long total;

  public SeperateFileBean(File file, long begin, long total) {
    this.file = file;
    this.begin = begin;
    this.total = total;
  }

  public File getFile() {
    return this.file;
  }

  public long getBegin() {
    return this.begin;
  }

  public long getTotal() {
    return this.total;
  }

  @Override
  public String toString() {
    return "SeperateFileBean [file=" + this.file + ", begin=" + this.begin + ", total=" + this.total
        + "]";
  }
}
