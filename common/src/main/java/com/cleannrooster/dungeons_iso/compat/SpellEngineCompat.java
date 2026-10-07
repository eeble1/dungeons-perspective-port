package com.cleannrooster.dungeons_iso.compat;

import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Method;

/**
 * Spell Engine has no build for this Minecraft version to compile against, so its API is reached
 * reflectively. Every lookup fails soft: if the classes or methods are missing, the compat simply
 * reports "not casting" / "not a staff".
 */
public class SpellEngineCompat {
    private static boolean resolved;
    private static Class<?> casterClass;
    private static Method isCastingSpell;
    private static Class<?> staffClass;

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            casterClass = Class.forName("net.spell_engine.internals.casting.SpellCasterEntity");
            isCastingSpell = casterClass.getMethod("isCastingSpell");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            casterClass = null;
            isCastingSpell = null;
        }
        try {
            staffClass = Class.forName("net.spell_engine.api.item.weapon.StaffItem");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            staffClass = null;
        }
    }

    public static boolean isCasting(){
        resolve();
        var player = MinecraftClient.getInstance().player;
        if (player == null || isCastingSpell == null || !casterClass.isInstance(player)) {
            return false;
        }
        try {
            return (boolean) isCastingSpell.invoke(player);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static boolean isHoldingStaff(){
        resolve();
        var player = MinecraftClient.getInstance().player;
        return player != null && staffClass != null
                && staffClass.isInstance(player.getMainHandStack().getItem());
    }
}
