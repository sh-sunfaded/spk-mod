// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class spk_pet<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "spk_pet"), "main");
	private final ModelPart heads;
	private final ModelPart body;
	private final ModelPart legs;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart leg_F_right;
	private final ModelPart leg_F_left;

	public spk_pet(ModelPart root) {
		this.heads = root.getChild("heads");
		this.body = root.getChild("body");
		this.legs = root.getChild("legs");
		this.bone = this.legs.getChild("bone");
		this.bone2 = this.legs.getChild("bone2");
		this.leg_F_right = root.getChild("leg_F_right");
		this.leg_F_left = root.getChild("leg_F_left");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition heads = partdefinition.addOrReplaceChild("heads", CubeListBuilder.create().texOffs(0, 8).addBox(-4.0F, -2.5F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(1, 15).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 19.0F, 4.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 21.0F, 1.0F));

		PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, -1.0F));

		PartDefinition bone = legs.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(18, 14).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 8).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 1.0F));

		PartDefinition bone2 = legs.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(18, 17).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 11).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.0F, 1.0F));

		PartDefinition leg_F_right = partdefinition.addOrReplaceChild("leg_F_right", CubeListBuilder.create().texOffs(18, 0).addBox(-0.2F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.7F, 21.3F, 2.5F));

		PartDefinition leg_F_left = partdefinition.addOrReplaceChild("leg_F_left", CubeListBuilder.create().texOffs(18, 3).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 21.3F, 2.5F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		heads.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_F_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_F_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}