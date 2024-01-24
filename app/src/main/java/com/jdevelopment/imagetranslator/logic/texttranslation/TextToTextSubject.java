package com.jdevelopment.imagetranslator.logic.texttranslation;

public interface TextToTextSubject {
    void addObserver(TextToTextTranslationObserver o);
    void removeObserver(TextToTextTranslationObserver o);
    void notifyObservers();
}
