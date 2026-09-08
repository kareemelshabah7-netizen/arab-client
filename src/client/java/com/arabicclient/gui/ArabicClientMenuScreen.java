package com.arabicclient.gui;

import com.arabicclient.ArabicClient;
import com.arabicclient.PerformanceManager;
import com.arabicclient.badge.BadgeManager;
import com.arabicclient.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * منيو Arabic Client الموحّد - نفس فكرة المنيو بتاع FPS clients زي Dawn/Lunar،
 * بس بهوية عربية بالكامل. بيتفتح بزرار "K" (قابل للتغيير).
 *
 * التابات:
 *  - الأداء: نفس خيارات تحسين الـ FPS.
 *  - Keystrokes: تفعيل/تعطيل overlay الأزرار وعداد CPS.
 *  - الشارة: التحكم في شارة الاسم في Tab list + رابط قايمة الأعضاء (اختياري).
 *  - عن المود: "حوار الأيقونة" - شعار المود بحجم كبير + معلومات الإصدار.
 */
public class ArabicClientMenuScreen extends Screen {

    private enum Tab { PERFORMANCE, KEYSTROKES, BADGE, ABOUT }

    private static final Identifier ICON_TEXTURE =
            Identifier.of("arabicclient", "textures/gui/icon.png");

    private final Screen parent;
    private final ModConfig config;
    private Tab currentTab;

    private TextFieldWidget badgeUrlField;

    public ArabicClientMenuScreen(Screen parent, ModConfig config) {
        super(Text.literal("Arabic Client"));
        this.parent = parent;
        this.config = config;
        this.currentTab = Tab.PERFORMANCE;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int tabsY = this.height / 2 - 100;

        // ==== أزرار التابات فوق ====
        int tabWidth = 100;
        int startX = centerX - (tabWidth * 4) / 2;
        addTabButton(startX, tabsY, tabWidth, "الأداء", Tab.PERFORMANCE);
        addTabButton(startX + tabWidth, tabsY, tabWidth, "Keystrokes", Tab.KEYSTROKES);
        addTabButton(startX + tabWidth * 2, tabsY, tabWidth, "الشارة", Tab.BADGE);
        addTabButton(startX + tabWidth * 3, tabsY, tabWidth, "عن المود", Tab.ABOUT);

        int contentY = tabsY + 30;

        switch (currentTab) {
            case PERFORMANCE -> initPerformanceTab(centerX, contentY);
            case KEYSTROKES -> initKeystrokesTab(centerX, contentY);
            case BADGE -> initBadgeTab(centerX, contentY);
            case ABOUT -> initAboutTab(centerX, contentY);
        }

        // زرار رجوع ثابت تحت في كل التابات
        this.addDrawableChild(ButtonWidget.builder(Text.literal("رجوع"), b -> this.close())
                .dimensions(centerX - 100, this.height / 2 + 100, 200, 20)
                .build());
    }

