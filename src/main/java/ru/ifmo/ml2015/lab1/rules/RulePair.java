package ru.ifmo.ml2015.lab1.rules;

import java.util.List;

public class RulePair {
  private List<Integer> x;
  private List<Integer> y;

  public RulePair(List<Integer> x, List<Integer> y) {
    this.x = x;
    this.y = y;
  }

  public List<Integer> getX() {
    return x;
  }

  public List<Integer> getY() {
    return y;
  }
}
