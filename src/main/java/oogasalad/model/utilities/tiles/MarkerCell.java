package oogasalad.model.utilities.tiles;

import java.util.List;
import java.util.function.Function;
import oogasalad.model.utilities.Coordinate;
import oogasalad.model.utilities.tiles.enums.CellState;
import oogasalad.model.utilities.tiles.enums.Marker;

public class MarkerCell implements CellInterface {

  private Marker myCellState;
  private int myHealth;
  private Coordinate myCoordinates;

  public MarkerCell(Marker state, Coordinate c) {
    myCellState = state;
    myCoordinates = c;
    myHealth = 0;
  }

  @Override
  public int hit() {
    return 0;
  }

  @Override
  public List<Function> boardUpdate() {
    return null;
  }

  @Override
  public List<Function> playerUpdate() {
    return null;
  }

  @Override
  public boolean canCarryObject() {
    return false;
  }

  @Override
  public void updateCoordinates(int row, int col) {

  }

  @Override
  public Coordinate getCoordinates() {
    return myCoordinates;
  }

  @Override
  public CellState getCellState() {
    return null;
//    return myCellState;
  }

  @Override
  public int getHealth() {
    return myHealth;
  }
}
