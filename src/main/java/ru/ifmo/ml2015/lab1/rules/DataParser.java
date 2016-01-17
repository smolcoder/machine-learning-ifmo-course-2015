package ru.ifmo.ml2015.lab1.rules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class DataParser {

  public static SupermarketDataset parseDataSetFile(String dataSetFileName) {
    SupermarketDataset dataset = new SupermarketDataset();
    try (BufferedReader br = new BufferedReader(new FileReader(dataSetFileName))) {
      String sCurrentLine;
      while ((sCurrentLine = br.readLine()) != null) {
        if (sCurrentLine.contains("@data")) {
          break;
        }
      }
      int currentProductNumericID = 0;
      while ((sCurrentLine = br.readLine()) != null) {
        String[] splittedLineList = sCurrentLine.split(",");
        String productName = splittedLineList[0];
        int productID;
        if (!dataset.getProductName2IdMap().containsKey(productName)) {
          dataset.getProductName2IdMap().put(productName, ++currentProductNumericID);
          dataset.getId2ProductNameMap().put(currentProductNumericID, productName);
          productID = currentProductNumericID;
        } else {
          productID = dataset.getProductName2IdMap().get(productName);
        }

        long basketID = Long.parseLong(splittedLineList[3]);
        if (!dataset.getBasket2SetOfProductsMap().containsKey(basketID)) {
          dataset.getBasket2SetOfProductsMap().put(basketID, new HashSet<>());
        }
        dataset.getBasket2SetOfProductsMap().get(basketID).add(productID);

        if (!dataset.getProduct2SetOfBasketsMap().containsKey(productID)) {
          dataset.getProduct2SetOfBasketsMap().put(productID, new HashSet<>());
        }
        dataset.getProduct2SetOfBasketsMap().get(productID).add(basketID);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return dataset;
  }
}
