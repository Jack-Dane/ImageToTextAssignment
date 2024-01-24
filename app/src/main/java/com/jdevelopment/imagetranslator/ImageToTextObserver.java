package com.jdevelopment.imagetranslator;

public interface ImageToTextObserver {
    void updateText(String text);
    void updateTextError();
}
