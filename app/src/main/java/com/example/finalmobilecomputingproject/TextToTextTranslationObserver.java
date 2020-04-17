package com.example.finalmobilecomputingproject;

public interface TextToTextTranslationObserver {
    void updateTranslatedText(String text, String originLanguage, String translatedLanguage);
    void updateTranslatedTextError();
}
