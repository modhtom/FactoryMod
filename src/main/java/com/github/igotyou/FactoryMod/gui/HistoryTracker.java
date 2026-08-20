package com.github.igotyou.FactoryMod.gui;

public class HistoryTracker<T extends HistoryItem> {
    public void add(T item) {}
    public boolean hasPrevious() { return false; }
    public T peekPrevious() { return null; }
    public T goBack() { return null; }
}
