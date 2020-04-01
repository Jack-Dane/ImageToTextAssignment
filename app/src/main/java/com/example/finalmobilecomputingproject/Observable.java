package com.example.finalmobilecomputingproject;

public interface Observable {
    void addObserver(Observer o);
    void removerObserver(Observer o);
    void notifyObservers();
}
