package client.presentation;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import common.Constants;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JavaFX Util Class for external resoruces,...
 */
public class FXUtil {
    public static boolean onJar=false;
    private static final Image DEFAULT_AVATAR_MIN=new Image(UIController.class.getResourceAsStream("resources/default_avatar_min.png"));
    private static final Image DEFAULT_AVATAR=new Image(UIController.class.getResourceAsStream("resources/default_avatar.png"));
    private static final Image DEFAULT_ICON=new Image(UIController.class.getResourceAsStream("resources/icon.png"));
    public static final Image DEFAULT_GROUP_PIC=new Image(UIController.class.getResourceAsStream("resources/icon.png"));
    public static final double SMILEY_BAR_HEIGHT=46.0, SMILEY_BAR_WIDTH=687.0;
    public static final List<String> SMILEY_PATHS=getSmileyImagePaths();

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
            String resourceUrl=null;
            if(onJar) {
                resourceUrl="client/";
            }
            else {
                resourceUrl=UIController.class.getResource("resources/").getPath();
            }
            resourceUrl+="messageStore_" + userName + ".xml";
            System.out.println("ResourceURL: " + resourceUrl);
            file=new File(resourceUrl);
            file.createNewFile();
            OutputStream output=new FileOutputStream(file);
            return output;
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static InputStream getMessageStoreFileStream(String userName) {
        if(onJar) {
            final URI url;
            try {
                return new FileInputStream("client/messageStore_" + userName + ".xml");
            }

            catch(IOException e) {
                e.printStackTrace();
            }
        }
        else {
            return UIController.class.getResourceAsStream("resources/messageStore_" + userName + ".xml");
        }
        return null;
    }

    public static File getMessageStoreFile(String userName) {
        if(onJar) {
            return new File("client/messageStore_" + userName + ".xml");
        }
        else {
            return new File(UIController.class.getResource("resources/").getPath() + "messageStore_" + userName + ".xml");
        }
    }

    public static File getSmileyDirFile() {
        File f=new File(FXUtil.class.getResource("resources/smileys/").getPath());
        System.out.println("Smiley path :" + f.getPath());
        return f;
    }

    /* public static Image getSmileyImage(int i) {
         File smileyDir = SMILEY_PATH;

         File[] smileys = null;

         smileys= smileyDir.listFiles();

         if(smileys.length<i || i<0) {
             return null;
         }
         File smiley = smileys[i];
         return new Image(smiley.toURI().toString());
     }*/
    public static Image getSmileyImage(int i) {
        return new Image(UIController.class.getResourceAsStream(SMILEY_PATHS.get(i)));
    }

    public static List<String> getSmileyImagePaths() {
        try {
            return getPaths("resources/smileys/");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getPaths(String folderPath) throws IOException {
        final File jarFile=new File(FXUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        List<String> filePaths=new ArrayList<>();
        if(jarFile.isFile()) {  // Run with JAR file
            System.out.println("JAR File: " + jarFile.getPath());
            onJar=true;
            try {
                final URI url=FXUtil.class.getResource(folderPath).toURI();
                FileSystem fileSystem=FileSystems.newFileSystem(url, Collections.<String, Object>emptyMap());
                Path path=fileSystem.getPath("/client/presentation/resources/smileys");
                Stream<Path> walk=Files.walk(path, 1);
                for(Iterator<Path> it=walk.iterator(); it.hasNext(); ) {
                    Path next=it.next();
                    filePaths.add("/client/presentation/resources/smileys/" + next.getFileName().toString());
                }
                filePaths.remove(0); //Folder entry
                Collections.sort(filePaths);
            }
            catch(URISyntaxException e) {
                e.printStackTrace();
            }
        }
        else { // Run with IDE
            final URL url=FXUtil.class.getResource(folderPath);
            if(url!=null) {
                try {
                    final File apps=new File(url.toURI());
                    for(File app : apps.listFiles()) {
                        filePaths.add(folderPath + "/" + app.getName());
                    }
                }
                catch(URISyntaxException ex) {
                    // never happens
                }
            }
        }
        return filePaths;
    }

    public static String convertFileToBase64(String filePath) {
        byte[] fileContent=new byte[0];
        try {
            fileContent=FileUtils.readFileToByteArray(new File(filePath));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static Image convertBase64ToImage(String base64Picture) {
        byte[] imageBytes=new byte[0];
        try {
            imageBytes=com.sun.org.apache.xml.internal.security.utils.Base64.decode(base64Picture.getBytes());
        }
        catch(Base64DecodingException e) {
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
     * Sets cropped Image into imageview or loads default image if no image available
     */
    public static void setBase64PicInImageView(ImageView imgView, String base64Picture) {
        setBase64PicInImageView(imgView, base64Picture, false);
    }

    public static void setBase64PicInImageView(ImageView imgView, String profilePicture, boolean isGroupPicture) {
        if(profilePicture!=null && !"".equals(profilePicture)) {
            Image image=FXUtil.convertBase64ToImage(profilePicture);
            imgView.setImage(image);
            imgView.setViewport(FXUtil.getImageCropBounds(image));
        }
        else {/** Default Avatar */
            Image img=isGroupPicture ? FXUtil.DEFAULT_GROUP_PIC : FXUtil.getDefaultAvatar();
            imgView.setImage(img);
            imgView.setViewport(new Rectangle2D(0, 0, img.getWidth(), img.getHeight()));
        }
    }

    public static String uploadPictureViaFileChooser(Stage stage, ImageView image) throws Exception {
        String base64ProfilePic="";
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("Choose profile picture");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg"));

        File file=fileChooser.showOpenDialog(stage);
        if(file!=null && file.length()>3000000) {
            throw new Exception("Image file is too large. Please, upload a picture < 3 MB");

        }
        else if(file!=null) {
            base64ProfilePic=FXUtil.convertFileToBase64(file.getAbsolutePath());
            Image img=FXUtil.convertBase64ToImage(base64ProfilePic);
            image.setImage(img);
            image.setViewport(FXUtil.getImageCropBounds(img));
        }
        return base64ProfilePic;
    }

}

