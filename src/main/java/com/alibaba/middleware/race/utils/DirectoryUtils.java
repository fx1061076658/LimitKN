package com.alibaba.middleware.race.utils;

import java.io.File;

import com.alibaba.middleware.race.LimitKNException;

/**
 * 文件夹处理工具类
 *
 * @author xionghui
 * @since 1.0.0
 */
public class DirectoryUtils {

  /**
   * 递归删除目录 <br />
   * containSelf标识是否删除当前目录
   */
  public static void deleteDirectory(File dirFile, boolean containSelf) {
    // 删除文件夹中的所有文件包括子目录
    File[] files = dirFile.listFiles();
    for (int i = 0; i < files.length; i++) {
      // 删除子文件
      if (files[i].isFile()) {
        if (!files[i].delete()) {
          throw new LimitKNException("can't delete " + files[i].getAbsolutePath());
        }
      }
      // 删除子目录
      else if (files[i].isDirectory()) {
        deleteDirectory(files[i], true);
      }
    }
    if (containSelf) {
      // 删除当前目录
      if (!dirFile.delete()) {
        throw new LimitKNException("can't delete " + dirFile.getAbsolutePath());
      }
    }
  }
}
