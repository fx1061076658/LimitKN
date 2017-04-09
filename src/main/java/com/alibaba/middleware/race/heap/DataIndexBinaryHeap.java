package com.alibaba.middleware.race.heap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.middleware.race.bean.DataIndexArrayBean;
import com.alibaba.middleware.race.bean.NumBeanWap;

/**
 * DataIndex最小堆
 *
 * @author xionghui
 * @since 1.0.0
 */
public class DataIndexBinaryHeap extends BinaryHeap {

  /**
   * add a element
   */
  public void add(DataIndexArrayBean bean) {
    // Grow backing store if necessary
    if (this.size + 1 == this.queue.length) {
      this.queue = Arrays.copyOf(this.queue, 2 * this.queue.length);
    }
    this.queue[++this.size] = new DataIndexNode(bean);
    this.fixUp(this.size);
  }

  /**
   * get the bean list
   */
  public List<DataIndexArrayBean> getBeanList() {
    List<DataIndexArrayBean> beanList = new LinkedList<>();
    for (int i = 1; i <= this.size; i++) {
      DataIndexArrayBean bean = ((DataIndexNode) this.queue[i]).bean;
      beanList.add(bean);
    }
    return beanList;
  }

  /**
   * remove the first element
   */
  public void removeFirst(NumBeanWap<DataIndexArrayBean> wap, int skip) {
    DataIndexArrayBean bean = ((DataIndexNode) this.queue[1]).bean;
    long[] dataArray = bean.getDataArray();
    int index = bean.getIndex();
    int next = bean.getNext();
    int num = next - index;
    bean.setIndex(next);
    if (next >= dataArray.length) {
      this.queue[1] = this.queue[this.size];
      // Drop extra reference to prevent memory leak
      this.queue[this.size--] = null;
    } else {
      next += skip;
      if (next > dataArray.length) {
        next = dataArray.length;
      }
      bean.setNext(next);
    }
    this.fixDown(1);
    wap.setBean(bean);
    wap.setNum(num);
  }

  /**
   * This class works as a bean carrier.
   */
  private final static class DataIndexNode extends Node {
    DataIndexArrayBean bean;

    DataIndexNode(DataIndexArrayBean bean) {
      this.bean = bean;
    }

    @Override
    int compareTo(Node node) {
      long[] dataArray = this.bean.getDataArray();
      int next = this.bean.getNext();
      long x = dataArray[next - 1];

      DataIndexNode ano = (DataIndexNode) node;
      long[] anoDataArray = ano.bean.getDataArray();
      int anoNext = ano.bean.getNext();
      long y = anoDataArray[anoNext - 1];

      return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
  }
}
