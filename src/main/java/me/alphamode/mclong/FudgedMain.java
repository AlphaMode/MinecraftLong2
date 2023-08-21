package me.alphamode.mclong;

import cpw.mods.modlauncher.Launcher;

public class FudgedMain {
    public static void main(String[] args) {
        System.setProperty("java.vm.name", "");
        Launcher.main(args);
    }
}
