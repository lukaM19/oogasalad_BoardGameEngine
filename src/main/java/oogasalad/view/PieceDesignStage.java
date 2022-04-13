package oogasalad.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class PieceDesignStage extends BuilderStage {

  private final int MAX_DIMENSION = 5;

  private int[][] stateMap;
  private BorderPane myPane;
  private Object pieceType;
  private List<String> piecePath = new ArrayList<>();

  public PieceDesignStage() {
    myPane = new BorderPane();
    stateMap = initializeBlankMap(MAX_DIMENSION, MAX_DIMENSION);
    String t = getMyBuilderResources().getString("possiblePieceType");

    myPane.setTop(makePieceSelectionBox(t.split(",")));
    myPane.setBottom(new ColorPicker());
    Stage myStage = new Stage();

    Scene myScene = new Scene(myPane,1000,500);
    myStage.setScene(myScene);
    myStage.showAndWait();
  }

  @Override
  protected Rectangle createCell(double xPos, double yPos, int i, int j, int state) {
    Rectangle newCell = new Rectangle(xPos, yPos, 20, 20);
    newCell.setStroke(Color.BLACK);
    newCell.setFill(Color.LIME);
    newCell.setOnMouseClicked(e -> {
      stateMap[i][j] = 1;
      newCell.setFill(Color.GRAY);
    });

    return newCell;
  }

  @Override
  protected void saveAndContinue() {

  }

  private VBox makePieceSelectionBox(String[] options) {
    VBox result = new VBox();
    ComboBox comboBox = makeComboBox(options);
    result.getChildren().add(comboBox);
    result.getChildren().add(makeButton("Select", e -> selectPieceType(result, comboBox)));

    return result;
  }

  private ComboBox makeComboBox(String[] options) {
    ComboBox comboBox = new ComboBox();
    for (String option : options) {
      comboBox.getItems().add(option);

    }

    return comboBox;
  }

  private void selectPieceType(VBox result, ComboBox comboBox) {
    pieceType = comboBox.getValue();
    String[] reqVars = getMyBuilderResources().getString(pieceType + "PieceRequiredInfo")
        .split(",");
    if (!reqVars[0].isEmpty()) {
      result.getChildren().add(makeComboBoxWithVariable(reqVars));
    }
    myPane.setCenter(arrangeCells(MAX_DIMENSION, MAX_DIMENSION, 20, stateMap));
  }

  private void updatePath(String path) {
    System.out.println(path);
    piecePath.add(path);
  }

  private VBox makeComboBoxWithVariable(String[] options) {

    TextArea infoBox = new TextArea();
    ComboBox comboBox = makeComboBox(options);
    infoBox.setMaxSize(50,20);
    VBox result = new VBox(comboBox);
    result.getChildren().add(infoBox);
    result.getChildren()
        .add(makeButton("Add to Path", e -> updatePath(comboBox.getValue() + infoBox.getText())));
    return result;
  }
}
