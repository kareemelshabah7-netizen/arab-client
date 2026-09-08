package com.arabicclient.badge;

import com.arabicclient.ArabicClient;
import com.arabicclient.config.ModConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * بيحدد مين يستاهل يظهرله شارة Arabic Client (glyph) جنب اسمه في Tab list.
 *
 * الأساس البديهي: أي حد شغّال المود ده، المود بتاعه بيعرف إنه هو نفسه عنده المود
 * (محليًا، من غير أي سيرفر) - فبنعرض له الشارة على نفسه دايمًا.
 *
 * إظهار الشارة على *لاعبين تانيين* في نفس السيرفر يحتاج مصدر بيانات مشترك،
 * لأن الكلاينت مش بيعرف تلقائيًا مين اللاعبين التانيين اللي عندهم نفس المود.
 * هنا بنستخدم قايمة UUIDs بترجع من رابط JSON بسيط تقدر تستضيفه إنت (GitHub raw
 * مثلاً)، وده أبسط وأصدق حل من غير ما نفترض وجود سيرفر backend حقيقي شغال.
 *
 * مثال شكل الـ JSON المتوقع (array نصوص UUID بدون شرطات):
 * ["069a79f444e94726a5befca90e38aaf5", "..."]
 */
public final class BadgeManager {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Set<UUID> remoteBadgedPlayers = Collections.synchronizedSet(new HashSet<>());
    private static boolean fetched = false;

    private BadgeManager() {
    }

    /** هل اللاعب ده (بالـ UUID بتاعه) يستاهل شارة Arabic Client؟ */
    public static boolean hasBadge(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        // نفسك دايمًا بتظهرلك الشارة لو مفعّلة في الإعدادات
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && uuid.equals(client.player.getUuid())) {
            return true;
        }
        return remoteBadgedPlayers.contains(uuid);
    }

    /** يجيب قايمة الـ UUIDs من الرابط المحدد في الإعدادات (مرة واحدة، بعدين بيتخزن مؤقتًا). */
    public static void refresh(ModConfig config) {
        if (fetched || config.badgeListUrl == null || config.badgeListUrl.isBlank()) {
            return;
        }
        fetched = true;

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.badgeListUrl))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
                    Set<UUID> parsed = new HashSet<>();
                    for (var element : array) {
                        try {
                            parsed.add(parseUuid(element.getAsString()));
                        } catch (IllegalArgumentException ignored) {
                            // تجاهل أي UUID مكتوب غلط في القايمة
                        }
                    }
                    remoteBadgedPlayers.clear();
                    remoteBadgedPlayers.addAll(parsed);
                    ArabicClient.LOGGER.info(
                            "[Arabic Client] تم تحميل {} شارة من badgeListUrl", parsed.size());
                }
            } catch (IOException | InterruptedException e) {
                ArabicClient.LOGGER.warn("[Arabic Client] فشل تحميل قايمة الشارات: {}", e.getMessage());
            }
        });
    }

    /** يقبل UUID بشرطات أو من غيرها */
    private static UUID parseUuid(String raw) {
        String cleaned = raw.trim();
        if (!cleaned.contains("-") && cleaned.length() == 32) {
            cleaned = cleaned.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        }
        return UUID.fromString(cleaned);
    }
}
