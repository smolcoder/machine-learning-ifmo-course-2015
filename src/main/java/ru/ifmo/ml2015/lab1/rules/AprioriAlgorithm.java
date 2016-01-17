package ru.ifmo.ml2015.lab1.rules;

import javafx.util.Pair;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AprioriAlgorithm {
  private final String outputFileName = "apriori_result.txt";

  private double minConf;
  private double minSupport;
  private int numOfItems;

  private SupermarketDataset dataset;

  private List<List<Integer>> currItemSets;
  private ArrayList<Integer> supportList;

  private List<Pair<RulePair, Double>> assRules = new ArrayList<>();
  private Map<List<Integer>, Integer> allSupports = new HashMap<>();

  public AprioriAlgorithm(SupermarketDataset dataset, int minSupport, double minConf) throws IOException {
    this.minConf = minConf;
    this.minSupport = minSupport;
    this.dataset = dataset;
    this.numOfItems = dataset.getProductName2IdMap().size();

    Files.deleteIfExists(Paths.get(outputFileName));
    Files.createFile(Paths.get(outputFileName));
    log("min support = " + minSupport + "; min confidence = " + minConf + "\n");
  }

  public void buildAssociationRules() {
    buildInitialItemSets();
    while (currItemSets.size() > 0) {
      printSets();
      List<List<Integer>> candidates = aprioriGen();
      pruneCandidates(candidates);
    }
    buildRules();
    logRules();
  }

  private void logRules() {
    StringBuilder builder = new StringBuilder();
    builder = builder.append("\n@ASSOCIATION RULES\n");
    builder = builder.append("count = ").append(assRules.size()).append("\n");
    assRules.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
    DecimalFormat myFormatter = new DecimalFormat("#.#");
    for (Pair<RulePair, Double> assRule : assRules) {
      builder = builder.append("confidence = ").append(myFormatter.format(assRule.getValue())).append("%: ")
        .append(mapProductIDsToFormattedString(assRule.getKey().getX())).append(" => ").
          append(mapProductIDsToFormattedString(assRule.getKey().getY())).append("\n");

    }
    log(builder.toString());
  }

  public List<List<Integer>> subsetsAll(List<Integer> list) {
    List<List<Integer>> allSubSets = new ArrayList<>();

    int allMasks = (1 << list.size());
    for (int i = 1; i < allMasks; i++) {
      List<Integer> subset = new ArrayList<>();
      for (int j = 0; j < list.size(); j++)
        if ((i & (1 << j)) > 0) {  // The j-th element is used
          subset.add(list.get(j));
        }
      if (subset.size() < list.size()) {
        allSubSets.add(subset);
      }
    }
    return allSubSets;
  }

  private void buildRules() {
    for (Map.Entry<List<Integer>, Integer> entry : allSupports.entrySet()) {
      List<Integer> itemSet = entry.getKey();
      int supportOfBoth = entry.getValue();
      List<List<Integer>> allSubSets = subsetsAll(itemSet);
      for (List<Integer> subset : allSubSets) {
        List<Integer> supplement = new ArrayList<>(itemSet);
        supplement.removeAll(subset);
        double confidence = 100. * supportOfBoth / allSupports.get(subset);
        if (confidence >= minConf) {
          assRules.add(new Pair<>(new RulePair(subset, supplement), confidence));
        }
      }
    }
  }

  /**
   * Filter item sets leaving only that ones which have required support
   */
  private void pruneCandidates(List<List<Integer>> candidates) {
    currItemSets.clear();
    supportList.clear();
    for (List<Integer> candidate : candidates) {
      int support = calculateSupport(candidate);
      if (support >= minSupport) {
        currItemSets.add(candidate);
        supportList.add(support);
        allSupports.put(candidate, support);
      }
    }
  }

  private List<List<Integer>> aprioriGen() {
    int prevK = currItemSets.get(0).size();

    List<List<Integer>> Ck = new ArrayList<>();

    // For pair of item sets
    for (int i = 0; i < currItemSets.size(); ++i) {
      List<Integer> ithSet = currItemSets.get(i);
      for (List<Integer> otherSet : currItemSets) {
        int index;  // index of the first item in which ith and other differ
        for (index = 0; index < prevK - 1; ++index) {
          if (!Objects.equals(ithSet.get(index), otherSet.get(index))) {
            break;
          }
        }
        if (index == prevK - 1 && ithSet.get(index) < otherSet.get(index)) {
          List<Integer> c = new ArrayList<>(ithSet);
          c.add(otherSet.get(prevK - 1));
          boolean goodCandidate = true;

          for (List<Integer> sbSet : subsetsOfSize(c, prevK)) {
            if (!allSupports.containsKey(sbSet)) {
              goodCandidate = false;
              break;
            }
          }
          if (goodCandidate) {
            Ck.add(c);
          }
        }
      }
    }
    return Ck;
  }

  private void recursiveSubsets(List<List<Integer>> sets, int s[], int k, int t[], int q, int r) {
    if (q == k) {
      int ss[] = new int[k];
      System.arraycopy(t, 0, ss, 0, k);
      sets.add(Arrays.asList(ArrayUtils.toObject(ss)));
    } else {
      for (int i = r; i < s.length; ++i) {
        t[q] = s[i];
        recursiveSubsets(sets, s, k, t, q + 1, i + 1);
      }
    }
  }

  /**
   * Generate subsets of the list with size k.
   */
  public List<List<Integer>> subsetsOfSize(List<Integer> list, int k) {
    List<List<Integer>> sets = new ArrayList<>();
    int t[] = new int[list.size()];
    int s[] = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      s[i] = list.get(i);
    }
    recursiveSubsets(sets, s, k, t, 0, 0);
    return sets;
  }

  private void buildInitialItemSets() {
    currItemSets = new ArrayList<>(numOfItems);
    supportList = new ArrayList<>(numOfItems);
    for (int itemId = 1; itemId <= numOfItems; ++itemId) {
      List<Integer> c1 = Arrays.asList(itemId);
      int support = calculateSupport(c1);
      if (support >= minSupport) {
        currItemSets.add(c1);
        supportList.add(support);
        allSupports.put(c1, support);
      }
    }
  }

  /**
   * Calculate support for set of items.
   */
  private Set<Long> calcSupportForItemsList(Set<Long> basketSuperSet, List<Integer> items) {
    if (!items.isEmpty()) {
      Set<Long> nextBasketSet = dataset.getProduct2SetOfBasketsMap().get(items.get(0));
      basketSuperSet = basketSuperSet.stream().filter(nextBasketSet::contains).collect(Collectors.toSet());
      basketSuperSet = calcSupportForItemsList(basketSuperSet, items.subList(1, items.size()));
    }
    return basketSuperSet;
  }

  /**
   * Calculate support for set of items.
   */
  private int calculateSupport(List<Integer> items) {
    Set<Long> intersectionSet = calcSupportForItemsList(
      dataset.getProduct2SetOfBasketsMap().get(items.get(0)),  // baskets with first item
      items.subList(1, items.size()));  // items without first one
    return intersectionSet.size();
  }


  //----------------------

  private void log(String message) {
    try {
      Files.write(Paths.get(outputFileName), message.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void printSets() {
    StringBuilder builder = new StringBuilder();
    builder = builder.append("k = ").append(currItemSets.get(0).size()).append("; count = ").append(currItemSets.size()).append("\n");
    for (int i = 0; i < currItemSets.size(); ++i) {
      String line = "support = " + supportList.get(i) + ": ";

      line += mapProductIDsToFormattedString(currItemSets.get(i));
      builder = builder.append(line).append("\n");
    }
    log(builder.toString());
  }

  private String mapProductIDsToFormattedString(List<Integer> list) {
    String ans = "";
    for (int k = 0; k < list.size(); ++k) {
      ans += dataset.getId2ProductNameMap().get(list.get(k)) + ((k == list.size() - 1) ? "" : ",");
    }
    return ans;
  }
}
