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
 * spk_pet.java（Blockbench 5.2.2 出力）を MOD パッケージへ移植したモデル。
 * allall + pumpkin ボーンを含む最新の骨・cube 定義に追従。
 * setupAnim に歩行と攻撃モーション（ホバギ突進）、虹色発光を実装。
 */
public class SpkPetModel<T extends SpkPetEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Spk.MODID, "spk_pet"), "main");

    // ---- 骨 ----
    private final ModelPart allall;      // ルート（カボチャ攻撃時の突進移動）
    private final ModelPart all;
    private final ModelPart heads;
    private final ModelPart hair;
    private final ModelPart body;
    private final ModelPart legs;
    private final ModelPart bone;        // legs の子：後ろ右
    private final ModelPart bone2;       // legs の子：後ろ左
    private final ModelPart leg_F_right; // 前右脚
    private final ModelPart leg_F_left;  // 前左脚
    private final ModelPart bone3;       // all の子
    private final ModelPart pumpkin;     // 攻撃用カボチャ（allall の子）

    // ---- 虹色発光用キャッシュ ----
    private boolean isRainbowSpinning = false;
    private float rainbowR = 1.0F;
    private float rainbowG = 1.0F;
    private float rainbowB = 1.0F;

    public SpkPetModel(ModelPart root) {
        this.allall      = root.getChild("allall");
        this.all         = this.allall.getChild("all");
        this.heads       = this.all.getChild("heads");
        this.hair        = this.heads.getChild("hair");
        this.body        = this.all.getChild("body");
        this.legs        = this.all.getChild("legs");
        this.bone        = this.legs.getChild("bone");
        this.bone2       = this.legs.getChild("bone2");
        this.leg_F_right = this.all.getChild("leg_F_right");
        this.leg_F_left  = this.all.getChild("leg_F_left");
        this.bone3       = this.all.getChild("bone3");
        this.pumpkin     = this.allall.getChild("pumpkin");
    }

    // ---- LayerDefinition（最新 spk_pet.java そのまま） ----
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition allall = partdefinition.addOrReplaceChild("allall", CubeListBuilder.create(), PartPose.offset(0.2F, 24.0F, 0.6F));

        PartDefinition all = allall.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -5.0F, -0.6F, 0.0F, 3.1416F, 0.0F));

        PartDefinition heads = all.addOrReplaceChild("heads", CubeListBuilder.create().texOffs(16, 6).addBox(-2.0F, -2.5F, -0.4F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-2.0F, -0.5F, 1.1F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 23).addBox(1.4F, -0.5F, 0.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 23).addBox(-2.4F, -0.5F, 0.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.6F));

        PartDefinition cube_r1 = heads.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(18, 0).addBox(-2.0F, -2.0F, -3.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 0).addBox(-4.8F, -2.0F, -3.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9F, -0.5F, 0.7F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r2 = heads.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, -1.0F, -3.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-5.3F, -1.0F, -3.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, -1.3F, -0.1F, 0.6545F, 0.0F, 0.0F));

        PartDefinition cube_r3 = heads.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -4.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.5F, -0.6F, 0.6545F, 0.0F, 0.0F));

        PartDefinition hair = heads.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(16, 12).addBox(-4.0F, -1.8F, -1.9F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 16).addBox(-4.0F, -1.9F, -2.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 12).addBox(-4.1F, -1.8F, -2.1F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 12).addBox(-3.9F, -1.8F, -2.1F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.0F, 1.6F));

        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(12, 20).addBox(-2.0F, -0.8F, -2.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 20).addBox(-2.0F, -0.3F, -2.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -0.8F));

        PartDefinition legs = all.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(-0.5F, 5.0F, -2.8F));

        PartDefinition bone = legs.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(12, 26).addBox(-1.8F, -1.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 20).addBox(-1.8F, 1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -2.0F, 1.0F));

        PartDefinition bone2 = legs.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(16, 26).addBox(-0.4F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 23).addBox(-0.4F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.7F, 0.0F, 1.0F));

        PartDefinition leg_F_right = all.addOrReplaceChild("leg_F_right", CubeListBuilder.create().texOffs(0, 1).addBox(0.8F, -0.3F, -0.5F, -1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.2F, 2.3F, 0.7F));

        PartDefinition leg_F_left = all.addOrReplaceChild("leg_F_left", CubeListBuilder.create().texOffs(8, 26).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 2.3F, 0.7F));

        PartDefinition bone3 = all.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 3.1F, -3.6F, 0.0F, 3.0543F, 0.0F));

        PartDefinition cube_r4 = bone3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(26, 23).addBox(-1.0F, -1.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r5 = bone3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(20, 26).addBox(-1.0F, -2.0F, -3.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.4F, 0.2618F, 0.0F, 0.0F));

        PartDefinition pumpkin = allall.addOrReplaceChild("pumpkin", CubeListBuilder.create().texOffs(0, 6).addBox(-3.0F, -3.0F, -1.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(2, 7).addBox(-2.5F, -3.4F, -0.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.2F, 0.0F, -0.6F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    // ----------------------------------------------------------------
    //  攻撃モーション（Blockbench「ホバギ突進」）のキーフレーム
    //  単位: tick、座標は px（1px = 1/16 ブロック）、回転は度
    //  変換規則: 位置 (x, y, z) → (-x, -y, z)、回転は符号を反転して加算
    // ----------------------------------------------------------------
    private static final float ATTACK_DURATION_TICKS = 41.0F; // 2.04167秒

    // allall: 前方へ突進（着地まで維持）
    private static final float[] ALLALL_Z_TICKS = {0.0F, 20.0F};
    private static final float[] ALLALL_Z_VALS  = {0.0F, -20.0F};

    // all: 前転（-90 → -360 度）しながらジャンプして着地
    private static final float[] ALL_ROT_X_TICKS = {0.0F, 25.0F, 35.0F};
    private static final float[] ALL_ROT_X_VALS  = {0.0F, -90.0F, -360.0F};
    private static final float[] ALL_POS_TICKS   = {0.0F, 5.0F, 10.0F, 15.833F, 25.0F, 35.0F};
    private static final float[] ALL_POS_Y_VALS  = {0.0F, 3.0F, 7.8F, 6.8F, 11.5F, 0.0F};
    private static final float[] ALL_POS_Z_VALS  = {0.0F, 0.0F, 0.0F, 0.0F, 10.8F, 20.1F};

    // pumpkin: 手元に出現（0.375秒後）して前に突き出し、終了時（2秒）に消える
    private static final float[] PUMPKIN_POS_Z_TICKS = {0.0F, 35.0F};
    private static final float[] PUMPKIN_POS_Z_VALS  = {-1.1F, -9.1F};
    private static final float PUMPKIN_APPEAR_TICK = 7.5F;
    private static final float PUMPKIN_HIDE_TICK   = 40.0F;
    private static final float PUMPKIN_SCALE       = 2.0F;

    private static float sampleLinear(float[] ticks, float[] vals, float t) {
        if (t <= ticks[0]) {
            return vals[0];
        }
        for (int i = 1; i < ticks.length; i++) {
            if (t <= ticks[i]) {
                float f = (t - ticks[i - 1]) / (ticks[i] - ticks[i - 1]);
                return Mth.lerp(f, vals[i - 1], vals[i]);
            }
        }
        return vals[vals.length - 1];
    }

    // ---- setupAnim ----
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        // 毎フレーム、レストポーズへ戻してからアニメーションを適用
        this.allall.resetPose();
        this.all.resetPose();
        this.pumpkin.resetPose();
        this.pumpkin.xScale = 0.0F;
        this.pumpkin.yScale = 0.0F;
        this.pumpkin.zScale = 0.0F;

        // 頭の向き
        this.heads.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.heads.xRot = headPitch  * Mth.DEG_TO_RAD;

        // 歩行アニメーション
        // 前脚：互い違いに前後
        this.leg_F_right.xRot = Mth.cos(limbSwing * 0.6662F)            * 1.4F * limbSwingAmount;
        this.leg_F_left.xRot  = Mth.cos(limbSwing * 0.6662F + Mth.PI)   * 1.4F * limbSwingAmount;

        // 後脚（bone = 右, bone2 = 左）
        this.bone.xRot  = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.bone2.xRot = Mth.cos(limbSwing * 0.6662F)          * 1.4F * limbSwingAmount;

        // 攻撃モーション（カボチャ突進）
        if (entity.isPumpkinAttacking()) {
            float t = Mth.clamp(ageInTicks - entity.getPumpkinAttackStartTick(), 0.0F, ATTACK_DURATION_TICKS);

            // allall: 前方へ突進
            this.allall.z += sampleLinear(ALLALL_Z_TICKS, ALLALL_Z_VALS, t);

            // all: 前転しながらジャンプ → 着地
            this.all.xRot += Mth.DEG_TO_RAD * -sampleLinear(ALL_ROT_X_TICKS, ALL_ROT_X_VALS, t);
            this.all.y -= sampleLinear(ALL_POS_TICKS, ALL_POS_Y_VALS, t);
            this.all.z += sampleLinear(ALL_POS_TICKS, ALL_POS_Z_VALS, t);

            // pumpkin: 横から構えて前に突き出す
            this.pumpkin.x += 7.2F; // Blockbench x=-7.2 は符号反転
            this.pumpkin.z += sampleLinear(PUMPKIN_POS_Z_TICKS, PUMPKIN_POS_Z_VALS, t);
            float pumpkinScale = (t >= PUMPKIN_APPEAR_TICK && t < PUMPKIN_HIDE_TICK) ? PUMPKIN_SCALE : 0.0F;
            this.pumpkin.xScale = pumpkinScale;
            this.pumpkin.yScale = pumpkinScale;
            this.pumpkin.zScale = pumpkinScale;
        }

        // 虹色発光状態の判定
        if (entity.isRainbowSpinning()) {
            this.isRainbowSpinning = true;
            // 虹色の色相を時間経過でスムーズに一周させる（約1.5秒で1周）
            float hue = (ageInTicks * 8.0F) % 360.0F / 360.0F;
            float[] rgb = SpkPetEntity.hsvToRgb(hue, 0.9F, 1.0F);
            this.rainbowR = rgb[0];
            this.rainbowG = rgb[1];
            this.rainbowB = rgb[2];
        } else {
            this.isRainbowSpinning = false;
        }
    }

    // ---- renderToBuffer ----
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        if (this.isRainbowSpinning) {
            // 光る：最大明度 0xF000F0（フルブライト）で虹色を乗算描画
            int brightLight = 0xF000F0;
            allall.render(poseStack, vertexConsumer, brightLight, packedOverlay,
                    this.rainbowR, this.rainbowG, this.rainbowB, alpha);
        } else {
            allall.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
