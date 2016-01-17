package ru.ifmo.ml2015.lab1.rules;

public class Main {
  public static void main(String[] args) throws Exception {
    SupermarketDataset dataset = DataParser.parseDataSetFile(
      "/Users/smolcoder/study/ml/code/labs/data/supermarket.arff");
    AprioriAlgorithm apriori = new AprioriAlgorithm(dataset, 3, 30);
    apriori.buildAssociationRules();
  }
}
