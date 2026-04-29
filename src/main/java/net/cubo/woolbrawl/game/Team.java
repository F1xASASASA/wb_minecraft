package net.cubo.woolbrawl.game;

import org.bukkit.Color;
import org.bukkit.Material;

public enum Team {
    RED("§cКрасные", Material.RED_WOOL, Color.fromRGB(0xB3312C), net.kyori.adventure.text.format.NamedTextColor.RED),
    BLUE("§9Синие", Material.BLUE_WOOL, Color.fromRGB(0x3C44AA), net.kyori.adventure.text.format.NamedTextColor.BLUE);

    private final String display;
    private final Material wool;
    private final Color armorColor;
    private final net.kyori.adventure.text.format.NamedTextColor chatColor;

    Team(String display, Material wool, Color armorColor, net.kyori.adventure.text.format.NamedTextColor chatColor) {
        this.display = display;
        this.wool = wool;
        this.armorColor = armorColor;
        this.chatColor = chatColor;
    }

    public String display() { return display; }
    public Material wool() { return wool; }
    public Color armorColor() { return armorColor; }
    public net.kyori.adventure.text.format.NamedTextColor chatColor() { return chatColor; }
    public Team other() { return this == RED ? BLUE : RED; }
}
