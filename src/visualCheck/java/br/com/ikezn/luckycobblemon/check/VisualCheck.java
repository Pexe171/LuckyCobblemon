package br.com.ikezn.luckycobblemon.check;

import br.com.ikezn.luckycobblemon.LuckyBlockItem;
import br.com.ikezn.luckycobblemon.LuckyCobblemonMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public final class VisualCheck implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getOverlay() == null && !(client.currentScreen instanceof PreviewScreen)) {
                client.setScreen(new PreviewScreen());
            }
        });
    }

    private static final class PreviewScreen extends Screen {
        private final ItemStack neutral = new ItemStack(LuckyCobblemonMod.LUCKY_BLOCK_ITEM);
        private final ItemStack lucky = new ItemStack(LuckyCobblemonMod.LUCKY_BLOCK_ITEM);
        private int ticks;
        private int saved;

        private PreviewScreen() {
            super(Text.literal("Lucky Cobblemon visual check"));
            LuckyBlockItem.setLuck(lucky, 100);
        }

        @Override
        protected void init() {
            var model = client.getItemRenderer().getModel(neutral, null, null, 0);
            var quads = model.getQuads(null, null, Random.create(0));
            if (quads.size() < 50 || quads.stream().anyMatch(quad ->
                quad.getSprite().getContents().getId().getPath().equals("missingno"))) {
                throw new IllegalStateException("Lucky Block mesh or textures did not bake correctly");
            }
            System.out.println("LUCKY_VISUAL_MODEL_OK quads=" + quads.size());
        }

        @Override
        public void tick() {
            ticks++;
            if (ticks > 140) client.scheduleStop();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0xff15212c);
            context.drawCenteredTextWithShadow(textRenderer, "LUCKY COBBLEMON 0.4 / MINECRAFT RENDER", width / 2, 12, 0xffeee6ce);
            context.getMatrices().push();
            context.getMatrices().translate(width * .25 - 56, height * .45 - 56, 0);
            context.getMatrices().scale(7, 7, 7);
            context.drawItem(neutral, 0, 0);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().translate(width * .75 - 56, height * .45 - 56, 0);
            context.getMatrices().scale(7, 7, 7);
            context.drawItem(lucky, 0, 0);
            context.getMatrices().pop();
            context.drawCenteredTextWithShadow(textRenderer, "Sorte 0", width / 4, height - 45, 0xffb7c7cb);
            context.drawCenteredTextWithShadow(textRenderer, "Sorte +100 / brilho", width * 3 / 4, height - 45, 0xffb7e9ce);
            context.drawItem(neutral, width / 4 - 8, height - 30);
            context.drawItem(lucky, width * 3 / 4 - 8, height - 30);
            context.draw();
            if ((ticks >= 50 && saved == 0) || (ticks >= 82 && saved == 1)) {
                try (var screenshot = ScreenshotRecorder.takeScreenshot(client.getFramebuffer())) {
                    var directory = client.runDirectory.toPath().resolve("screenshots");
                    java.nio.file.Files.createDirectories(directory);
                    screenshot.writeTo(directory.resolve("lucky-render-" + (++saved) + ".png"));
                    System.out.println("LUCKY_VISUAL_SCREENSHOT_OK frame=" + saved);
                } catch (java.io.IOException error) {
                    throw new IllegalStateException("Could not save renderer check", error);
                }
            }
        }
    }
}
