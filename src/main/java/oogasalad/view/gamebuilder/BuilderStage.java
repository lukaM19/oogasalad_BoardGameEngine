package oogasalad.view.gamebuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import oogasalad.view.maker.LabelMaker;

public abstract class BuilderStage {

  private ResourceBundle myBuilderResources;
  private int selectedType;
  private List<Rectangle> colorOptionList = new ArrayList<>();
  private String itemID;
  private Stage myStage;
  private final ListView<String> objectsListView = new ListView<>();
  private ObservableList<String> objectList = FXCollections.observableArrayList();
  private int originX;
  private int originY;
  private int activeWidth;
  private int activeHeight;

  private static final int DEFAULT_INPUT_BOX_WIDTH = 120;
  private static final int DEFAULT_INPUT_BOX_HEIGHT = 30;
  private static final int MAX_OBJECT_LIST_WIDTH = 200;
  private static final int MAX_OBJECT_LIST_HEIGHT = 400;

  public BuilderStage() {
    myBuilderResources = ResourceBundle.getBundle("/BuilderInfo");
  }

  protected ListView setUpObjectView() {
    objectsListView.setItems(objectList);
    objectsListView.setMaxSize(MAX_OBJECT_LIST_WIDTH, MAX_OBJECT_LIST_HEIGHT);
    return objectsListView;
  }

  protected void addToObjectList(String s) {
    objectList.add(s);
  }

  protected void clearObjectList() {
    objectList.clear();
  }

  protected ResourceBundle getMyBuilderResources() {
    return myBuilderResources;
  }

  protected void setUpStage(BorderPane myPane) {
    myStage = new Stage();
    myStage.setScene(getScene(myPane));
    myStage.showAndWait();
  }

  protected void closeWindow() {
    myStage.close();
  }

  public Scene getScene(BorderPane myPane) {
    return new Scene(myPane, 900, 500);
  }

  protected int[][] initializeMatrixWithValue(int height, int width, int initialValue) {
    int[][] stateMap = new int[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        stateMap[i][j] = initialValue;
      }
    }
    return stateMap;
  }

  protected Object createInstance(String className, Class<?>[] parameterTypes, Object[] parameters)
      throws IOException {

    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> constructor = clazz.getConstructor(parameterTypes);
      return constructor.newInstance(parameters);
    } catch (Error | Exception e) {
      e.printStackTrace();
      throw new IOException(String.format("Class parsing failed: %s className"));
    }

  }

  protected Button makeButton(String name, EventHandler<ActionEvent> handler) {
    Button result = new Button(name);
    result.setOnAction(handler);

    return result;
  }

  protected TextArea makeTextArea() {
    TextArea result = new TextArea();
    result.setMaxSize(DEFAULT_INPUT_BOX_WIDTH, DEFAULT_INPUT_BOX_HEIGHT);

    return result;
  }

  protected TextArea makeTextAreaWithDefaultValue(String text) {
    TextArea result = makeTextArea();
    result.setText(text);
    return result;
  }

  protected HBox makeComboBoxWithVariable(String[] options, String buttonText,
      Consumer<String> consumer) {

    TextArea infoBox = new TextArea();
    ComboBox comboBox = makeComboBox(options);
    infoBox.setMaxSize(50, 20);
    HBox result = new HBox(comboBox);
    result.getChildren().add(infoBox);
    result.getChildren()
        .add(makeButton(buttonText, e -> consumer.accept(comboBox.getValue() + infoBox.getText())));
    return result;
  }

  protected Group arrangeCells(int height, int width, double cellHeight, double cellWidth,
      int[][] stateMap) {
    Group cellGroup = new Group();
    double xPos;
    double yPos = 0;
    for (int i = 0; i < height; i++) {
      xPos = 0;
      for (int j = 0; j < width; j++) {
        cellGroup.getChildren().add(createCell(xPos, yPos, i, j, stateMap[i][j]));
        xPos = xPos + cellWidth;
      }
      yPos = yPos + cellHeight;
    }
    return cellGroup;
  }

  protected VBox displayColorChoice(String[] DEFAULT_STATE_OPTIONS, List<Color> colorList) {
    VBox result = new VBox();
    for (Color c : colorList) {
      Rectangle option = createColorOptionRectangle(c, colorList);
      colorOptionList.add(option);
      result.getChildren().add(new HBox(new Text(DEFAULT_STATE_OPTIONS[colorList.indexOf(c)]),
          option));

    }
    result.setId("ColorChoiceBox");

    return result;
  }

  protected int getSelectedType() {
    return selectedType;
  }

  private Rectangle createColorOptionRectangle(Color c, List<Color> colorList) {
    Rectangle result = new Rectangle(50, 25);
    result.setFill(c);
    result.setId("ColorOption");

    result.setOnMouseClicked(e -> {
      removeSelectionStrokeFromOption();
      result.setStroke(Color.RED);
      selectedType = colorList.indexOf(c);
    });
    return result;
  }

  private void removeSelectionStrokeFromOption() {
    for (Rectangle option : colorOptionList) {
      option.setStroke(null);
    }
  }

  protected Button makeContinueButton() {

    Button continueButton = new Button("Continue");
    continueButton.setOnAction(e -> saveAndContinue());
    continueButton.setId("ContinueButton");
    return continueButton;
  }

  protected Boolean checkIntConversion(String string) {
    try {
      int x = Integer.parseInt(string);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  protected ComboBox makeComboBox(String[] options) {
    ComboBox comboBox = new ComboBox();
    for (String option : options) {
      comboBox.getItems().add(option);

    }

    return comboBox;
  }

  protected HBox makeIdInput(String defaultId) {
    return new HBox(LabelMaker.makeLabel("id:", "id" + "_label"),
        makeTextAreaWithDefaultValue(defaultId));
  }

  protected void findReferencePoint(int[][] stateMap) {
    int minX = stateMap.length;
    int minY = stateMap[0].length;
    int maxX = 0;
    int maxY = 0;
    for (int i = 0; i < stateMap.length; i++) {
      for (int j = 0; j < stateMap[0].length; j++) {
        if (stateMap[i][j] != 0) {
          if (i <= minX) {
            minX = i;
          }
          if (j <= minY) {
            minY = j;
          }
          if (i >= maxX) {
            maxX = i;
          }
          if (j >= maxY) {
            maxY = j;
          }
        }
      }
    }
    activeHeight = maxY - minY + 1;
    activeWidth = maxX - minX + 1;
    originX = minX;
    originY = minY;
  }

  protected int getOriginX() {
    return originX;
  }

  protected int getOriginY() {
    return originY;
  }

  protected int getActiveWidth() {
    return activeWidth;
  }

  protected int getActiveHeight() {
    return activeHeight;
  }

  protected int[][] cropToActiveGrid(int[][] stateMap){
    int[][] croppedGrid=new int[activeWidth][activeHeight];
    System.out.println(originX+"width"+(originX+activeWidth));
    System.out.println(originY+"height"+(originY+activeHeight));
    for(int i=originX;i<originX+activeWidth;i++){
      for(int j=originY;j<originY+activeHeight;j++){
          croppedGrid[i-originX][j-originY]=stateMap[i][j];
          System.out.print(croppedGrid[i-originX][j-originY]);
        //System.out.print("("+(i-originX)+";"+(j-originY)+")");

      }
      System.out.println();
    }

    return croppedGrid;
  }

  protected abstract Rectangle createCell(double xPos, double yPos, int i, int j, int state);

  protected abstract Object launch();

  protected abstract Object saveAndContinue();
}