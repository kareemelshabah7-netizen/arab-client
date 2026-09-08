package com.arabicclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * إعدادات موديول تحسين الأداء (FPS Boost).
 * كل إعداد ليه قيمة افتراضية آمنة، ويتم حفظه في:
 * .minecraft/config/arabicclient.json
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("arabicclient.json");

    /** تفعيل موديول تحسين الأداء بشكل عام */
    public boolean performanceModeEnabled = false;

    /** إلغاء رسم الغيوم (Clouds) لتوفير FPS ملحوظ في الأماكن المفتوحة */
    public boolean disableClouds = true;

    /** تقليل عدد الجسيمات (Particles) المرسومة على الشاشة */
    public boolean reduceParticles = true;

    /** تقليل مسافة رؤية الكيانات (Entities) البعيدة نسبيًا لتخفيف الحمل */
    public boolean reduceEntityDistance = true;

    /** نسبة تقليل مسافة رؤية الكيانات (0.5 = النص) لما reduceEntityDistance = true */
    public double entityDistanceScale = 0.75;

    /** تحويل وضع الرسومات (Graphics) إلى "Fast" بدل "Fancy" أثناء وضع الأداء */
    public boolean forceFastGraphics = true;

    /** إيقاف smooth lighting (بيوفر شوية FPS خصوصًا في الأجهزة الضعيفة) */
    public boolean disableSmoothLighting = false;

    // ==================== Keystrokes ====================

    /** إظهار overlay الـ Keystrokes (WASD + كليكات الماوس + عداد CPS) */
    public boolean keystrokesEnabled = false;

    /** إحداثيات الـ overlay على الشاشة (زاوية علوية شمال الصندوق) */
    public int keystrokesX = 10;
    public int keystrokesY = 10;

    /** حجم الـ overlay (مقياس عام، 1.0 = الحجم الافتراضي) */
    public double keystrokesScale = 1.0;

    // ==================== Badge (الشارة جنب الاسم) ====================

    /** إظهار شارة Arabic Client جنب اسمك في Tab list */
    public boolean badgeEnabled = true;

    /**
     * رابط JSON اختياري يرجّع Array من UUIDs (بدون شرطات) بتاعة أعضاء موثّقين
     * تظهر لهم الشارة عند أي حد تاني عنده المود، مثال: ["069a79f4..."]
     * سيبه فاضي لو مش عايز شارات لغير نفسك.
     */
    public String badgeListUrl = "";

    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                // لو حصل خطأ في القراءة، هنرجع للإعدادات الافتراضية ونعيد الكتابة
            }
        }
        ModConfig defaultConfig = new ModConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            com.arabicclient.ArabicClient.LOGGER.error("[Arabic Client] فشل حفظ ملف الإعدادات", e);
        }
    }
}
