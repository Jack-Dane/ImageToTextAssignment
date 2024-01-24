package com.jdevelopment.imagetranslator;

public interface TextToTextSubject {
    void addObserver(TextToTextTranslationObserver o);
    void removeObserver(TextToTextTranslationObserver o);
    void notifyObservers();
}
