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
import java.util.List;

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
        private final List<ItemStack> variants = List.of(
            new ItemStack(LuckyCobblemonMod.LUCKY_BLOCK_ITEM),
            new ItemStack(LuckyCobblemonMod.RARE_LUCKY_BLOCK_ITEM),
            new ItemStack(LuckyCobblemonMod.LEGENDARY_LUCKY_BLOCK_ITEM),
            new ItemStack(LuckyCobblemonMod.CURSED_LUCKY_BLOCK_ITEM)
        );
        private int ticks;
        private int saved;

        private PreviewScreen() {
            super(Text.literal("Lucky Cobblemon 0.6 visual check"));
        }

        @Override
        protected void init() {
            for (ItemStack stack : variants) {
                var model = client.getItemRenderer().getModel(stack, null, null, 0);
                var quads = model.getQuads(null, null, Random.create(0));
                if (quads.size() < 50 || quads.stream().anyMatch(quad ->
                    quad.getSprite().getContents().getId().getPath().equals("missingno"))) {
                    throw new IllegalStateException(stack.getName().getString() + " mesh or textures did not bake correctly");
                }
                System.out.println("LUCKY_VISUAL_MODEL_OK item=" + stack.getItem() + " quads=" + quads.size());
            }
        }

        @Override
        public void tick() {
            ticks++;
            if (ticks > 140) client.scheduleStop();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0xff15212c);
            context.drawCenteredTextWithShadow(textRenderer, "LUCKY COBBLEMON 0.6 / FOUR VARIANTS", width / 2, 12, 0xffeee6ce);
            int[][] positions = {
                {width / 4, height / 4 + 18},
                {width * 3 / 4, height / 4 + 18},
                {width / 4, height * 3 / 4 - 18},
                {width * 3 / 4, height * 3 / 4 - 18}
            };
            for (int index = 0; index < variants.size(); index++) {
                ItemStack stack = variants.get(index);
                int x = positions[index][0];
                int y = positions[index][1];
                context.getMatrices().push();
                context.getMatrices().translate(x - 40, y - 40, 0);
                context.getMatrices().scale(5, 5, 5);
                context.drawItem(stack, 0, 0);
                context.getMatrices().pop();
                context.drawCenteredTextWithShadow(textRenderer, stack.getName(), x, y + 58, 0xffd9e4e7);
                context.drawCenteredTextWithShadow(textRenderer, "Luck " + LuckyBlockItem.getLuck(stack), x, y + 70, 0xffb7c7cb);
            }
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
