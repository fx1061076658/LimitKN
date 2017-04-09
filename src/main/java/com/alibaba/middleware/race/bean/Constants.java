package com.alibaba.middleware.race.bean;

/**
 * 常量定义
 *
 * @author xionghui
 * @since 1.0.0
 */
public class Constants {
  public static final int M1 = 1024 * 1024;
  public static final int G1 = M1 * 1024;

  // 读取数据文件的目录
  public static final String DATA_DIR = "/home/admin/topkn-datafiles/";
  // 数据文件的命名前缀
  public static final String FILE_PREFIX = "KNLIMIT_";
  // 数据文件的命名后缀
  public static final String FILE_SUFFIX = ".data";

  // 编号
  public static final String TEAM_CODE = "61nlfe";

  // 如果需要生成中间结果文件
  public static final String MIDDLE_DIR = "/home/admin/middle/" + TEAM_CODE;
  // 元数据文件
  public static final String METADATA_FILE = "/metadata";

  // 结果文件输出目录
  public static final String RESULT_DIR = "/home/admin/topkn-resultfiles/" + TEAM_CODE + "/";
  // 结果文件的命名
  public static final String RESULT_NAME = "RESULT.rs";

}
