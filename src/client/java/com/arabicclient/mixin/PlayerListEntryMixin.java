package com.arabicclient.mixin;

import com.arabicclient.ArabicClientMod;
import com.arabicclient.badge.BadgeManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * بيضيف رمز الشارة (glyph من خط arabicclient:icons) في آخر اسم أي لاعب
 * عنده شارة Arabic Client، وده بيظهر تلقائيًا في Tab list (وفي أي مكان تاني
 * بيستخدم getDisplayName() زي الـ nameplate فوق الراس).
 *
 * ملحوظة: لو الـ method name اختلف شوية عن getDisplayName() في الـ mappings
 * بتاعتك، افتح PlayerListEntry.java من الـ generated sources ودور على أقرب method
 * بترجع Text وبتمثل الاسم المعروض.
 */
@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    private static final Identifier BADGE_FONT = Identifier.of("arabicclient", "icons");
    private static final String BADGE_GLYPH = "\uE001";

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void arabicclient$appendBadge(CallbackInfoReturnable<Text> cir) {
        if (!ArabicClientMod.getConfig().badgeEnabled) {
            return;
        }

        PlayerListEntry self = (PlayerListEntry) (Object) this;
        GameProfile profile = self.getProfile();
        if (profile == null || profile.getId() == null) {
            return;
        }

        if (!BadgeManager.hasBadge(profile.getId())) {
            return;
        }

        Text original = cir.getReturnValue();
        Text base = (original != null) ? original : Text.literal(profile.getName());

        MutableText badge = Text.literal(" " + BADGE_GLYPH)
                .setStyle(Style.EMPTY.withFont(BADGE_FONT));

        cir.setReturnValue(base.copy().append(badge));
    }
}
