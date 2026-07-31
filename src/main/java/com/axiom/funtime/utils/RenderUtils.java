package com.axiom.funtime.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void drawLabel(BlockPos pos, String text, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        MatrixStack matrices = new MatrixStack();
        matrices.translate(pos.getX() + 0.5 - cam.x, pos.getY() + 1.0 - cam.y, pos.getZ() + 0.5 - cam.z);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        mc.textRenderer.draw(text, 0, 0, color, false, matrix4f, immediate, net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        immediate.draw();
    }
}
