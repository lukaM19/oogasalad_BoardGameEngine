package oogasalad.model.parsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import oogasalad.model.utilities.winconditions.WinCondition;

public class ParseWinConditions extends ParsedElement{
  private final String PROPERTIES_WINCONDITIONS_FILE = "WinConditionsFile";
  private final String WINCONDITIONS = "WinConditions";
  private final String WINCONDITIONS_JSON = "WinConditions.json";


  @Override
  public void save(Properties props, String location, Object o) throws ParserException {
    location += WINCONDITIONS_JSON;
    List<WinCondition> winConditionsList = (List<WinCondition>) o;
    Gson gson = new GsonBuilder().setPrettyPrinting().
        registerTypeHierarchyAdapter(WinCondition.class, new GSONHelper()).
        create();
    putJsonInProp(props, location, winConditionsList, gson, PROPERTIES_WINCONDITIONS_FILE);
  }

  @Override
  public List<WinCondition> parse(Properties props) throws ParserException {
    String winConditionsFile = props.getProperty(PROPERTIES_WINCONDITIONS_FILE);
    Gson gson = new GsonBuilder().registerTypeAdapter(WinCondition.class, new GSONHelper()).create();
    Type listOfMyClassObject = new TypeToken<ArrayList<WinCondition>>() {}.getType();
    return (List<WinCondition>) getParsedObject(winConditionsFile, gson, listOfMyClassObject, WINCONDITIONS);
  }

  @Override
  public Class getParsedClass() {
    return List.class;
  }
}