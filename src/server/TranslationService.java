package server;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides interface for IBM Watson Translation API
 * https://cloud.ibm.com/apidocs/language-translator
 */
public class TranslationService {
    private final static String API_KEY="0jVDMEAoG3a7rEOfzvaA5WVzxrSbnSrmW_GyYnInySk2";
    private final static String SERVICE_URL="https://gateway-fra.watsonplatform.net/language-translator/api";
    private final static String DATE="2019-04-30";
    public final static String ENGLISH_VALUE="en",
            ENGLISH_KEY="English";
    /**
     * All Options for languages
     */
    public final static Map<String, String> languages;

    /** Fill languages Map*/
    static {
        languages=new HashMap<>();
        languages.put("Afrikaans", "af");
        languages.put("Arabic", "ar");
        languages.put("Azerbaijani", "az");
        languages.put("Bashkir", "ba");
        languages.put("Belarusian", "be");
        languages.put("Bulgarian", "bg");
        languages.put("Bengali", "bn");
        languages.put("Catalan", "ca");
        languages.put("Czech", "cs");
        languages.put("Chuvash", "cv");
        languages.put("Danish", "da");
        languages.put("German", "de");
        languages.put("Greek", "el");
        languages.put(ENGLISH_KEY, ENGLISH_VALUE);
        languages.put("Esperanto", "eo");
        languages.put("Spanish", "es");
        languages.put("Estonian", "et");
        languages.put("Basque", "eu");
        languages.put("Persian", "fa");
        languages.put("Finnish", "fi");
        languages.put("French", "fr");
        languages.put("Irish", "ga");
        languages.put("Gujarati", "gu");
        languages.put("Hebrew", "he");
        languages.put("Hindi", "hi");
        languages.put("Croatian", "hr");
        languages.put("Haitian", "ht");
        languages.put("Hungarian", "hu");
        languages.put("Armenian", "hy");
        languages.put("Icelandic", "is");
        languages.put("Italian", "it");
        languages.put("Japanese", "ja");
        languages.put("Georgian", "ka");
        languages.put("Kazakh", "kk");
        languages.put("Central Khmer", "km");
        languages.put("Korean", "ko");
        languages.put("Kurdish", "ku");
        languages.put("Kirghiz", "ky");
        languages.put("Lithuanian", "lt");
        languages.put("Latvian", "lv");
        languages.put("Malayalam", "ml");
        languages.put("Mongolian", "mn");
        languages.put("Malay", "ms");
        languages.put("Maltese", "mt");
        languages.put("Norwegian Bokmal", "nb");
        languages.put("Dutch", "nl");
        languages.put("Norwegian Nynorsk", "nn");
        languages.put("Panjabi", "pa");
        languages.put("Polish", "pl");
        languages.put("Pushto", "ps");
        languages.put("Portuguese", "pt");
        languages.put("Romanian", "ro");
        languages.put("Russian", "ru");
        languages.put("Slovakian", "sk");
        languages.put("Slovenian", "sl");
        languages.put("Somali", "so");
        languages.put("Albanian", "sq");
        languages.put("Serbian", "sr");
        languages.put("Swedish", "sv");
        languages.put("Tamil", "ta");
        languages.put("Telugu", "te");
        languages.put("Thai", "th");
        languages.put("Turkish", "tr");
        languages.put("Ukrainian", "uk");
        languages.put("Urdu", "ur");
        languages.put("Vietnamese", "vi");
        languages.put("Simplified Chinese", "zh");
        languages.put("Traditional Chinese", "zh-TW");
    }

    public static Map<String, String> getSupportedLanguages() {
        Map<String, String> map=new HashMap<>();
        map.put("German", "de");
        map.put("English", "en");
        map.put("Spanish", "es");
        map.put("Italian", "it");
        map.put("Japanese", "ja");
        map.put("French", "fr");
        map.put("Korean", "ko");
        map.put("Dutch", "nl");
        map.put("Russian", "ru");
        map.put("Polish", "pl");
        map.put("Portuguese", "pt");
        map.put("Swedish", "sv");
        map.put("Simplified Chinese", "zh");
        return map;
    }

    private final IamAuthenticator authenticator;
    private final LanguageTranslator languageTranslator;

    public TranslationService() {
        authenticator=new IamAuthenticator(API_KEY);
        languageTranslator=new LanguageTranslator(DATE, authenticator);
        languageTranslator.setServiceUrl(SERVICE_URL);
    }

    /**
     * Returns translated text from entry to exit language
     * if direct translation fails, try translating to english and then to exitLanguage
     */
    public String translate(String text, String entryLanguage, String exitLanguage) {
        TranslateOptions translateOptions=new TranslateOptions.Builder()
                .addText(text)
                .modelId(entryLanguage + "-" + exitLanguage)
                .build();
        TranslationResult result;
        /** Try direct translation*/
        try {
            result=languageTranslator.translate(translateOptions)
                    .execute().getResult();
        }
        catch(NotFoundException e) {
            /** Translation failed if translate to English failed*/
            if(exitLanguage.equals(ENGLISH_VALUE)) {
                return null;
            }
            /** First translate to english and then to exitLanguage if direct translation failed */
            String enTxt=translate(text, entryLanguage, ENGLISH_VALUE);
            return translate(enTxt, ENGLISH_VALUE, exitLanguage);
        }
        if(result.getTranslations().size()>0) {
            return result.getTranslations().get(0).getTranslation();
        }
        return null;
    }

    /**
     * Return most likely language of text (language key), confidence at least 50% otherwise returns null
     */
    public String identifyLanguage(String text) {
        IdentifyOptions identifyOptions=new IdentifyOptions.Builder()
                .text(text)
                .build();
        IdentifiedLanguages languages=languageTranslator.identify(identifyOptions)
                .execute().getResult();
        /** Check if identifiedlanguages is not empty and has at least 50% confidence */
        if(languages.getLanguages().size()>0 && languages.getLanguages().get(0).getConfidence()>0.5) {
            return languages.getLanguages().get(0).getLanguage();
        }
        return null;
    }

    /**
     * Get all available Languages of IBM Translation API
     */
    public Map<String, String> getIBMLanguages() {
        IdentifiableLanguages languages=languageTranslator.listIdentifiableLanguages()
                .execute().getResult();
        Map<String, String> languageMap=languages.getLanguages().stream()
                .collect(Collectors.toMap(IdentifiableLanguage::getName, IdentifiableLanguage::getLanguage));
        return languageMap;
    }

}
