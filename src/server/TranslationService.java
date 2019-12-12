package server;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.*;

import java.util.Map;
import java.util.stream.Collectors;

import static common.Constants.Translation.ENGLISH_VALUE;

/**
 * Provides interface for IBM Watson Translation API
 * https://cloud.ibm.com/apidocs/language-translator
 */
public class TranslationService {
    private final static String API_KEY="0jVDMEAoG3a7rEOfzvaA5WVzxrSbnSrmW_GyYnInySk2";
    private final static String SERVICE_URL="https://gateway-fra.watsonplatform.net/language-translator/api";
    private final static String DATE="2019-04-30";

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
        /** Check if identifiedlanguages is not empty and has at least 19% confidence */
        if(languages.getLanguages().size()>0 && languages.getLanguages().get(0).getConfidence()>0.19) {
            return languages.getLanguages().get(0).getLanguage();
        }
        else {
            System.out.println("Couldnt identify language of: '" + text + "', confidence too small");
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
