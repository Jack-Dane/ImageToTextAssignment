package com.jdevelopment.imagetranslator.logic.texttranslation;

public interface TextToTextTranslationObserver {
    void updateTranslatedText(String text, String originLanguage, String translatedLanguage);
    void updateTranslatedTextError();
}
