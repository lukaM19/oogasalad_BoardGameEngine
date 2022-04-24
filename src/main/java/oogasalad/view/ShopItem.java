package oogasalad.view;


import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import oogasalad.PropertyObservable;
import oogasalad.view.maker.BoxMaker;
import oogasalad.view.maker.ButtonMaker;
import oogasalad.view.maker.LabelMaker;

public class ShopItem extends PropertyObservable {
  private static final String DEFAULT_RESOURCE_PACKAGE = "/";
  private static final ResourceBundle WEAPON_IMAGE_RESOURCES = ResourceBundle.getBundle(
          "/WeaponImages");
  private static final String WEAPON_IMAGES_PATH = "images/weapon_images/";

  private static final String PRICE_LABEL_FORMAT = "Price: %d Gold";

  private static final double ITEM_SIZE = 30;
  private static final double X_SPACING = 90;
  private static final double Y_SPACING = 120;
  private static final double GAP = 20;

  private ImageView item; //FIXME replace with ImageView later
  private int myPrice;
  private VBox myVBox;
  private String myName;

  public ShopItem(String itemName, int price, String usableClassName, int index) {
    //item = new Rectangle(WIDTH,HEIGHT);
    //ResourceBundle weaponImageBundle = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE+WEAPON_IMAGES_RESOURCE_BUNDLE);
    String imageName = WEAPON_IMAGE_RESOURCES.getString(usableClassName);
    item = new ImageView(new Image(WEAPON_IMAGES_PATH+imageName));
    item.setFitHeight(ITEM_SIZE);
    item.setFitWidth(ITEM_SIZE);
    myPrice = price;
    myName = itemName;
    Button buyButton = ButtonMaker.makeTextButton("Buy" + itemName, e -> buyItem(), "Buy");
    Label nameLabel = LabelMaker.makeLabel(myName, "shop-item-name-label");
    Label priceLabel = LabelMaker.makeLabel(String.format(PRICE_LABEL_FORMAT, myPrice), "shop-item-price-label");
    myVBox = BoxMaker.makeVBox("VBoxId", 10, Pos.CENTER, nameLabel, item, priceLabel, buyButton);
    myVBox.setLayoutX((GAP + X_SPACING) * (index % 3));
    myVBox.setLayoutY((GAP + Y_SPACING) * (index / 3));
  }

  private void buyItem() {
    notifyObserver("buyItem", myName);
  }

  public VBox getMyVBox() {
    return myVBox;
  }
}
