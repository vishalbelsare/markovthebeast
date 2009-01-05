package com.googlecode.thebeast.inference;

import com.googlecode.thebeast.pml.Assignment;
import com.googlecode.thebeast.pml.GroundMarkovNetwork;
import com.googlecode.thebeast.pml.GroundNode;
import com.googlecode.thebeast.pml.PMLVector;

import java.util.ArrayList;

/**
 * @author Sebastian Riedel
 */
public class ExhaustiveMAPSolver {

  private GroundMarkovNetwork gmn;
  private PMLVector weights;
  private int evaluations = 0;

  public ExhaustiveMAPSolver(GroundMarkovNetwork gmn, PMLVector weights) {
    this.gmn = gmn;
    this.weights = weights;
  }

  public Assignment solve() {
    //build list of atoms to change
    Assignment observed = new Assignment(gmn);
    ArrayList<GroundNode> hiddenNodes = new ArrayList<GroundNode>();
    for (GroundNode node : gmn.getNodes())
      if (!observed.hasValue(node))
        hiddenNodes.add(node);

    //create result with observed assignments
    Assignment currentAssignment = new Assignment(observed);

    //create result to return
    Assignment currentResult = new Assignment(currentAssignment);

    //initialize all hidden atoms to 0.0
    currentAssignment.setValue(hiddenNodes, 0.0);

    //max score so far
    double maxScore = Double.MIN_VALUE;

    //initialize number of evaluations
    evaluations = 0;

    //iterate over all possible assignments of the hidden atoms
    int size = hiddenNodes.size();
    int currentIndex = hiddenNodes.size() - 1;
    int minIndex = currentIndex;
    for (int flip = 0; flip < Math.pow(2, size); ++flip) {
      //extract feature vector
      PMLVector features = gmn.extractFeatureVector(currentAssignment);
      ++evaluations;
      double score = weights.dotProduct(features);
      if (score > maxScore) {
        maxScore = score;
        currentResult = new Assignment(currentAssignment);
      }
      //flip
      for (int nodeIndex = 0; nodeIndex < size; ++nodeIndex) {
        if (flip % Math.pow(2, nodeIndex) == 0) {
          GroundNode node = hiddenNodes.get(nodeIndex);
          currentAssignment.setValue(node,
            1.0 - currentAssignment.getValue(node));
        }
      }
    }
    return currentResult;
  }


  public int getEvaluationCount() {
    return evaluations;
  }
}