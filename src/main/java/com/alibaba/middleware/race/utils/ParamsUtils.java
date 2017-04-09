package com.alibaba.middleware.race.utils;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.middleware.race.LimitKNException;
import com.alibaba.middleware.race.bean.Constants;

/**
 * 处理参数
 *
 * @author xionghui
 * @since 1.0.0
 */
public class ParamsUtils {
  private static final Logger LOGGER = LogManager.getLogger(ParamsUtils.class);

  /**
   * 检查参数是否合格并处理，初始化文件夹
   */
  public static void initParams(String[] args) {
    LOGGER.debug("initParams begin");
    // 如果没有传递2个参数则提示出错
    if (args.length != 2) {
      throw new IllegalArgumentException("Please give the right paramaters 'k' and 'n'.");
    }
    long k = Long.valueOf(args[0]);
    // 0 <= k
    if (k < 0) {
      throw new IllegalArgumentException("Please give the right paramaters 'k'.");
    }
    // 0 < n < 100
    int n = Integer.valueOf(args[1]);
    if (n <= 0 || n >= 100) {
      throw new IllegalArgumentException("Please give the right paramaters 'n'.");
    }

    // 初始化中间文件夹：需要考虑文件夹无效的情况
    {
      File dirFile = new File(Constants.MIDDLE_DIR);
      File parenFile = dirFile;
      do {
        if (parenFile.exists()) {
          if (parenFile.isDirectory()) {
            break;
          }
          if (!parenFile.delete()) {
            throw new LimitKNException("can't delete " + parenFile.getAbsolutePath());
          }
        }
        parenFile = parenFile.getParentFile();
      } while (parenFile != null);

      if (!dirFile.exists()) {
        dirFile.mkdirs();
      }

      // 元数据文件不能为文件夹
      File statusFile = new File(Constants.MIDDLE_DIR + Constants.METADATA_FILE);
      if (statusFile.exists() && statusFile.isDirectory()) {
        DirectoryUtils.deleteDirectory(statusFile, true);
      }
    }

    // 初始化结果文件：需要考虑文件无效的情况
    {
      File resultFile = new File(Constants.RESULT_DIR + Constants.RESULT_NAME);
      if (resultFile.exists()) {
        // 防止创建无效目录
        if (resultFile.isDirectory()) {
          DirectoryUtils.deleteDirectory(resultFile, true);
        } else {
          // 删除上次创建的文件
          resultFile.delete();
        }
      } else {
        File dirFile = resultFile.getParentFile();
        if (!dirFile.exists()) {
          dirFile.mkdirs();
        }
      }
      try {
        resultFile.createNewFile();
      } catch (IOException e) {
        throw new LimitKNException(e);
      }
    }
    LOGGER.debug("initParams end");
  }
}
