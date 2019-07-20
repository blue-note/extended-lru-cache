import java.util.*;

public class ExtendedLRUCache {
  TreeMap<Integer, QueueElem> expiryMap;
  TreeMap<Integer, Queue<QueueElem>> priorityMap;
  Map<String, QueueElem> keyMap;
  int maxSize;

  public static void main(String[] args) {
    ExtendedLRUCache cache = new ExtendedLRUCache(4);
    System.out.println("cache size: 4");
    cache.set("A", 1, 5, 100, 0); // parameters: key, value, priority, expiration, current time
    System.out.println("set: (A, 1)");
    cache.set("B", 2, 3, 3, 1);
    System.out.println("set: (B, 2)");
    cache.set("C", 3, 2, 10, 2);
    System.out.println("set: (C, 3)");
    cache.set("D", 4, 2, 15, 3);
    System.out.println("set: (D, 4)");
    cache.set("E", 5, 5, 150, 4);
    System.out.println("set: (E, 5)");
    System.out.println("get C: " + cache.get("C"));
    try {
      System.out.println("Try to get B: " + cache.get("B"));
    } catch (IllegalArgumentException e) {
      System.out.println("Expected error while getting B: " + e.getMessage()); // B should have expired
    }

    cache.set("F", 6, 1, 10, 5);
    System.out.println("set: (F, 4)");

    try {
      System.out.println("Try to get D: " + cache.get("D"));
    } catch (IllegalArgumentException e) {
      System.out.println("Expected error while getting D: " + e.getMessage()); // D should have been evicted; LRU of
                                                                               // lowest
      // priority
    }
  }

  public ExtendedLRUCache(int size) {
    this.maxSize = size;
    this.expiryMap = new TreeMap<Integer, QueueElem>();
    this.priorityMap = new TreeMap<Integer, Queue<QueueElem>>();
    this.keyMap = new HashMap<String, QueueElem>();
  }

  public void evictItem(int currentTime) {
    // Assumption is that expiration times are unique
    QueueElem toRemove = expiryMap.firstEntry().getValue();
    Queue<QueueElem> lruCache;
    if (toRemove.expireTime <= currentTime) {
      lruCache = priorityMap.get(toRemove.priority);
    } else {
      lruCache = priorityMap.firstEntry().getValue();
      toRemove = lruCache.peek();
    }

    lruCache.remove(toRemove);
    // If cache is empty, remove this priority from the map entirely
    if (lruCache.size() == 0) {
      priorityMap.remove(toRemove.priority);
    }
    expiryMap.remove(toRemove.expireTime);
    keyMap.remove(toRemove.key);
  }

  public void set(String key, int value, int priority, int expireTime, int currentTime) {
    if (keyMap.size() >= this.maxSize) {
      evictItem(currentTime);
    }

    if (!keyMap.containsKey(key)) {
      QueueElem elem = new QueueElem(key, value, priority, expireTime);
      Queue<QueueElem> lruCache = new LinkedList<QueueElem>();
      lruCache.add(elem);
      expiryMap.put(elem.expireTime, elem);
      priorityMap.put(elem.priority, lruCache);
      keyMap.put(key, elem);
    } else {
      // Update value for existing key
      // Move element to back of queue in priority map

      QueueElem elem = keyMap.get(key);
      elem.value = value;
      updateMostRecentElement(elem);
    }
  }

  void updateMostRecentElement(QueueElem elem) {
    Queue<QueueElem> lruCache = priorityMap.get(elem.priority);
    lruCache.remove(elem);
    lruCache.add(elem);
  }

  public int get(String key) throws IllegalArgumentException {
    if (!this.keyMap.containsKey(key)) {
      throw new IllegalArgumentException("Key not found in cache.");
    }

    QueueElem elem = keyMap.get(key);
    updateMostRecentElement(elem);
    return elem.value;
  }

  public class QueueElem {

    public String key;
    public int value;
    public int priority;
    public int expireTime;

    public QueueElem(String key, int value, int priority, int expireTime) {
      this.key = key;
      this.value = value;
      this.priority = priority;
      this.expireTime = expireTime;
    }

  }

}
