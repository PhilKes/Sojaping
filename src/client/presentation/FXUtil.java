package client.presentation;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import common.Constants;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static common.Constants.Contexts.INVITATION_EMAIL;

/**
 * JavaFX Util Class for external resoruces,...
 */
public class FXUtil {
    private static final Image DEFAULT_AVATAR_MIN = new Image(UIController.class.getResourceAsStream("resources/default_avatar_min.png"));
    private static final Image DEFAULT_AVATAR = new Image(UIController.class.getResourceAsStream("resources/default_avatar.png"));
    private static final Image DEFAULT_ICON = new Image(UIController.class.getResourceAsStream("resources/icon.png"));
    public static final double SMILEY_BAR_HEIGHT = 46.0, SMILEY_BAR_WIDTH = 687.0;
    private static final File SMILEY_PATH = getSmileyDirFile();

    public static Image getDefaultAvatarMin() {
        return DEFAULT_AVATAR_MIN;
    }

    public static Image getDefaultAvatar() {
        return DEFAULT_AVATAR;
    }

    public static Image getDefaultIcon() {
        return DEFAULT_ICON;
    }

    public static void fillLanguageMenu(final MenuButton menuLanguages, List<String> selectedLanguages, int[] languageCounter) {
        final List<CheckMenuItem> items=Constants.Translation.getSupportedLanguages().keySet()
                .stream().sorted().map(CheckMenuItem::new).collect(Collectors.toList());
        menuLanguages.getItems().addAll(items);

        /** Add languages to selectedLangauges and highlight in item List*/
        for(final CheckMenuItem item : items) {
            item.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if(newValue) {
                    /** Add language */
                    selectedLanguages.add(item.getText());
                    languageCounter[0]++;
                    item.setText(languageCounter[0] + "." + item.getText());
                    menuLanguages.setStyle("");
                }
                else {
                    /** Remove language*/
                    String t=item.getText();
                    String[] s=t.split("\\.");
                    item.setText(s[1]);
                    selectedLanguages.remove(s[1]);
                    languageCounter[0]--;
                    /** Update selected numbers*/
                    for(int i=0; i<languageCounter[0]; i++) {
                        String lang=selectedLanguages.get(i);
                        for(CheckMenuItem menuItem : items.stream().filter(CheckMenuItem::isSelected)
                                .collect(Collectors.toList())) {
                            if(menuItem.getText().split("\\.")[1].equals(lang)) {
                                menuItem.setText((i + 1) + "." + lang);
                                break;
                            }
                        }
                    }
                    if(selectedLanguages.isEmpty()) {
                        menuLanguages.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    }
                }
                menuLanguages.setText(String.join(",", selectedLanguages));
            });
        }
    }

    public static GlyphIcon generateIcon(FontAwesomeIcon icon) {
        return GlyphsBuilder.create(FontAwesomeIconView.class).
                glyph(icon).build();
    }

    /**
     * Show error message with Label for 2 Seconds
     */
    public static void showInfo(Label labelInfo, String message, UIControllerWithInfo.InfoType type) {
        Platform.runLater(() -> {
            if(message==null) {
                labelInfo.setText("");
                labelInfo.setDisable(true);
            }
            else {
                labelInfo.setText(message);
                labelInfo.setDisable(false);
                /** See main.css for ID styling */
                labelInfo.setId(type.get());
                PauseTransition delay=new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(event -> showInfo(labelInfo, null, type));
                delay.play();
            }
        });
    }

    /**
     * Return OutputStream to MessageStore XML File of user
     */
    public static OutputStream getMessageStoreFileOutStream(String userName) {
        File file=null;
        try {
            String resourceUrl=UIController.class.getResource("resources/").getPath();
            resourceUrl+="messageStore_" + userName + ".xml";
            System.out.println(resourceUrl);
            file=new File(resourceUrl);
            file.createNewFile();
            OutputStream output=new FileOutputStream(file);
            return output;
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static InputStream getMessageStoreFileStream(String userName) {
        return UIController.class.getResourceAsStream("resources/messageStore_" + userName + ".xml");
    }

    public static File getMessageStoreFile(String userName) {
        return new File(UIController.class.getResource("resources/").getPath() + "messageStore_" + userName + ".xml");
    }

    public static File getSmileyDirFile() {
        return new File(UIController.class.getResource("resources/smileys/").getPath());
    }

    public static Image getSmileyImage(int i) {
        File smileyDir = SMILEY_PATH;
        File[] smileys = smileyDir.listFiles();
        if (smileys.length < i) {
            return null;
        }
        File smiley = smileys[i];
        return new Image(smiley.toURI().toString());
    }

    public static String getSmileyImagePath(int i) {
        File smileyDir = SMILEY_PATH;
        File[] smileys = smileyDir.listFiles();
        if (smileys.length < i) {
            return null;
        }
        File smiley = smileys[i];
        return smiley.getPath();
    }

    public static List<String> getSmileyImagePaths() {
        File smileyDir = SMILEY_PATH;
        File[] smileys = smileyDir.listFiles();
        return Arrays.stream(smileys).map(f -> f.getPath()).collect(Collectors.toList());
    }

    public static String convertFileToBase64(String filePath){
        byte[] fileContent = new byte[0];
        try {
            fileContent = FileUtils.readFileToByteArray(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static Image convertBase64ToImage(String base64Picture){
        byte[] imageBytes= new byte[0];
        try {
            imageBytes = com.sun.org.apache.xml.internal.security.utils.Base64.decode(base64Picture.getBytes());
        } catch (Base64DecodingException e) {
            e.printStackTrace();
        }
        ByteArrayInputStream is=new ByteArrayInputStream(imageBytes);
        return new Image(is);
    }

    public static Rectangle2D getImageCropBounds(Image image) {
        double imgWidth=image.getWidth();
        double imgHeight=image.getHeight();
        double cube=Math.min(imgWidth, imgHeight);
        double x=imgWidth / 2 - cube / 2;
        if(x<0) {
            x=0;
        }
        double y=imgHeight / 2 - cube / 2;
        if(y<0) {
            y=0;
        }
        return new Rectangle2D(x, y, cube, cube);
    }

    /**
     * Sets cropped Image as Avatar or loads default Avatar if no profilepicture
     */
    public static void setAvatarOfProfile(ImageView imgView, String profilePicture) {
        if(profilePicture!=null && !"".equals(profilePicture)) {
            Image image=FXUtil.convertBase64ToImage(profilePicture);
            imgView.setImage(image);
            imgView.setViewport(FXUtil.getImageCropBounds(image));

        }
        else {/** Default Avatar */
            Image img=FXUtil.getDefaultAvatar();
            imgView.setImage(img);
            imgView.setViewport(new Rectangle2D(0, 0, img.getWidth(), img.getHeight()));
        }
    }
}

