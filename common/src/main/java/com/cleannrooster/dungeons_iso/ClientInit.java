package com.cleannrooster.dungeons_iso;


import com.cleannrooster.dungeons_iso.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.glfw.GLFW;

public class ClientInit {
    public static ClientInit instance;

    public static KeyBinding toggleBinding;
    public static KeyBinding isoBinding;
    public static KeyBinding verticalBinding;
    public static KeyBinding lockOn;
    public static KeyBinding clickToMove;
    public static KeyBinding moveCameraBinding;
    public static KeyBinding zoomInBinding;
    public static KeyBinding zoomOutBinding;
    public static KeyBinding rotateToggle;
    public static KeyBinding cycleTargetBinding;
    public static KeyBinding openLootMenu;
    public static KeyBinding rotateCounterClockwise;
    public static KeyBinding rotateClockwise;
    public static KeyBinding interact;
    public static KeyBinding contextToggleBinding;
    public static KeyBinding cullDebugBinding;

    /**
     * Creates all KeyBinding objects.
     * Called during NeoForge's RegisterKeyMappingsEvent and from Fabric's onInitializeClient.
     * Does NOT call any platform-specific registration API — see getAllKeyBindings().
     */
    /**
     * Shown in the controls screen as {@code key.category.dungeons_iso.binds}. Each loader registers
     * it its own way (vanilla's list on Fabric, RegisterKeyMappingsEvent on NeoForge), so it is
     * only constructed here.
     */
    public static final KeyBinding.Category CATEGORY =
            new KeyBinding.Category(Identifier.of("dungeons_iso", "binds"));

    public static void registerKeyBindings() {
        // Unbound by default: a support tool, not a feature, and claiming a key in a modpack
        // invites a clash. Still listed in the controls screen for anyone who wants it.
        cullDebugBinding = new KeyBinding(
                "dungeons_iso.binds.cullDebug",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                CATEGORY
        );
        toggleBinding = new KeyBinding(
                "dungeons_iso.binds.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F4,
                CATEGORY
        );
        isoBinding = new KeyBinding(
                "dungeons_iso.binds.iso",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_HOME,
                CATEGORY
        );
        moveCameraBinding = new KeyBinding(
                "dungeons_iso.binds.moveCamera",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_3,
                CATEGORY
        );
        lockOn = new KeyBinding(
                "dungeons_iso.binds.lockOn",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
        );
        verticalBinding = new KeyBinding(
                "dungeons_iso.binds.verticalBinding",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                CATEGORY
        );
        clickToMove = new KeyBinding(
                "dungeons_iso.binds.clickToMove",
                InputUtil.Type.MOUSE,
                InputUtil.UNKNOWN_KEY.getCode(),
                CATEGORY
        );
        zoomInBinding = new KeyBinding(
                "dungeons_iso.binds.zoomIn",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_KEY_UP,
                CATEGORY
        );
        zoomOutBinding = new KeyBinding(
                "dungeons_iso.binds.zoomOut",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_KEY_DOWN,
                CATEGORY
        );
        rotateToggle = new KeyBinding(
                "dungeons_iso.binds.rotateToggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DELETE,
                CATEGORY
        );
        rotateClockwise = new KeyBinding(
                "dungeons_iso.binds.rotateClockwise",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                CATEGORY
        );
        interact = new KeyBinding(
                "dungeons_iso.binds.interact",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        );
        rotateCounterClockwise = new KeyBinding(
                "dungeons_iso.binds.rotateCounterClockwise",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                CATEGORY
        );
        contextToggleBinding = new KeyBinding(
                "dungeons_iso.binds.dynCameraToggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_END,
                CATEGORY
        );
        cycleTargetBinding = new KeyBinding(
                "dungeons_iso.binds.cycleTargetBinding",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_4,
                CATEGORY
        );
        openLootMenu = new KeyBinding(
                "dungeons_iso.binds.openLootMenu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        );
    }

    /**
     * Returns all key bindings so platform modules can register them.
     * Fabric: KeyBindingHelper.registerKeyBinding(). NeoForge: event.register().
     */
    public static KeyBinding[] getAllKeyBindings() {
        return new KeyBinding[]{
                toggleBinding, isoBinding, moveCameraBinding, lockOn, verticalBinding,
                clickToMove, zoomInBinding, zoomOutBinding, rotateToggle, rotateClockwise,
                interact, rotateCounterClockwise, contextToggleBinding, cycleTargetBinding,
                openLootMenu, cullDebugBinding
        };
    }

    /**
     * Called during client setup (after key bindings are registered).
     */
    public static void init() {
        instance = new ClientInit();
        Config.GSON.load();
        com.cleannrooster.dungeons_iso.config.FirstTimeState.load();
        // Must happen on the client thread, before any world loads. Chunk meshing runs on a worker
        // pool, and a worker must never be the first thread to load these classes — see
        // TerrainCulling.warmUp().
        com.cleannrooster.dungeons_iso.api.cullers.room.TerrainCulling.warmUp();
    }

    public static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
        VertexRendering.drawOutline(matrices, vertexConsumer, shape, offsetX, offsetY, offsetZ,
                ColorHelper.fromFloats(alpha, red, green, blue),
                MinecraftClient.getInstance().getWindow().getMinimumLineWidth());
    }
}
