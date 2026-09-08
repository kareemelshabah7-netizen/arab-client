package com.arabicclient.hud;

import com.arabicclient.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Overlay الـ Keystrokes: بيعرض حالة أزرار W/A/S/D + زرار الماوس الشمال/اليمين
 * + عداد CPS (Clicks Per Second) لزرار الماوس الشمال، زي أي FPS client معروف.
 *
 * بيتسجل عن طريق Fabric API's HudRenderCallback، فمش محتاج mixin خالص.
 */
public final class KeystrokesHud {

    private static final int BOX_SIZE = 20;
    private static final int GAP = 2;

    private static final int COLOR_IDLE = 0x88222222;
    private static final int COLOR_PRESSED = 0xFF4CAF50; // أخضر لما الزرار يكون مضغوط
    private static final int COLOR_BORDER = 0xFFFFFFFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private static final Deque<Long> leftClickTimestamps = new ArrayDeque<>();
    private static boolean wasLeftPressed = false;

    private KeystrokesHud() {
    }

    public static void register(ModConfig config) {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context, config));
    }

    private static void render(DrawContext context, ModConfig config) {
        if (!config.keystrokesEnabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options == null) {
            return;
        }

        int baseX = config.keystrokesX;
        int baseY = config.keystrokesY;

        // ==== صف W في النص فوق ====
        drawKey(context, client, baseX + BOX_SIZE + GAP, baseY, "W", client.options.forwardKey);

        // ==== صف A S D تحت بعض ====
        int row2Y = baseY + BOX_SIZE + GAP;
        drawKey(context, client, baseX, row2Y, "A", client.options.leftKey);
        drawKey(context, client, baseX + BOX_SIZE + GAP, row2Y, "S", client.options.backKey);
        drawKey(context, client, baseX + (BOX_SIZE + GAP) * 2, row2Y, "D", client.options.rightKey);

        // ==== زرار المسافة (Jump) تحت WASD بعرض أكبر ====
        int row3Y = row2Y + BOX_SIZE + GAP;
        boolean jumpPressed = client.options.jumpKey.isPressed();
        drawBox(context, baseX, row3Y, (BOX_SIZE * 3) + (GAP * 2), BOX_SIZE, jumpPressed);
        drawCenteredText(context, client, "SPACE", baseX, row3Y, (BOX_SIZE * 3) + (GAP * 2), BOX_SIZE);

        // ==== أزرار الماوس + عداد CPS جنب الصندوق ====
        long windowHandle = client.getWindow().getHandle();
        boolean leftPressed = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightPressed = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        updateCps(leftPressed);

        int mouseX = baseX + (BOX_SIZE + GAP) * 3 + 10;
        drawBox(context, mouseX, baseY, BOX_SIZE, BOX_SIZE, leftPressed);
        drawCenteredText(context, client, "LMB", mouseX, baseY, BOX_SIZE, BOX_SIZE);

        drawBox(context, mouseX, baseY + BOX_SIZE + GAP, BOX_SIZE, BOX_SIZE, rightPressed);
        drawCenteredText(context, client, "RMB", mouseX, baseY + BOX_SIZE + GAP, BOX_SIZE, BOX_SIZE);

        int cps = getCps();
        context.drawText(client.textRenderer, Text.literal(cps + " CPS"),
                mouseX, baseY + (BOX_SIZE + GAP) * 2 + 2, COLOR_TEXT, true);
    }

    private static void drawKey(DrawContext context, MinecraftClient client, int x, int y, String label, KeyBinding key) {
        boolean pressed = key.isPressed();
        drawBox(context, x, y, BOX_SIZE, BOX_SIZE, pressed);
        drawCenteredText(context, client, label, x, y, BOX_SIZE, BOX_SIZE);
    }

    private static void drawBox(DrawContext context, int x, int y, int width, int height, boolean pressed) {
        context.fill(x, y, x + width, y + height, pressed ? COLOR_PRESSED : COLOR_IDLE);
        // إطار بسيط حوالين الصندوق
        context.fill(x, y, x + width, y + 1, COLOR_BORDER);
        context.fill(x, y + height - 1, x + width, y + height, COLOR_BORDER);
        context.fill(x, y, x + 1, y + height, COLOR_BORDER);
        context.fill(x + width - 1, y, x + width, y + height, COLOR_BORDER);
    }

    private static void drawCenteredText(DrawContext context, MinecraftClient client, String label,
                                          int x, int y, int width, int height) {
        int textWidth = client.textRenderer.getWidth(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - client.textRenderer.fontHeight) / 2;
        context.drawText(client.textRenderer, Text.literal(label), textX, textY, COLOR_TEXT, true);
    }

    private static void updateCps(boolean leftPressed) {
        long now = System.currentTimeMillis();

        // نسجل ضغطة جديدة بس لما الزرار يتحول من مش-مضغوط لمضغوط (rising edge)
        if (leftPressed && !wasLeftPressed) {
            leftClickTimestamps.addLast(now);
        }
        wasLeftPressed = leftPressed;

        while (!leftClickTimestamps.isEmpty() && now - leftClickTimestamps.peekFirst() > 1000) {
            leftClickTimestamps.pollFirst();
        }
    }

    private static int getCps() {
        return leftClickTimestamps.size();
    }
}
