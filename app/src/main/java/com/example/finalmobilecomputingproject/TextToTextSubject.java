package com.example.finalmobilecomputingproject;

public interface TextToTextSubject {
    void addObserver(TextToTextTranslationObserver o);
    void removeObserver(TextToTextTranslationObserver o);
    void notifyObservers();
}
