package oogasalad.model.parsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import oogasalad.PlayerData;
import oogasalad.model.utilities.Piece;
import oogasalad.model.utilities.tiles.enums.CellState;


public class Parser {


  public Parser() {
  }

  private final String PROPERTIES_PLAYER_LIST = "Players";
  private final String PROPERTIES_PIECES_FILE = "PiecesFile";
  private final String PROPERTIES_BOARD_FILE = "BoardFile";
  private final List<String> REQUIRED_ARGS = List.of(PROPERTIES_PLAYER_LIST, PROPERTIES_PIECES_FILE, PROPERTIES_BOARD_FILE);
  private final String MISSING_ARG = "missingArg";
  private final String DOT = ".";
  private final String PROPERTIES_EXTENSION = "properties";
  private final String JSON_EXTENSION = "json";
  private final List<String> jsonPaths = List.of("PiecesFile", "BoardFile");
  private Properties exceptionMessageProperties;

  public void save(PlayerData data, String pathToNewFile)  {
    exceptionMessageProperties = new Properties();

    try {
      InputStream is = new FileInputStream("src/main/resources/ParserExceptions.properties");
      exceptionMessageProperties.load(is);
      is.close();
    } catch (IOException ignored) {
    }
    File file = new File(pathToNewFile);
    Properties props = new Properties();
    savePlayers(props, data.players());
    String nameOfNewFile = file.toString().replaceFirst("[.][^.]+$", "");
    String nameOfBoardFile = nameOfNewFile + "Board.json";
    String nameOfPiecesFile = nameOfNewFile + "Pieces.json";
    saveBoard(props, data.board(), nameOfBoardFile);
    savePieces(props, data.pieces(), nameOfPiecesFile);
    try {
      FileOutputStream outputStream = new FileOutputStream(file);
      props.store(outputStream, "generated via save");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * @param pathToFile
   * @param expectedExtension
   * @throws Exception
   */
  public void checkExtension(String pathToFile, String expectedExtension) throws ParserException {
    String passedFileExtension = pathToFile.substring(pathToFile.lastIndexOf(DOT) + 1);
    if (!passedFileExtension.equals(expectedExtension)) {
        throw new ParserException(exceptionMessageProperties.getProperty(expectedExtension).formatted(passedFileExtension));
      }
  }

  private void checkProperties(String pathToFile, Properties props) throws ParserException {
    for (String key: REQUIRED_ARGS){
      if(props.getProperty(key) == null) {
        throw new ParserException(exceptionMessageProperties.getProperty(MISSING_ARG).formatted(key));
      }
    }
  }

  
  // add method to check if saving works fine (e.g. the player's saved data is not null)
  /**
   *
   * @param pathToFile properties file
   * @return a parser Player Data
   * @throws FileNotFoundException
   */
  public PlayerData parse(String pathToFile){
    File file = new File(pathToFile);
    Properties props = new Properties();
    try {
      InputStream is = new FileInputStream(file);
      props.load(is);
      is.close();
    } catch (IOException e) {
      if (e instanceof FileNotFoundException) {
        //throw (FileNotFoundException) e;
      }
      e.printStackTrace();
    }

    try {
      checkExtension(pathToFile, PROPERTIES_EXTENSION);
      checkProperties(pathToFile, props);
      for (String path:jsonPaths) {
        checkExtension(props.getProperty(path), JSON_EXTENSION);
      }
    }
    catch(Exception e){
      return null;
    }




    List<String> players = loadPlayers(props);
    CellState[][] cellBoard = loadBoard(props);
    List<Piece> pieces = loadPieces(props);

    return new PlayerData(players, pieces, cellBoard);

  }

  private List<String> loadPlayers(Properties props) {
    String[] playersData = props.getProperty(PROPERTIES_PLAYER_LIST).split(" ");
    return new ArrayList<>(Arrays.asList(playersData));
  }

  private CellState[][] loadBoard(Properties props)  {
    String boardFile = props.getProperty(PROPERTIES_BOARD_FILE);
    Gson gson = new GsonBuilder().create();
    CellState[][] boardData;
    try {
      boardData = gson.fromJson(new FileReader(boardFile), CellState[][].class);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    return boardData;
  }

  private List<Piece> loadPieces(Properties props) {
    String piecesFile = props.getProperty(PROPERTIES_PIECES_FILE);
    Gson gson = new GsonBuilder().registerTypeAdapter(Piece.class, new GSONHelper()).create();

    Type listOfMyClassObject = new TypeToken<ArrayList<Piece>>() {}.getType();
    List<Piece> ret = null;
    try {
      ret = gson.fromJson(new FileReader(piecesFile), listOfMyClassObject);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return ret;
  }

  private void savePlayers(Properties props, List<String> players) {
    props.put(PROPERTIES_PLAYER_LIST, String.join(" ", players));
  }

  private void saveBoard(Properties props, CellState[][] board, String location) {
    //make json for board
    //put link to json in props file
    Gson gson = new GsonBuilder().setPrettyPrinting().
        create();
    String json = gson.toJson(board);
    File myNewFile = new File(location);
    try {
      FileWriter myWriter = new FileWriter(myNewFile);
      myWriter.write(json);
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    props.put(PROPERTIES_BOARD_FILE, myNewFile.toString());
  }

  private void savePieces(Properties props, List<Piece> pieces, String location) {
    Gson gson = new GsonBuilder().setPrettyPrinting().
        registerTypeHierarchyAdapter(Piece.class, new GSONHelper()).
        create();

    String json = gson.toJson(pieces);

    File myNewFile = new File(location);
    try {
      if (myNewFile.createNewFile()) { //new file created
        FileWriter myWriter = new FileWriter(myNewFile);
        myWriter.write(json);
        myWriter.close();
      } else { //file already exists, maybe prompt to make sure you want to delete this
        System.out.println("Encountered else block in saveData in Parser.saveData");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    props.put(PROPERTIES_PIECES_FILE, myNewFile.toString());
  }

}
