package com.arabicclient;

import com.arabicclient.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.text.Text;

/**
 * المسؤول عن تطبيق/إلغاء "وضع تحسين الأداء" على إعدادات اللعبة الرسمية (GameOptions).
 *
 * ملحوظة للمطور: أسماء الـ getters هنا (زي getCloudRenderMode أو getEntityDistanceScaling)
 * مبنية على Yarn mappings لإصدار 1.21.1. لو ظهر compile error بعد ما تعمل import
 * للمشروع في IntelliJ/الـ IDE، افتح GameOptions.java من الـ sources المولّدة (Loom genSources)
 * وشوف الاسم الدقيق لكل accessor - أحيانًا بيتغير بسيط بين نسخ الـ mappings.
 */
public final class PerformanceManager {

    private static boolean applied = false;

    // قيم الإعدادات الأصلية قبل التفعيل، عشان نرجّعها زي ما كانت عند الإلغاء
    private static CloudRenderMode originalClouds;
    private static ParticlesMode originalParticles;
    private static GraphicsMode originalGraphics;
    private static double originalEntityDistance;

    private PerformanceManager() {
    }

    public static void toggle(ModConfig config) {
        if (applied) {
            disable(config);
        } else {
            enable(config);
        }
    }

    public static void enable(ModConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;

        // خزّن القيم الأصلية أول مرة بس
        originalClouds = options.getCloudRenderMode().getValue();
        originalParticles = options.getParticles().getValue();
        originalGraphics = options.getGraphicsMode().getValue();
        originalEntityDistance = options.getEntityDistanceScaling().getValue();

        if (config.disableClouds) {
            options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        }

        if (config.reduceParticles) {
            options.getParticles().setValue(ParticlesMode.MINIMAL);
        }

        if (config.forceFastGraphics) {
            options.getGraphicsMode().setValue(GraphicsMode.FAST);
        }

        if (config.reduceEntityDistance) {
            double clamped = Math.max(0.5, Math.min(1.0, config.entityDistanceScale));
            options.getEntityDistanceScaling().setValue(clamped);
        }

        applied = true;
        config.performanceModeEnabled = true;
        config.save();

        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§6[Arabic Client] §aتم تفعيل وضع تحسين الأداء"), true);
        }
    }

    public static void disable(ModConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;

        if (originalClouds != null) {
            options.getCloudRenderMode().setValue(originalClouds);
        }
        if (originalParticles != null) {
            options.getParticles().setValue(originalParticles);
        }
        if (originalGraphics != null) {
            options.getGraphicsMode().setValue(originalGraphics);
        }
        options.getEntityDistanceScaling().setValue(originalEntityDistance);

        applied = false;
        config.performanceModeEnabled = false;
        config.save();

        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§6[Arabic Client] §cتم إلغاء وضع تحسين الأداء"), true);
        }
    }

    public static boolean isApplied() {
        return applied;
    }
}
