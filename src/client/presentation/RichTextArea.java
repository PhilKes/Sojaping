package client.presentation;

import client.presentation.genericarea.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;
import org.reactfx.util.Either;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Modfied version of RichTextDemo Area
 * See https://github.com/FXMisc/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/richtext/RichTextDemo.java
 */
public class RichTextArea {

    private final TextOps<String, TextStyle> styledTextOps=SegmentOps.styledTextOps();
    private final LinkedImageOps<TextStyle> linkedImageOps=new LinkedImageOps<>();
    private final TextStyle defaultTextStyle=TextStyle.EMPTY.updateFontSize(10).updateFontFamily("Arial").updateTextColor(Color.WHITE);

    private final GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> area=
            new GenericStyledArea<>(
                    ParStyle.EMPTY,                                                 // default paragraph style
                    (paragraph, style) -> paragraph.setStyle(style.toCss()),        // paragraph style setter

                    defaultTextStyle,  // default segment style
                    styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()),                            // segment operations
                    seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss())));                     // Node creator and segment style setter

    {
        area.setWrapText(true);
        area.setStyleCodecs(
                ParStyle.CODEC,
                Codec.styledSegmentCodec(Codec.eitherCodec(Codec.STRING_CODEC, LinkedImage.codec()), TextStyle.CODEC));
    }

    public GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> getArea() {
        return area;
    }

    private Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg,
                            BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
                text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
                LinkedImage::createNode
        );
    }

    /**
     * Action listener which inserts a new image at the current caret position.
     */
    public void insertImage(String imagePath) {
        imagePath=imagePath.replace('\\', '/');
        RealLinkedImage img=null;
        //try {
           /* String path=UIController.class.getResource(imagePath).toURI().toString().substring(6);
            if(FXUtil.onJar) {
                path=path.substring(4);
            }
            System.out.println("Image: " + path);*/
        if(FXUtil.onJar) {
            img=new RealLinkedImage(imagePath.substring(1));
        }
        else {
            String path=null;
            try {
                path=UIController.class.getResource(imagePath).toURI().toString().substring(6);
                img=new RealLinkedImage(path);
            }
            catch(URISyntaxException e) {
                e.printStackTrace();
            }
        }

        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros=
                ReadOnlyStyledDocument.fromSegment(Either.right(img),
                        ParStyle.EMPTY, defaultTextStyle, area.getSegOps());
        area.replaceSelection(ros);
        /*}
        catch(URISyntaxException e) {
            e.printStackTrace();
        }*/
    }

}
