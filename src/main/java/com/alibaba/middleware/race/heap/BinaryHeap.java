package com.alibaba.middleware.race.heap;

/**
 * 最小堆
 *
 * @author xionghui
 * @since 1.0.0
 */
public abstract class BinaryHeap {
  private static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The default initial capacity for this table, used when not otherwise specified in a
   * constructor.
   */
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  protected Node[] queue;

  protected volatile int size;

  public BinaryHeap() {
    // Find a power of 2 >= toSize
    int initialCapacity = this.roundUpToPowerOf2(DEFAULT_INITIAL_CAPACITY);
    this.queue = new Node[initialCapacity];
  }

  private int roundUpToPowerOf2(int number) {
    return number >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY
        : number > 1 ? Integer.highestOneBit(number - 1 << 1) : 1;
  }

  /**
   * Establishes the heap invariant (described above) assuming the heap satisfies the invariant
   * except possibly for the leaf-node indexed by k (which may have a element greater than its
   * parent's).
   *
   * This method functions by "promoting" queue[k] up the hierarchy (by swapping it with its parent)
   * repeatedly until queue[k]'s element is less than or equal to that of its parent.
   */
  protected void fixUp(int k) {
    while (k > 1) {
      int j = k >> 1;
      if (this.queue[j].compareTo(this.queue[k]) <= 0) {
        break;
      }
      Node tmp = this.queue[j];
      this.queue[j] = this.queue[k];
      this.queue[k] = tmp;
      k = j;
    }
  }


  /**
   * Establishes the heap invariant (described above) in the subtree rooted at k, which is assumed
   * to satisfy the heap invariant except possibly for node k itself (which may have a element less
   * than its children's).
   *
   * This method functions by "demoting" queue[k] down the hierarchy (by swapping it with its
   * smaller child) repeatedly until queue[k]'s element is greater than or equal to those of its
   * children.
   */
  protected void fixDown(int k) {
    int j;
    while ((j = k << 1) <= this.size && j > 0) {
      if (j < this.size && this.queue[j].compareTo(this.queue[j + 1]) > 0) {
        // j indexes litter kid
        j++;
      }
      if (this.queue[k].compareTo(this.queue[j]) <= 0) {
        break;
      }
      Node tmp = this.queue[j];
      this.queue[j] = this.queue[k];
      this.queue[k] = tmp;
      k = j;
    }
  }

  public int size() {
    return this.size;
  }

  /**
   * This class works as a bean carrier.
   */
  abstract static class Node {
    abstract int compareTo(Node node);
  }
}
