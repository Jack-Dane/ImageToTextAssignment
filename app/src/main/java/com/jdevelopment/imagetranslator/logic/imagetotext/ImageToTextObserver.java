package com.jdevelopment.imagetranslator.logic.imagetotext;

public interface ImageToTextObserver {
    void updateText(String text);
    void updateTextError();
}
