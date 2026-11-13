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
    private final ModelPart all;
    private final ModelPart heads;
    private final ModelPart hair;
    private final ModelPart body;
    private final ModelPart legs;
    private final ModelPart bone;        // legs の子：後ろ右
    private final ModelPart bone2;       // legs の子：後ろ左
    private final ModelPart leg_F_right; // 前右脚
    private final ModelPart leg_F_left;  // 前左脚
    private final ModelPart bone3;

    public SpkPetModel(ModelPart root) {
        this.all         = root.getChild("all");
        this.heads       = this.all.getChild("heads");
        this.hair        = this.heads.getChild("hair");
        this.body        = this.all.getChild("body");
        this.legs        = this.all.getChild("legs");
        this.bone        = this.legs.getChild("bone");
        this.bone2       = this.legs.getChild("bone2");
        this.leg_F_right = this.all.getChild("leg_F_right");
        this.leg_F_left  = this.all.getChild("leg_F_left");
        this.bone3       = root.getChild("bone3");
    }

    // ---- LayerDefinition（Blockbench 出力そのまま） ----
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 19.0F, -2.2F, 0.0F, 3.1416F, 0.0F));

        PartDefinition heads = all.addOrReplaceChild("heads", CubeListBuilder.create().texOffs(0, 6).addBox(-4.0F, -1.5F, -2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-4.0F, 0.5F, -0.5F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 9).addBox(-4.4F, 0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = heads.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(17, 16).addBox(-2.0F, -2.0F, -3.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(17, 16).addBox(-4.8F, -2.0F, -3.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, 0.5F, -0.9F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r2 = heads.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -1.0F, -3.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-5.3F, -1.0F, -3.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -0.3F, -1.7F, 0.6545F, 0.0F, 0.0F));

        PartDefinition cube_r3 = heads.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(4, 20).addBox(-1.0F, -1.0F, -4.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, -2.2F, 0.6545F, 0.0F, 0.0F));

        PartDefinition hair = heads.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(0, 28).addBox(-4.0F, -1.8F, -1.9F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 28).addBox(-4.0F, -1.9F, -2.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-4.1F, -1.8F, -2.1F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-3.9F, -1.8F, -2.1F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(3, 3).addBox(-2.0F, -0.8F, -2.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(-2.0F, -0.3F, -2.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 2.0F, -3.0F));

        PartDefinition legs = all.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(-2.5F, 5.0F, -5.0F));

        PartDefinition bone = legs.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(14, 15).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 12).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.8F, 0.0F, 1.0F));

        PartDefinition bone2 = legs.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(16, 0).addBox(-0.4F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 6).addBox(-0.4F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.7F, 0.0F, 1.0F));

        PartDefinition leg_F_right = all.addOrReplaceChild("leg_F_right", CubeListBuilder.create().texOffs(8, 15).addBox(0.8F, -0.3F, -0.5F, -1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.8F, 2.3F, -1.5F));

        PartDefinition leg_F_left = all.addOrReplaceChild("leg_F_left", CubeListBuilder.create().texOffs(10, 15).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 2.3F, -1.5F));

        PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(3.3F, 22.1F, 3.2F));

        PartDefinition cube_r4 = bone3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(27, 1).addBox(-1.0F, -1.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r5 = bone3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(25, 1).addBox(-1.0F, -2.0F, -3.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.4F, 0.2618F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    // ---- setupAnim ----
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        // 頭の向き（バニラ標準の方向）
        this.heads.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.heads.xRot = headPitch  * Mth.DEG_TO_RAD;

        // 歩行アニメーション
        // 前脚：互い違いに前後
        this.leg_F_right.xRot = Mth.cos(limbSwing * 0.6662F)            * 1.4F * limbSwingAmount;
        this.leg_F_left.xRot  = Mth.cos(limbSwing * 0.6662F + Mth.PI)   * 1.4F * limbSwingAmount;

        // 後脚（bone = 右, bone2 = 左）
        this.bone.xRot  = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.bone2.xRot = Mth.cos(limbSwing * 0.6662F)          * 1.4F * limbSwingAmount;
    }

    // ---- renderToBuffer ----
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        all.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
