package com.arabicclient.mixin;

import com.arabicclient.ArabicClient;
import com.arabicclient.ArabicClientMod;
import com.arabicclient.gui.ArabicClientMenuScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * شاشة رئيسية مخصصة بالكامل لـ Arabic Client - بتستبدل شاشة ماين كرافت
 * الافتراضية (Panorama + شعار الحروف المكعبة) بهوية عربية: خلفية متدرجة
 * أسود/عنّابي-ذهبي، نقشة هندسية خفيفة، شعار الطربوش، وأزرار منسّقة.
 *
 * الفكرة التقنية: بنحقن جوه TitleScreen نفسه (مش هنعمل كلاس منفصل) عشان
 * نضمن إنها هي اللي بتظهر فعليًا أول ما اللعبة تفتح، من غير ما نحتاج نلاقي
 * ونعدّل كل مكان بينادي على `new TitleScreen()` في كود اللعبة.
 *
 * ملحوظة: زي باقي الـ mixins، أسماء الكلاسات (MultiplayerScreen, SelectWorldScreen,
 * OptionsScreen) وتوقيعاتها اتكتبت حسب Yarn mappings 1.21.1. لو اختلفت شوية،
 * افتح الكلاس المعني من generated sources ودور على الـ constructor الصحيح.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    // ألوان الهوية: أسود عميق -> عنّابي غامق -> لمسة ذهبية للتفاصيل
    private static final int COLOR_TOP = 0xFF0D0A08;
    private static final int COLOR_BOTTOM = 0xFF2A120A;
    private static final int COLOR_GOLD = 0xFFD4AF37;
    private static final int COLOR_GOLD_DIM = 0x33D4AF37;

    private static final Identifier LOGO_TEXTURE =
            Identifier.of("arabicclient", "textures/gui/icon.png");

    private static final String TAGLINE = "أسرع، أوضح، أعرب";

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void arabicclient$customInit(CallbackInfo ci) {
        this.clearChildren();

        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;
        int startY = this.height / 2 + 10;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("اللعب المنفرد"), b ->
                        this.client.setScreen(new SelectWorldScreen((Screen) (Object) this)))
                .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("متعدد اللاعبين"), b ->
                        this.client.setScreen(new MultiplayerScreen((Screen) (Object) this)))
                .dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6Arabic Client §r▸ الإعدادات"), b ->
                        this.client.setScreen(new ArabicClientMenuScreen(
                                (Screen) (Object) this, ArabicClientMod.getConfig())))
                .dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("الإعدادات العامة"), b ->
                        this.client.setScreen(new OptionsScreen(
                                (Screen) (Object) this, this.client.options)))
                .dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("خروج"), b ->
                        this.client.scheduleStop())
                .dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight)
                .build());

        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void arabicclient$customRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        arabicclient$drawBackground(context);
        arabicclient$drawPattern(context);
        arabicclient$drawLogo(context);
        arabicclient$drawFooter(context);

        // بيرسم الأزرار (widgets) اللي سجلناها في init() فوق الخلفية بتاعتنا
        super.render(context, mouseX, mouseY, delta);

        ci.cancel();
    }

    private void arabicclient$drawBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, COLOR_TOP, COLOR_BOTTOM);

        // خط ذهبي رفيع أسفل الشعار كفاصل بصري
        int lineY = this.height / 2 - 30;
        context.fill(this.width / 2 - 80, lineY, this.width / 2 + 80, lineY + 1, COLOR_GOLD_DIM);
    }

    /** نقشة هندسية خفيفة (معينات صغيرة متكررة) تعطي إحساس زخرفة عربية من غير أي صورة خارجية */
    private void arabicclient$drawPattern(DrawContext context) {
        int step = 48;
        for (int x = -step; x < this.width + step; x += step) {
            for (int y = -step; y < this.height + step; y += step) {
                int cx = x + (((y / step) % 2 == 0) ? 0 : step / 2);
                arabicclient$drawDiamond(context, cx, y, 3, COLOR_GOLD_DIM);
            }
        }
    }

    private void arabicclient$drawDiamond(DrawContext context, int cx, int cy, int r, int color) {
        for (int i = 0; i <= r; i++) {
            context.fill(cx - i, cy - r + i, cx - i + 1, cy - r + i + 1, color);
            context.fill(cx + i, cy - r + i, cx + i + 1, cy - r + i + 1, color);
            context.fill(cx - i, cy + r - i, cx - i + 1, cy + r - i + 1, color);
            context.fill(cx + i, cy + r - i, cx + i + 1, cy + r - i + 1, color);
        }
    }

    private void arabicclient$drawLogo(DrawContext context) {
        int centerX = this.width / 2;
        int logoSize = 64;
        int logoY = this.height / 2 - 110;

        context.drawTexture(LOGO_TEXTURE, centerX - logoSize / 2, logoY, 0, 0,
                logoSize, logoSize, logoSize, logoSize);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§6§lArabic Client"), centerX, logoY + logoSize + 8, COLOR_GOLD);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7" + TAGLINE), centerX, logoY + logoSize + 20, 0xAAAAAA);
    }

    private void arabicclient$drawFooter(DrawContext context) {
        String modVersion = FabricLoader.getInstance()
                .getModContainer(ArabicClient.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");

        String footer = "Arabic Client v" + modVersion + "  |  Minecraft "
                + SharedConstants.getGameVersion().name();

        context.drawTextWithShadow(this.textRenderer, Text.literal(footer), 4, this.height - 14, 0x888888);
    }
}
