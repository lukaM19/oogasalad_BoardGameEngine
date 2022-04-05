package oogasalad.model.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import oogasalad.model.utilities.Board;
import oogasalad.model.utilities.Coordinate;
import oogasalad.model.utilities.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AIPlayerTest {

  private List<AIPlayer> allPlayers;
  private AIPlayer singlePlayer;


  @BeforeEach
  void setup() {
    // setup player
    allPlayers = new ArrayList<>();
    for(int i = 0; i < 3; i++) {
      Board b = new Board(new int[][]{{1, 1, 1}, {0, 1, 1}});
      AIPlayer p = new AIPlayer(b, i, null);
      allPlayers.add(p);
    }
    // add players to players
    List<Player> listOfPlayers = new ArrayList<>(allPlayers);
    for(AIPlayer p: allPlayers) {
      p.addPlayers(listOfPlayers);
    }
    singlePlayer = allPlayers.get(0);
  }


  @Test
  void initialHealthIsZero() {
    int initialHealth = singlePlayer.getHealth();
    assertEquals(initialHealth, 0);
  }

  @Test
  void noDamageDoneOnEmptyBoard() {
    int initialHealth = singlePlayer.getHealth();
    singlePlayer.strike(new Coordinate(0,0));
    int finalHealth = singlePlayer.getHealth();
    assertEquals(initialHealth, finalHealth);
  }


}
