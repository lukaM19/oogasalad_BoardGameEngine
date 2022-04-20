package oogasalad.view.gameBuilder;

import java.util.Arrays;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

public class BasicGameSetupStage extends BuilderStage {

  private BorderPane myPane;
  private String availableWinConditionTypes;
  private String availablePlayerTypes;
  private String availableEngineTypes;
  private String needEngineTypes;
  private String selectedEngineType;
  private ComboBox engineTypeComboBox;
  private HBox playerSelectionBox;


  public BasicGameSetupStage() {
    myPane = new BorderPane();
    availablePlayerTypes = getMyBuilderResources().getString("possiblePlayerType");
    needEngineTypes = getMyBuilderResources().getString("needsEngineSelection");
    myPane.setTop(makePlayerSelectionBox());
    selectedEngineType = getMyBuilderResources().getString("BlankEngineState");

    makeContinueButton();
    setUpStage(myPane);
  }

  private HBox makePlayerSelectionBox() {
    playerSelectionBox = new HBox();
    ComboBox comboBox = makeComboBox(availablePlayerTypes.split(","));
    playerSelectionBox.getChildren().add(comboBox);
    playerSelectionBox.getChildren().add(makeButton("Select", e -> handlePlayerSelection(comboBox)));

    return playerSelectionBox;
  }

  private void resetSelection() {
    engineTypeComboBox=null;
  }

  private void handlePlayerSelection(ComboBox comboBox) {
    try {
      resetSelection();

      String selection = comboBox.getValue().toString();
      if (Arrays.stream(needEngineTypes.split(",")).anyMatch(selection::equals)) {
        addEngineSelectionOption(playerSelectionBox, selection);
      }
      playerSelectionBox.getChildren()
          .add(makeButton("Add Player", e->addSavePlayerButton(selection, engineTypeComboBox)));


    } catch (NullPointerException e) {
      System.out.println("Please Make Selection");
      e.printStackTrace();
    }
  }

  private void addSavePlayerButton(String selectedPlayerType,
      ComboBox engineTypeComboBox) {
    if (engineTypeComboBox!=null) {
      try {
        selectedEngineType = engineTypeComboBox.getValue().toString();
      } catch (NullPointerException e) {

      }
    }
    System.out.println(selectedPlayerType+selectedEngineType);

  }

  private void addEngineSelectionOption(HBox result, String selection) {
    String[] engineOptions = getMyBuilderResources().getString(selection + "EngineOption")
        .split(",");
    engineTypeComboBox = makeComboBox(engineOptions);
    result.getChildren().add(engineTypeComboBox);


  }

  @Override
  protected Rectangle createCell(double xPos, double yPos, int i, int j, int state) {
    return null;
  }

  @Override
  protected Object saveAndContinue() {
    closeWindow();
    return null;
  }
}
