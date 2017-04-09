package com.alibaba.middleware.race.bean;

/**
 * 包含数量的bean，用于记num
 *
 * @author xionghui
 * @since 1.0.0
 */
public class NumBeanWap<T> {
  private T bean;
  private int num;

  public T getBean() {
    return this.bean;
  }

  public void setBean(T bean) {
    this.bean = bean;
  }

  public int getNum() {
    return this.num;
  }

  public void setNum(int num) {
    this.num = num;
  }

  @Override
  public String toString() {
    return "NumBeanWap [bean=" + this.bean + ", num=" + this.num + "]";
  }
}
