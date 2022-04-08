package oogasalad.view.board;

import java.util.ArrayList;
import java.util.List;

public class BoardShapeType extends ShapeType {

  private static final double sizeMultiplier = 60;


  public BoardShapeType(double width, double height) {
    super(width, height);
  }
  public List<Double> calculatePoints(double row, double col, double width, double height) {
    // FIXME: refactor to not use default square points

    List<Double> points = new ArrayList<>();


    // arrays used as multiplicative factors
    int[] xFactor = new int[]{0, 1, 1, 0};
    int[] yFactor = new int[]{0, 0, 1, 1};

    for (int i = 0; i < 4; i++) {
      points.add(calculateXPos(width, col) + sizeMultiplier * xFactor[i]);
      points.add(calculateYPos(height, row) + sizeMultiplier * yFactor[i]);
    }

    return points;
  }

//  private double calculateHeight(double height) {
//    return (myBoardHeight - (height + 1) * Y_SPACING) / height;
//  }
//
//  private double calculateWidth(double width) {
//    return (myBoardWidth - (width + 1) * X_SPACING) / width;
//  }

  protected double calculateXPos(double width, double col) {
    return ((col + 1) * X_SPACING) + (col * sizeMultiplier);
  }

  protected double calculateYPos(double height, double row) {
    return ((row + 1) * Y_SPACING) + (row * sizeMultiplier);
  }
}