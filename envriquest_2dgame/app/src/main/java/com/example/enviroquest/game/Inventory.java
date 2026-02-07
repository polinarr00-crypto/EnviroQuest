package com.example.enviroquest.game;

import java.util.ArrayList;

public class Inventory {
    private ArrayList<InventoryItem> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    public void addItem(String itemName) {
        for (InventoryItem item : items) {
            if (item.name.equals(itemName)) {
                item.count++;
                return;
            }
        }
        items.add(new InventoryItem(itemName, 1));
    }

    public int getItemCount(String itemName) {
        for (InventoryItem item : items) {
            if (item.name.equals(itemName)) {
                return item.count;
            }
        }
        return 0;
    }

    public int getTotalTrashCount() {
        int total = 0;
        for (InventoryItem item : items) {
            total += item.count;
        }
        return total;
    }

    public ArrayList<InventoryItem> getItems() {
        return items;
    }
}