package com.jdevelopment.imagetranslator;

public interface ImageToTextSubject {
    void addObserver(ImageToTextObserver o);
    void removeObserver(ImageToTextObserver o);
    void notifyObservers();
}
