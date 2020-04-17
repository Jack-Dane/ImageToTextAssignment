package com.example.finalmobilecomputingproject;

public interface ImageToTextSubject {
    void addObserver(ImageToTextObserver o);
    void removeObserver(ImageToTextObserver o);
    void notifyObservers();
}
