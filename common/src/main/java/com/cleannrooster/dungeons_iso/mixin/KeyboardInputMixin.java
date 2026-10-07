package com.cleannrooster.dungeons_iso.mixin;

import com.cleannrooster.dungeons_iso.ClientInit;
import com.cleannrooster.dungeons_iso.ModCompat;
import com.cleannrooster.dungeons_iso.api.MinecraftClientAccessor;
import com.cleannrooster.dungeons_iso.compat.MidnightControlsCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix2f;
import org.joml.Vector2f;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.cleannrooster.dungeons_iso.config.Config;
import com.cleannrooster.dungeons_iso.mod.Mod;
import com.cleannrooster.dungeons_iso.util.JoystickInput;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    // 1.21.2+ keeps movement as a single vector (x = sideways, y = forward) and applies
    // sneak/use-item slowdown later in ClientPlayerEntity, so the logic below works on these two
    // components and writes them back once at the end.
    @Unique
    private float movementForward;
    @Unique
    private float movementSideways;

    @Inject(
            method = "tick", at = @At("TAIL")
    )
    private void movementXIV(CallbackInfo ci) {
        this.movementForward = this.movementVector.y;
        this.movementSideways = this.movementVector.x;
        movementBodyXIV();
        this.movementVector = new Vec2f(this.movementSideways, this.movementForward);
        if(this.playerInput.backward() || this.playerInput.forward() || this.playerInput.left() || this.playerInput.right()
                || Mod.joystickRawFwd != 0 || Mod.joystickRawSide != 0){
            Mod.useTimer = 0;
        }
        else{
            Mod.useTimer++;
        }
    }

    @Unique
    private void movementBodyXIV() {
        if (Mod.enabled ) {
            if(ModCompat.isModLoaded("midnightcontrols") && Mod.noMouse && Mod.useTimer > 40){
                if(MidnightControlsCompat.isEnabled()){
                    return;
                }
            }
            MinecraftClient client = MinecraftClient.getInstance();
            assert client.player != null;

            // Native left-stick movement: when a controller is connected and the stick is past
            // the deadzone it overrides the keyboard for this tick, feeding the same
            // movementForward/movementSideways the camera-relative rotation below already handles.
            // GLFW reads the stick straight from hardware, so — unlike keyboard input — it is NOT
            // suppressed while a screen (inventory, chat, pause menu, ...) is open; gate on
            // currentScreen == null so those menus still stop movement as they normally would.
            Vector2f joystick = (Config.GSON.instance().isJoystickMovement() && client.currentScreen == null)
                    ? JoystickInput.readLeftStick() : null;
            if (joystick != null) {
                Mod.joystickRawFwd = joystick.x;
                Mod.joystickRawSide = joystick.y;
                this.playerInput = new PlayerInput(
                        joystick.x > 0.2F,
                        joystick.x < -0.2F,
                        joystick.y > 0.2F,
                        joystick.y < -0.2F,
                        this.playerInput.jump(),
                        this.playerInput.sneak(),
                        this.playerInput.sprint());
                this.movementForward  = joystick.x;
                this.movementSideways = joystick.y;
            } else {
                Mod.joystickRawFwd = 0F;
                Mod.joystickRawSide = 0F;
            }

            Vector2f movement = new Vector2f(this.movementForward, this.movementSideways);

            float tickDelta = client.gameRenderer.getCamera().getLastTickProgress();

            boolean bool = ((MinecraftClientAccessor)client).getLocation() instanceof EntityHitResult result && result.getEntity() instanceof ItemEntity;
            // Cancel the tick-aligned player yaw (the same value vanilla's updateVelocity rotates the
            // movement by), NOT the render-interpolated getYaw(tickDelta). While aiming/using and
            // strafing, lookAt() changes the player yaw every tick; using the interpolated yaw here
            // leaves a tickDelta-dependent residual, so the strafe direction (and thus the camera,
            // cursor and cursor-facing) wobbles in a feedback loop. Using the tick yaw cancels
            // vanilla's rotation exactly, keeping movement precisely camera-relative.
            float yaw = client.gameRenderer.getCamera().getYaw() - client.player.getYaw();
            if((Config.GSON.instance().isClickToMove() || bool) &&  ((MinecraftClientAccessor)client).getOriginalLocation() != null  && ((MinecraftClientAccessor)client).getLocation() != null &&((MinecraftClientAccessor)client).getLocation().getPos() instanceof Vec3d vec3d
                    && client.player.squaredDistanceTo(((MinecraftClientAccessor)client).getOriginalLocation()) < (((MinecraftClientAccessor)client).getOriginalLocation()).squaredDistanceTo(((MinecraftClientAccessor)client).getLocation().getPos())-1) {
                   if(((MinecraftClientAccessor)client).getLocation() instanceof EntityHitResult && ((MinecraftClientAccessor)client).getLocation().getPos().subtract(0,((MinecraftClientAccessor)client).getLocation().getPos().getY()-(client.player.getEntityPos()).getY(),0)
                           .squaredDistanceTo(client.player.getEntityPos()) < (bool ? client.player.getWidth()/2 :(client.player.getEntityInteractionRange() * client.player.getEntityInteractionRange()/4))){
                       return;
                   }
                if(((MinecraftClientAccessor)client).getLocation() instanceof BlockHitResult result && Mod.isInteractable(result)){
                    Hand[] var1 = Hand.values();
                    for (Hand hand : var1) {
                        var interact = client.interactionManager.interactBlock(client.player,Hand.MAIN_HAND, result);

                        if (interact.isAccepted()) {
                            if (Mod.shouldSwingHand(interact)) {
                                client.player.swingHand(hand);
                            }

                            Mod.crosshairTarget = null;
                            ((MinecraftClientAccessor)client).setLocation(null);

                            ((MinecraftClientAccessor)client).setOriginalLocation(null);
                            ClientInit.clickToMove.setPressed(false);
                            return;
                        }
                    }
                    return;
                }
                    movement = new Vector2f(1.0F, 0F);
                   yaw = getAngle(new Vec3d(0, 0, 0), vec3d.subtract(client.player.getEntityPos()).subtract(0, vec3d.subtract(client.player.getEntityPos()).getY(), 0));

                   movement.mul(new Matrix2f().rotate((float) Math.toRadians(yaw)));
                   movement.mul(new Matrix2f().rotate((float) Math.toRadians(+client.player.getYaw(tickDelta))));
                   Mod.unModMovement = new Vector2f(1.0F,0F).mul(new Matrix2f().rotate((float) Math.toRadians(+MinecraftClient.getInstance().gameRenderer.getCamera().getYaw())));

                   this.movementForward = movement.x;
                   this.movementSideways = movement.y;

                return;

            }
            else{
                movementForward=0;
                movementSideways=0;
            }
            if(Config.GSON.instance().cameraRelative) {
                Mod.relativeYaw = yaw;
                movement.mul(new Matrix2f().rotate((float) Math.toRadians(-yaw)));
            }


            this.movementForward = movement.x;
            this.movementSideways = movement.y;

        }
    }
    public float getAngle(Vec3d start, Vec3d target) {
        return (float) Math.toDegrees(Math.atan2(target.x - start.x, target.z - start.z));
    }

}
