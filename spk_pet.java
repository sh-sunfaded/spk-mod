// Made with Blockbench 5.2.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class spk_pet<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "spk_pet"), "main");
	private final ModelPart all;
	private final ModelPart heads;
	private final ModelPart hair;
	private final ModelPart body;
	private final ModelPart legs;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart leg_F_right;
	private final ModelPart leg_F_left;
	private final ModelPart bone3;

	public spk_pet(ModelPart root) {
		this.all = root.getChild("all");
		this.heads = this.all.getChild("heads");
		this.hair = this.heads.getChild("hair");
		this.body = this.all.getChild("body");
		this.legs = this.all.getChild("legs");
		this.bone = this.legs.getChild("bone");
		this.bone2 = this.legs.getChild("bone2");
		this.leg_F_right = this.all.getChild("leg_F_right");
		this.leg_F_left = this.all.getChild("leg_F_left");
		this.bone3 = root.getChild("bone3");
	}

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

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		all.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}