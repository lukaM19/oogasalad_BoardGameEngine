package oogasalad.view;

import com.stripe.exception.StripeException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javafx.stage.Stage;
import javafx.util.Duration;
import oogasalad.PropertyObservable;
import oogasalad.model.utilities.Coordinate;
import oogasalad.model.utilities.tiles.enums.CellState;

import oogasalad.com.stripe.StripeIntegration;
import oogasalad.view.board.BoardView;
import oogasalad.view.board.EnemyBoardView;
import oogasalad.view.board.GameBoardView;
import oogasalad.view.board.SelfBoardView;
import oogasalad.view.interfaces.BoardVisualizer;
import oogasalad.view.interfaces.GameDataVisualizer;
import oogasalad.view.interfaces.ShopVisualizer;
import oogasalad.view.interfaces.ShotVisualizer;
import oogasalad.view.maker.BoxMaker;
import oogasalad.view.maker.ButtonMaker;
import oogasalad.view.maker.LabelMaker;
import oogasalad.view.panes.ConfigPane;
import oogasalad.view.panes.LegendPane;
import oogasalad.view.panes.SetPiecePane;
import oogasalad.view.panels.TitlePanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameView extends PropertyObservable implements PropertyChangeListener, BoardVisualizer,
    ShopVisualizer, ShotVisualizer, GameDataVisualizer {

  private static final Logger LOG = LogManager.getLogger(GameView.class);
  private static final double SCREEN_WIDTH = 1200;
  private static final double SCREEN_HEIGHT = 800;
  private static final String DEFAULT_RESOURCE_PACKAGE = "/";
  private static final String DAY_STYLESHEET = "stylesheets/viewStylesheet.css";
  private static final String NIGHT_STYLESHEET = "stylesheets/nightStylesheet.css";
  private static final String CELL_STATE_RESOURCES_PATH = "/CellState";
  private static final String IMAGES_PATH = "images/";
  private static final String EXPLOSION_IMAGE_NAME = "explosion-icon.png";
  private static final String BOARD_CLICKED_LOG = "Board %d was clicked at row: %d, col: %d";
  private static final String BOARD_HOVERED_LOG = "Board %d was hovered over at row: %d, col: %d";
  private static final String CENTER_PANE_ID = "view-center-pane";
  private static final String VIEW_PANE_ID = "view-pane";
  private static final String INVALID_METHOD = "Invalid method name given";
  private static final String SHOT_METHOD = "handleShot";
  private static final double BOARD_SIZE = 50;
  private static final int EXPLOSION_DURATION = 2000;


  public static ResourceBundle CELL_STATE_RESOURCES = ResourceBundle.getBundle(
      CELL_STATE_RESOURCES_PATH);
  public static final String FILL_PREFIX = "FillColor_";

  private TitlePanel myTitle;
  private List<BoardView> myBoards;
  private List<Collection<Collection<Coordinate>>> myPiecesLeft;
  private BorderPane myPane;
  private VBox myCenterPane;
  private Label currentBoardLabel;
  private HBox boardButtonBox;
  private Button leftButton;
  private Button rightButton;
  private Button endTurnButton;
  private Button stripeButton;
  private VBox myRightPane;
  private Button shopButton;
  private SetPiecePane piecesRemainingPane;
  private LegendPane pieceLegendPane;
  private ConfigPane configPane;
  private DynamicLabel shotsRemainingLabel;
  private DynamicLabel numPiecesLabel;
  private DynamicLabel goldLabel;
  private PassComputerMessageView passComputerMessageView;
  private ResourceBundle myResources;
  private boolean nightMode;

  private Scene myScene;

  private int currentBoardIndex;

  private Map<Integer, String> playerIDToNames;

  public GameView(List<CellState[][]> firstPlayerBoards,
      Collection<Collection<Coordinate>> initialPiecesLeft, Map<Integer, String> idToNames, ResourceBundle resourceBundle) {
    myPane = new BorderPane();
    myPane.setId(VIEW_PANE_ID);
    nightMode = false;
    myBoards = new ArrayList<>();
    myPiecesLeft = new ArrayList<>();
    currentBoardIndex = 0;
    playerIDToNames = idToNames;
    myResources = resourceBundle;
    initialize(firstPlayerBoards, initialPiecesLeft);

  }

  private List<Integer> createInitialIDList(int numPlayers) {
    List<Integer> idList = new ArrayList<>();
    for (int i = 0; i < numPlayers; i++) {
      idList.add(i);
    }
    return idList;
  }

  private void initialize(List<CellState[][]> firstPlayerBoards,
      Collection<Collection<Coordinate>> initialPiecesLeft) {
    initializeBoards(firstPlayerBoards, createInitialIDList(firstPlayerBoards.size()));
    createCenterPane();
    createRightPane();
    createTitlePanel();
    createPassMessageView();
    initializePiecesLeft(initialPiecesLeft);
  }

  public void updateLabels(int shots, int numPieces, int gold) {
    shotsRemainingLabel.changeDynamicText(String.valueOf(shots));
    numPiecesLabel.changeDynamicText(String.valueOf(numPieces));
    goldLabel.changeDynamicText(String.valueOf(gold));
  }

  private void createPassMessageView() {
    passComputerMessageView = new PassComputerMessageView();
    passComputerMessageView.setButtonOnMouseClicked(e -> myScene.setRoot(myPane));
  }

  public void initializePiecesLeft(Collection<Collection<Coordinate>> piecesLeft) {
    for (int i = 0; i < myBoards.size(); i++) {
      myPiecesLeft.add(piecesLeft);
    }
    updatePiecesLeft(myPiecesLeft.get(currentBoardIndex));
  }

  private void initializeBoards(List<CellState[][]> boards, List<Integer> idList) {
    GameBoardView self = new SelfBoardView(BOARD_SIZE, boards.get(0), idList.get(0));
    myBoards.add(self);
    self.addObserver(this);
    for (int i = 1; i < boards.size(); i++) {
      GameBoardView enemy = new EnemyBoardView(BOARD_SIZE, boards.get(i), idList.get(i));
      myBoards.add(enemy);
      enemy.addObserver(this);
    }
  }

  public Scene createScene() {
    myScene = new Scene(myPane, SCREEN_WIDTH, SCREEN_HEIGHT);
    myScene.getStylesheets()
        .add(getClass().getResource(DEFAULT_RESOURCE_PACKAGE + DAY_STYLESHEET).toExternalForm());
    return myScene;
  }

  private void createRightPane() {
    shopButton = ButtonMaker.makeTextButton("view-shop", e -> openShop(), "Open Shop");
    StripeIntegration stripeIntegration = new StripeIntegration();
    AtomicBoolean received = new AtomicBoolean(false); // this is true when the button is pressed and the payment has been received
    stripeButton = ButtonMaker.makeTextButton("stripe", e -> {
      try {
        stripeIntegration.build();
      } catch (URISyntaxException ex) {
        ex.printStackTrace();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }, "Stripe");
    Button stripeReceiveButton = ButtonMaker.makeTextButton("stripe-recceive", e -> {
      try {
        received.set(stripeIntegration.receive());
      } catch (StripeException ex) {
        ex.printStackTrace();
      }
    }, "stripe-receive");

    piecesRemainingPane = new SetPiecePane(20);
    piecesRemainingPane.setText("Ships Remaining");

    setupPieceLegendPane();

    shotsRemainingLabel = LabelMaker.makeDynamicLabel("Shots Remaining: %s", "",
        "shots-remaining-label");
    numPiecesLabel = LabelMaker.makeDynamicLabel("Number of Pieces Left: %s", "", "num-pieces-label");
    goldLabel = LabelMaker.makeDynamicLabel("Gold: %s", "", "gold-label");

    configPane = new ConfigPane();
    configPane.setOnAction(e -> changeStylesheet());

    myRightPane = BoxMaker.makeVBox("configBox", 0, Pos.TOP_CENTER, shotsRemainingLabel,
        numPiecesLabel, goldLabel, shopButton, stripeButton, stripeReceiveButton,
        piecesRemainingPane, pieceLegendPane, configPane);
    myRightPane.setMinWidth(300);
    myPane.setRight(myRightPane);
  }


  private void setupPieceLegendPane() {
    LinkedHashMap<String, Color> colorMap = new LinkedHashMap<>();
    for (CellState state : CellState.values()) {
      colorMap.put(state.name(),
          Color.valueOf(CELL_STATE_RESOURCES.getString(FILL_PREFIX + state.name())));
    }
    pieceLegendPane = new LegendPane(colorMap);
  }

  private void createCenterPane() {
    myCenterPane = BoxMaker.makeVBox(CENTER_PANE_ID, 20, Pos.CENTER);
    myPane.setCenter(myCenterPane);

    setupBoardLabel();
    myCenterPane.getChildren().add(myBoards.get(currentBoardIndex).getBoardPane());
    setupBoardButtons();
  }

  private void createTitlePanel() {
    myTitle = new TitlePanel("Player 1's Turn");
    myTitle.setId("game-title");
    myPane.setTop(myTitle);
  }


  private void setupBoardLabel() {
    currentBoardLabel = LabelMaker.makeLabel("Your Board", "board-label");
    currentBoardLabel.setId("currentBoardLabel");
    myCenterPane.getChildren().add(currentBoardLabel);
  }

  private void setupBoardButtons() {
    leftButton = ButtonMaker.makeImageButton("left-button", e -> decrementBoardIndex(),
        IMAGES_PATH + "arrow-left.png", 50, 50);
    leftButton.getStyleClass().add("arrow-button");

    rightButton = ButtonMaker.makeImageButton("right-button", e -> incrementBoardIndex(),
        IMAGES_PATH + "arrow-right.png", 50, 50);
    rightButton.getStyleClass().add("arrow-button");

    endTurnButton = ButtonMaker.makeTextButton("end-turn-button", e -> endTurn(), "End Turn");
    endTurnButton.setDisable(true);
    boardButtonBox = BoxMaker.makeHBox("board-button-box", 20, Pos.CENTER, leftButton, rightButton, endTurnButton);

    myCenterPane.getChildren().add(boardButtonBox);
  }

  public void allowEndTurn() {
    endTurnButton.setDisable(false);
  }

  private void endTurn() {
    endTurnButton.setDisable(true);
    notifyObserver("endTurn", new Info(0, 0, 0));
  }

  // Decrements currentBoardIndex and updates the shown board
  private void decrementBoardIndex() {
    currentBoardIndex = (currentBoardIndex + myBoards.size() - 1) % myBoards.size();
    updateDisplayedBoard();
  }

  // Increments currentBoardIndex and updates the shown board
  private void incrementBoardIndex() {
    currentBoardIndex = (currentBoardIndex + myBoards.size() + 1) % myBoards.size();
    updateDisplayedBoard();
  }

  // Displays the board indicated by the updated value of currentBoardIndex
  private void updateDisplayedBoard() {
    LOG.info("Current board index: " + currentBoardIndex);
    currentBoardLabel.setText(currentBoardIndex == 0 ? "Your Board"
        : "Your Shots Against " + playerIDToNames.getOrDefault(
            myBoards.get(currentBoardIndex).getID(),
            "Player " + (myBoards.get(currentBoardIndex).getID() + 1)));
    refreshCenterPane();
    updatePiecesLeft(myPiecesLeft.get(currentBoardIndex));
    LOG.info("Current board index: " + currentBoardIndex);
    LOG.info("Showing board " + (myBoards.get(currentBoardIndex).getID() + 1));
  }

  private void refreshCenterPane() {
    myCenterPane.getChildren().clear();
    myCenterPane.getChildren()
        .addAll(currentBoardLabel, myBoards.get(currentBoardIndex).getBoardPane(), boardButtonBox);
  }

  private void updateTitle(String playerName) {
    myTitle.changeTitle(playerName + myResources.getString("TurnSuffix"));
  }

  private void switchPlayerMessage(String nextPlayer) {
    passComputerMessageView.setPlayerName(nextPlayer);
    myScene.setRoot(passComputerMessageView);
  }


  public int getCurrentBoardIndex() {
    return currentBoardIndex;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    try {
      Method m = this.getClass().getDeclaredMethod(evt.getPropertyName(), Info.class);
      m.invoke(this, evt.getNewValue());
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
        NullPointerException e) {
      throw new NullPointerException(INVALID_METHOD);
    }
  }

  private void cellClickedSelf(Info info) {
    int id = info.ID();
    int row = info.row();
    int col = info.col();
    LOG.info("cellClickedSelf");
    LOG.info(String.format(BOARD_CLICKED_LOG, id, row, col));
    notifyObserver("applyUsable", info);
  }

  private void cellClickedEnemy(Info info) {
    int id = info.ID();
    int row = info.row();
    int col = info.col();
    LOG.info("cellClickedEnemy");
    LOG.info(String.format(BOARD_CLICKED_LOG, id, row, col));
    notifyObserver(SHOT_METHOD, info);
  }

  private void cellHoveredSelf(Info info) {
//    int id = info.ID();
//    int row = info.row();
//    int col = info.col();
//    LOG.info("cellHoveredSelf");
//    LOG.info(String.format(BOARD_HOVERED_LOG, id, row, col));
  }

  private void cellHoveredEnemy(Info info) {
//    int id = info.ID();
//    int row = info.row();
//    int col = info.col();
//    LOG.info("cellHoveredEnemy");
//    LOG.info(String.format(BOARD_HOVERED_LOG, id, row, col));
  }

  private void cellExitedSelf(Info info) {
//    int id = info.ID();
//    int row = info.row();
//    int col = info.col();
//    LOG.info("cellExitedSelf");
//    LOG.info(String.format(BOARD_HOVERED_LOG, id, row, col));
  }

  private void cellExitedEnemy(Info info) {
//    int id = info.ID();
//    int row = info.row();
//    int col = info.col();
//    LOG.info("cellExitedEnemy");
//    LOG.info(String.format(BOARD_HOVERED_LOG, id, row, col));
  }

  private void changeStylesheet() {
    nightMode = !nightMode;
    myScene.getStylesheets().clear();
    if (nightMode) {
      myScene.getStylesheets().add(getClass().getResource(DEFAULT_RESOURCE_PACKAGE + NIGHT_STYLESHEET).toExternalForm());
    } else {
      myScene.getStylesheets().add(getClass().getResource(DEFAULT_RESOURCE_PACKAGE + DAY_STYLESHEET).toExternalForm());
    }
  }

  /**
   * Places a Piece of a certain type at the specified coordinates
   *
   * @param coords Coordinates to place Piece at
   * @param type   Type of piece being placed
   */

  @Override
  public void placePiece(Collection<Coordinate> coords, CellState type) { //TODO: Change type to some enum
    for (Coordinate coord : coords) {
      myBoards.get(currentBoardIndex).setColorAt(coord.getRow(), coord.getColumn(),
          Color.valueOf(CELL_STATE_RESOURCES.getString(FILL_PREFIX + type.name())));
    }
  }

  /**
   * Removes any Pieces that are at the coordinates contained in coords.
   *
   * @param coords Coordinates that contain pieces to remove
   */
  @Override
  public void removePiece(Collection<Coordinate> coords) {
    for (Coordinate coord : coords) {
      myBoards.get(currentBoardIndex).setColorAt(coord.getRow(), coord.getColumn(),
          Color.valueOf(CELL_STATE_RESOURCES.getString(CellState.WATER.name())));
    }
  }

  public void displayWinningMessage(String name) {
    LOG.info(name + " " + myResources.getString("WonSuffix"));
  }

  public void displayLosingMessage(String name) {
    LOG.info(name + " " + myResources.getString("LostSuffix"));
  }

  /**
   * Updates the user's side-view to show which of the opponent's ships are still alive.
   *
   * @param pieceCoords Coordinates of Piece objects owned by the opponent that are still alive.
   */
  @Override
  public void updatePiecesLeft(Collection<Collection<Coordinate>> pieceCoords) {
    myPiecesLeft.set(currentBoardIndex, pieceCoords);
    piecesRemainingPane.updateShownPieces(pieceCoords);
  }

  /**
   * Updates the text that shows the user how many shots they have left in their turn.
   *
   * @param shotsRemaining number of shots the user has left in their turn
   */
  @Override
  public void setNumShotsRemaining(int shotsRemaining) {
    shotsRemainingLabel.changeDynamicText(String.valueOf(shotsRemaining));
  }

  /**
   * Updates the text that shows the user how much gold they currently have.
   *
   * @param amountOfGold gold that user has
   */
  @Override
  public void setGold(int amountOfGold) {
    goldLabel.changeDynamicText(String.valueOf(amountOfGold));
  }

//  /**
//   * Updates the text that shows the user whose turn it currently is.
//   *
//   * @param playerName name or ID of player whose turn it is
//   */
//  @Override
//  public void setPlayerTurnIndicator(String playerName) {
//
//  }

  /**
   * Updates the text that shows how much health the current player has left.
   *
   * @param healthRemaining amount of health points remaiing
   */
  @Override
  public void setHealthRemaining(int healthRemaining) {
    numPiecesLabel.changeDynamicText(String.valueOf(healthRemaining));
  }

  @Override
  public void openShop() {
    System.out.println("Shop Opened");
  }

  @Override
  public void closeShop() {

  }

  public void showError(String errorMsg) {
    Alert alert = new Alert(AlertType.ERROR, errorMsg);
    Node alertNode = alert.getDialogPane();
    alertNode.setId("alert");
    alert.showAndWait();
  }

  @Override
  public void displayShotAt(int x, int y, CellState result) {
    myBoards.get(currentBoardIndex)
        .setColorAt(x, y, Color.valueOf(CELL_STATE_RESOURCES.getString(FILL_PREFIX + result.name())));
  }

  public void displayShotAnimation(int row, int col, Consumer<Integer> consumer, int id) {
    ImageView explosion = new ImageView();
    explosion.setImage(
        new Image(getClass().getResource(DEFAULT_RESOURCE_PACKAGE + IMAGES_PATH + EXPLOSION_IMAGE_NAME).toString(),
            true));
    myBoards.get(currentBoardIndex).displayExplosionOnCell(row, col, explosion);
    FadeTransition ft = new FadeTransition(new Duration(EXPLOSION_DURATION), explosion);
    ft.setFromValue(1);
    ft.setToValue(0);
    ft.setOnFinished(e -> {
      consumer.accept(id);
      myBoards.get(currentBoardIndex).removeExplosionImage(explosion);
    });
    ft.play();
  }

  public void moveToNextPlayer(List<CellState[][]> boardList, List<Integer> idList,
      List<Collection<Collection<Coordinate>>> pieceList) {
    switchPlayerMessage(" " + (idList.get(0) + 1));
    myBoards.clear();
    myPiecesLeft = pieceList;
    currentBoardIndex = 0;
    int firstID = idList.get(currentBoardIndex);
    initializeBoards(boardList, idList);
    updateTitle(playerIDToNames.get(firstID));
    updateDisplayedBoard();
  }

//  public void updateCurrentPlayerName(String name) {
//    updateTitle(name);
//

  public void displayAIMove(int id, List<Info> shots) {
    String message = "";
    for (int i = 0; i < shots.size(); i++) {
      message += String.format("Player %d took a shot at row %d, column %d on player %d",
          id+1, shots.get(i).row(), shots.get(i).col(), shots.get(i).ID()+1)+"\n";
    }
    Alert alert = new Alert(AlertType.INFORMATION, message);
    Node alertNode = alert.getDialogPane();
    alertNode.setId("alert");
    alert.show();
  }

}