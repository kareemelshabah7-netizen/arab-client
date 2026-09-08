package com.arabicclient;

import com.arabicclient.badge.BadgeManager;
import com.arabicclient.config.ModConfig;
import com.arabicclient.gui.ArabicClientMenuScreen;
import com.arabicclient.hud.KeystrokesHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * نقطة دخول الـ client الخاصة بـ Arabic Client.
 * هنا بيتسجل الزرار (Keybinding) اللي بيفتح شاشة الإعدادات، وبنراقب الضغط عليه كل تيك.
 */
public class ArabicClientMod implements ClientModInitializer {

    private static ModConfig config;

    // الزرار الافتراضي لفتح إعدادات Arabic Client: الحرف "K"
    // المستخدم يقدر يغيّره من Options > Controls > Key Binds > Arabic Client
    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load();

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.arabicclient.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.arabicclient.main"
        ));

        // لو كان وضع تحسين الأداء كان مفعّل من آخر مرة، رجّعه تلقائيًا عند تشغيل اللعبة
        if (config.performanceModeEnabled) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.player != null && !PerformanceManager.isApplied()) {
                    PerformanceManager.enable(config);
                }
            });
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ArabicClientMenuScreen(null, config));
                }
            }
        });

        // موديول الـ Keystrokes (WASD + كليكات الماوس + CPS)
        KeystrokesHud.register(config);

        // تحميل قايمة الشارات (لو المستخدم حاطط رابط في الإعدادات)
        BadgeManager.refresh(config);

        ArabicClient.LOGGER.info("[Arabic Client] تم تحميل موديول الـ client");
    }

    public static ModConfig getConfig() {
        return config;
    }
}
