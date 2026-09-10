package br.com.ikezn.luckycobblemon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuckyVariantResourcesTest {
    private static final List<String> VARIANTS = List.of(
        "rare_lucky_block", "legendary_lucky_block", "cursed_lucky_block"
    );
    private static final List<String> NEW_EVENT_KEYS = List.of(
        "apricorn_harvest", "berry_picnic", "evolution_cache", "medicine_kit", "mineral_cache", "fossil_cache",
        "common_swarm", "rare_duo", "starter_parade", "crystal_altar", "healing_garden",
        "unlucky_thunder", "unlucky_web", "unlucky_hunger", "unlucky_fake_gold"
    );

    @Test
    void everyVariantHasBlockstateModelsTextureAndRecipe() {
        for (String variant : VARIANTS) {
            assertResource("assets/luckycobblemon/blockstates/" + variant + ".json");
            assertResource("assets/luckycobblemon/models/block/" + variant + ".json");
            assertResource("assets/luckycobblemon/models/item/" + variant + ".json");
            assertResource("assets/luckycobblemon/textures/block/" + variant + ".png");
            assertResource("data/luckycobblemon/recipe/" + variant + ".json");
        }
    }

    @Test
    void bothLanguagesContainEveryNewEvent() {
        for (String language : List.of("pt_br", "en_us")) {
            JsonObject translations = json("assets/luckycobblemon/lang/" + language + ".json");
            for (String event : NEW_EVENT_KEYS) {
                assertTrue(translations.has("message.luckycobblemon." + event),
                    () -> language + " is missing event translation " + event);
            }
        }
    }

    private static JsonObject json(String path) {
        var stream = LuckyVariantResourcesTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, () -> "Missing resource " + path);
        return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void assertResource(String path) {
        assertNotNull(LuckyVariantResourcesTest.class.getClassLoader().getResource(path),
            () -> "Missing resource " + path);
    }
}