    private void addTabButton(int x, int y, int width, String label, Tab tab) {
        boolean active = currentTab == tab;
        String prefix = active ? "§e▶ " : "";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(prefix + label), b -> {
                    this.currentTab = tab;
                    this.clearAndInit();
                })
                .dimensions(x, y, width, 20)
                .build());
    }

    // ==================== تاب الأداء ====================

    private void initPerformanceTab(int centerX, int y) {
        int spacing = 24;

        addToggle(centerX, y, "وضع تحسين الأداء", () -> config.performanceModeEnabled, v -> {
            if (v) {
                PerformanceManager.enable(config);
            } else {
                PerformanceManager.disable(config);
            }
        });

        addToggle(centerX, y + spacing, "إلغاء رسم الغيوم", () -> config.disableClouds, v -> {
            config.disableClouds = v;
            config.save();
        });

        addToggle(centerX, y + spacing * 2, "تقليل الجسيمات (Particles)", () -> config.reduceParticles, v -> {
            config.reduceParticles = v;
            config.save();
        });

        addToggle(centerX, y + spacing * 3, "تقليل مسافة رؤية الكيانات", () -> config.reduceEntityDistance, v -> {
            config.reduceEntityDistance = v;
            config.save();
        });

        addToggle(centerX, y + spacing * 4, "رسومات سريعة (Fast Graphics)", () -> config.forceFastGraphics, v -> {
            config.forceFastGraphics = v;
            config.save();
        });
    }

    // ==================== تاب Keystrokes ====================

    private void initKeystrokesTab(int centerX, int y) {
        addToggle(centerX, y, "إظهار Keystrokes Overlay", () -> config.keystrokesEnabled, v -> {
            config.keystrokesEnabled = v;
            config.save();
        });

        // ملحوظة: تخصيص الموضع بالسحب (drag-to-move) ممكن يتضاف لاحقًا،
        // حاليًا الموضع بيتحدد من قيم keystrokesX / keystrokesY في ملف الإعدادات مباشرة.
    }

    // ==================== تاب الشارة ====================

    private void initBadgeTab(int centerX, int y) {
        addToggle(centerX, y, "إظهار الشارة جنب اسمي", () -> config.badgeEnabled, v -> {
            config.badgeEnabled = v;
            config.save();
        });

        badgeUrlField = new TextFieldWidget(this.textRenderer, centerX - 100, y + 30, 200, 20,
                Text.literal("رابط قايمة الشارات"));
        badgeUrlField.setMaxLength(256);
        badgeUrlField.setText(config.badgeListUrl == null ? "" : config.badgeListUrl);
        this.addDrawableChild(badgeUrlField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("حفظ الرابط"), b -> {
                    config.badgeListUrl = badgeUrlField.getText();
                    config.save();
                    BadgeManager.refresh(config);
                })
                .dimensions(centerX - 100, y + 55, 200, 20)
                .build());
    }

    // ==================== تاب "عن المود" (حوار الأيقونة) ====================

    private void initAboutTab(int centerX, int y) {
        // مفيش widgets تفاعلية هنا، كل حاجة بترتسم في render() تحت
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, this.height / 2 - 120, 0xFFFFFF);

        if (currentTab == Tab.ABOUT) {
            renderAboutDialog(context, centerX);
        }
    }

    private void renderAboutDialog(DrawContext context, int centerX) {
        int iconSize = 64;
        int iconY = this.height / 2 - 60;

        // رسم شعار المود بحجم كبير.
        // ملحوظة: توقيع drawTexture ممكن يختلف شوية حسب نسخة الـ mappings/Minecraft
        // (في بعض النسخ بيتطلب RenderLayer إضافي). لو حصل compile error هنا،
        // شوف الـ overloads المتاحة لـ DrawContext#drawTexture في الـ IDE.
        context.drawTexture(ICON_TEXTURE, centerX - iconSize / 2, iconY, 0, 0,
                iconSize, iconSize, iconSize, iconSize);

        String version = FabricLoader.getInstance()
                .getModContainer(ArabicClient.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§6Arabic Client"), centerX, iconY + iconSize + 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("الإصدار " + version), centerX, iconY + iconSize + 22, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("مود عربي لتحسين أداء ماين كرافت"), centerX, iconY + iconSize + 38, 0xCCCCCC);
    }

    private void addToggle(int centerX, int y, String label, java.util.function.BooleanSupplier getter,
                            java.util.function.Consumer<Boolean> setter) {
        boolean current = getter.getAsBoolean();
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal(label + ": " + (current ? "§aمفعّل" : "§cمعطّل")),
                        button -> {
                            boolean newValue = !getter.getAsBoolean();
                            setter.accept(newValue);
                            button.setMessage(Text.literal(label + ": " + (newValue ? "§aمفعّل" : "§cمعطّل")));
                        })
                .dimensions(centerX - 100, y, 200, 20)
                .build());
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
