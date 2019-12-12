package client.presentation;

import common.Constants;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class FXUtil {
    private static final Image DEFAULT_AVATAR_MIN=new Image(UIController.class.getResourceAsStream("resources/default_avatar_min.png"));
    private static final Image DEFAULT_AVATAR=new Image(UIController.class.getResourceAsStream("resources/default_avatar.png"));
    private static final Image DEFAULT_ICON=new Image(UIController.class.getResourceAsStream("resources/icon.png"));

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
}

