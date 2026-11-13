package org.spk.spk.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spk.spk.Spk;
import org.spk.spk.entity.SpkPetEntity;

/**
 * spk_pet.java（Blockbench 出力）を MOD パッケージへ移植したモデル。
 * 骨・cube 定義は変更しない。setupAnim のみ実装。
 */
public class SpkPetModel<T extends SpkPetEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Spk.MODID, "spk_pet"), "main");

    // ---- 骨 ----
    private final ModelPart heads;
    private final ModelPart body;
    private final ModelPart legs;
    private final ModelPart bone;        // legs の子：後ろ右（knee）
    private final ModelPart bone2;       // legs の子：後ろ左（knee）
    private final ModelPart leg_F_right; // 前右脚
    private final ModelPart leg_F_left;  // 前左脚

    public SpkPetModel(ModelPart root) {
        this.heads      = root.getChild("heads");
        this.body       = root.getChild("body");
        this.legs       = root.getChild("legs");
        this.bone       = this.legs.getChild("bone");
        this.bone2      = this.legs.getChild("bone2");
        this.leg_F_right = root.getChild("leg_F_right");
        this.leg_F_left  = root.getChild("leg_F_left");
    }

    // ---- LayerDefinition（Blockbench 出力そのまま） ----
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition heads = partdefinition.addOrReplaceChild("heads",
                CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-4.0F, -2.5F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 15).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, 19.0F, 4.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(1, 1).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, 21.0F, 1.0F));

        PartDefinition legs = partdefinition.addOrReplaceChild("legs",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, -1.0F));

        PartDefinition bone = legs.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 8).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 0.0F, 1.0F));

        PartDefinition bone2 = legs.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(18, 17).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 11).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 0.0F, 1.0F));

        PartDefinition leg_F_right = partdefinition.addOrReplaceChild("leg_F_right",
                CubeListBuilder.create()
                        .texOffs(18, 0).addBox(-0.2F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.7F, 21.3F, 2.5F));

        PartDefinition leg_F_left = partdefinition.addOrReplaceChild("leg_F_left",
                CubeListBuilder.create()
                        .texOffs(18, 3).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 21.3F, 2.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    // ---- setupAnim ----
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        // モデル全体を 180 度回転させたため、頭のヨー・ピッチの符号を反転
        this.heads.yRot = -netHeadYaw * Mth.DEG_TO_RAD;
        this.heads.xRot = -headPitch  * Mth.DEG_TO_RAD;

        // 歩行アニメーション（前脚が進行方向へ自然に振り出されるよう調整）
        // 前脚：互い違いに前後
        this.leg_F_right.xRot = -Mth.cos(limbSwing * 0.6662F)            * 1.4F * limbSwingAmount;
        this.leg_F_left.xRot  = -Mth.cos(limbSwing * 0.6662F + Mth.PI)   * 1.4F * limbSwingAmount;

        // 後脚（bone = 右, bone2 = 左）
        this.bone.xRot  = -Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.bone2.xRot = -Mth.cos(limbSwing * 0.6662F)          * 1.4F * limbSwingAmount;
    }

    // ---- renderToBuffer ----
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        heads.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg_F_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg_F_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
