package com.arabicclient.mixin;

import com.arabicclient.ArabicClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * بيضيف سطر بسيط "Arabic Client vX.X.X" في أول قايمة نصوص شاشة الديباج (F3).
 *
 * ملحوظة: اسم الـ method (getLeftText) وتوقيعه ممكن يختلف شوية حسب نسخة الـ
 * mappings. لو حصل compile error هنا، افتح DebugHud.java من generated sources
 * ودور على الـ method اللي بترجع List<String> للنصوص الشمال في F3.
 */
@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void arabicclient$addBrandingLine(CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = cir.getReturnValue();
        lines.add("");
        lines.add("§6Arabic Client §7v" + getModVersion());
        cir.setReturnValue(lines);
    }

    private String getModVersion() {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer(ArabicClient.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
    }
}
