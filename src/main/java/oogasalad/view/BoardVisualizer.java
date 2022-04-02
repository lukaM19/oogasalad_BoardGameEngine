package oogasalad.view;

import java.util.Collection;
import oogasalad.model.utilities.Coordinate;

public interface BoardVisualizer {

  /**
   * Places a Piece of a certain type at the specified coordinates
   * @param coords Coordinates to place Piece at
   * @param type Type of piece being placed
   */
  void placePiece(Collection<Coordinate> coords, String type);

  /**
   * Removes any Pieces that are at the coordinates contained in coords.
   * @param coords Coordinates that contain pieces to remove
   */
  void removePiece(Collection<Coordinate> coords);
}