package com.arabicclient;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * نقطة الدخول العامة (common entrypoint) - Arabic Client.
 * المود ده client-only بالكامل (فيه تعديلات على العرض والأداء فقط)
 * فمفيش منطق سيرفر هنا حاليًا، لكن Fabric بيحتاج entrypoint "main" مسجل في fabric.mod.json.
 */
public class ArabicClient implements ModInitializer {
    public static final String MOD_ID = "arabicclient";
    public static final Logger LOGGER = LoggerFactory.getLogger("Arabic Client");

    @Override
    public void onInitialize() {
        LOGGER.info("[Arabic Client] تم تحميل المود بنجاح");
    }
}
