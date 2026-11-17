// Made with Blockbench 5.2.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class spk_pet<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "spk_pet"), "main");
	private final ModelPart allall;
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
	private final ModelPart pumpkin;

	public spk_pet(ModelPart root) {
		this.allall = root.getChild("allall");
		this.all = this.allall.getChild("all");
		this.heads = this.all.getChild("heads");
		this.hair = this.heads.getChild("hair");
		this.body = this.all.getChild("body");
		this.legs = this.all.getChild("legs");
		this.bone = this.legs.getChild("bone");
		this.bone2 = this.legs.getChild("bone2");
		this.leg_F_right = this.all.getChild("leg_F_right");
		this.leg_F_left = this.all.getChild("leg_F_left");
		this.bone3 = this.all.getChild("bone3");
		this.pumpkin = this.allall.getChild("pumpkin");
	}

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

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		allall.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}