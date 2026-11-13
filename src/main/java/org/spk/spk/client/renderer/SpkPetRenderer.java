package org.spk.spk.client.renderer;

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
    public ResourceLocation getTextureLocation(SpkPetEntity entity) {
        return TEXTURE;
    }
}
