package com.alibaba.middleware.race.heap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.middleware.race.bean.FileDescBean;
import com.alibaba.middleware.race.bean.NumBeanWap;

/**
 * FileDesc最小堆
 *
 * @author xionghui
 * @since 1.0.0
 */
public class FileDescBinaryHeap extends BinaryHeap {

  /**
   * add a element
   */
  public void add(List<FileDescBean> beanList) {
    // Grow backing store if necessary
    if (this.size + 1 == this.queue.length) {
      this.queue = Arrays.copyOf(this.queue, 2 * this.queue.length);
    }
    this.queue[++this.size] = new FileDescNode(beanList);
    this.fixUp(this.size);
  }

  /**
   * get the first bean of each list
   */
  public List<FileDescBean> getListFirst() {
    List<FileDescBean> firstList = new LinkedList<>();
    for (int i = 1; i <= this.size; i++) {
      List<FileDescBean> beanList = ((FileDescNode) this.queue[i]).beanList;
      FileDescBean bean = beanList.get(0);
      firstList.add(bean);
    }
    return firstList;
  }

  /**
   * remove the first element
   */
  public void removeFirst(NumBeanWap<FileDescBean> wap) {
    List<FileDescBean> beanList = ((FileDescNode) this.queue[1]).beanList;
    FileDescBean bean = beanList.remove(0);
    int num = 0;
    if (beanList.size() == 0) {
      this.queue[1] = this.queue[this.size];
      // Drop extra reference to prevent memory leak
      this.queue[this.size--] = null;
    } else {
      num = beanList.get(0).getSize();
    }
    this.fixDown(1);
    wap.setBean(bean);
    wap.setNum(num);
  }

  /**
   * This class works as a bean carrier.
   */
  private final static class FileDescNode extends Node {
    List<FileDescBean> beanList;

    FileDescNode(List<FileDescBean> beanList) {
      this.beanList = beanList;
    }

    @Override
    int compareTo(Node node) {
      FileDescBean bean = this.beanList.get(0);
      long x = bean.getValue();

      FileDescNode ano = (FileDescNode) node;
      FileDescBean anoBean = ano.beanList.get(0);
      long y = anoBean.getValue();

      return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
  }
}
