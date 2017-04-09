package com.alibaba.middleware.race;

/**
 * 求(k, n)问题的接口
 *
 * @author xionghui
 * @since 1.0.0
 */
public interface KNLimit {

  public void processTopKN(long k, int n);

}
