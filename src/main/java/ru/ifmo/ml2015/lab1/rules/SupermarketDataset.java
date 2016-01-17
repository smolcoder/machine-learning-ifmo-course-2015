package ru.ifmo.ml2015.lab1.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SupermarketDataset {

  private Map<String, Integer> productName2idMap = new HashMap<>();
  private Map<Integer, String> id2ProductNameMap = new HashMap<>();
  private Map<Long, Set<Integer>> basketMaps = new HashMap<>();
  private Map<Integer, Set<Long>> productBasketsMap = new HashMap<>();

  public Map<String, Integer> getProductName2IdMap() {
    return productName2idMap;
  }

  public Map<Long, Set<Integer>> getBasket2SetOfProductsMap() {
    return basketMaps;
  }

  public Map<Integer, Set<Long>> getProduct2SetOfBasketsMap() {
    return productBasketsMap;
  }

  public Map<Integer, String> getId2ProductNameMap() {
    return id2ProductNameMap;
  }
}
