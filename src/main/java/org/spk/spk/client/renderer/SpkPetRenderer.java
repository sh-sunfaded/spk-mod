package org.spk.spk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spk.spk.Spk;
import org.spk.spk.client.model.SpkPetModel;
import org.spk.spk.entity.SpkPetEntity;

public class SpkPetRenderer extends MobRenderer<SpkPetEntity, SpkPetModel<SpkPetEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Spk.MODID, "textures/entity/spk_pet.png");

    public SpkPetRenderer(EntityRendererProvider.Context context) {
        super(context, new SpkPetModel<>(context.bakeLayer(SpkPetModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    protected void setupRotations(SpkPetEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (entity.isRainbowSpinning()) {
            // 虹色スピン演出：Y軸高速回転（1秒間に2回転）
            float spinAngle = (entity.tickCount + partialTicks) * 36.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(spinAngle));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(SpkPetEntity entity) {
        return TEXTURE;
    }
}
