package me.alphamode.mclong.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class MCLongKeyBinds {
    public static final KeyMapping toggleBigDecimal = new KeyMapping("key.toggleBigDecimal", GLFW.GLFW_KEY_O, "key.categories.mclong");
    public static final KeyMapping toggleBigInteger = new KeyMapping("key.toggleBigInteger", GLFW.GLFW_KEY_I, "key.categories.mclong");
}
