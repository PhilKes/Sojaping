package test.server;

import org.junit.BeforeClass;
import org.junit.Test;
import server.TranslationService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TranslationServiceTest {
    private static TranslationService service;

    @BeforeClass
    public static void init(){
        service=new TranslationService();
    }

    @Test
    public void testTranslation(){
        System.out.println("testTranslation:");
        String originalTxt="Hallo wie geht es dir an diesem schönen Tag?";
        String translatedTxt=service.translate(originalTxt,
                TranslationService.languages.get("German"),TranslationService.languages.get("English"));
        System.out.println("de: "+originalTxt+"\n"+"en: "+translatedTxt);
        assertEquals(translatedTxt,"Hello, how are you doing on this beautiful day?");
    }

    @Test
    public void testIdentifyLanguage(){
        System.out.println("testIdentifyLanguage:");
        String text="Ik geniet echt van dit project";
        String language=service.identifyLanguage(text);
        System.out.println(text+"\nLanguage: "+language);
        assertEquals(language,TranslationService.languages.get("Dutch"));
    }

    @Test
    public void testNotIdentifyLanguage(){
        System.out.println("testINotdentifyLanguage:");
        String text="roflcopter lol";
        String language=service.identifyLanguage(text);
        System.out.println(text+"\nLanguage: "+language);
        assertNull(language);
    }
}
