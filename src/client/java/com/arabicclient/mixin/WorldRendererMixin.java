package com.arabicclient.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * محجوز للتوسعات المستقبلية (زي حقن مباشر في مسار رسم العالم لأداء إضافي).
 * حاليًا كل تحسينات الأداء بترجع لـ GameOptions مباشرة عبر PerformanceManager
 * لأنها أكثر أمانًا وتوافقًا مع باقي المودات من التعديل المباشر على WorldRenderer.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    // فاضي دلوقتي عن قصد - مكانه محجوز لتحسينات رندرة متقدمة لاحقًا
}
