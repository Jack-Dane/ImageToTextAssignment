package com.jdevelopment.imagetranslator.logic.imagetotext;

public interface ImageToTextSubject {
    void addObserver(ImageToTextObserver o);
    void removeObserver(ImageToTextObserver o);
    void notifyObservers();
}
