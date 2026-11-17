package org.spk.spk;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.spk.spk.client.model.SpkPetModel;
import org.spk.spk.client.renderer.SpkPetRenderer;
import org.spk.spk.entity.SpkPetEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Spk.MODID)
public class Spk {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "spk";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // ---- Deferred Registers ----
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    // ---- 既存コンテンツ ----
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(EXAMPLE_ITEM.get());
    }).build());

    // ---- SpkPet EntityType ----
    public static final RegistryObject<EntityType<SpkPetEntity>> SPK_PET = ENTITY_TYPES.register(
            "spk_pet",
            () -> EntityType.Builder.<SpkPetEntity>of(SpkPetEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(10)
                    .build("spk_pet")
    );

    // ---- スポーンエッグ ----
    // 色：外側 0xC0A060（茶系）、内側 0xFFFFFF（白）
    // ForgeSpawnEggItem を使い、Supplier<EntityType> を遅延評価で渡す（ITEMS 登録時に SPK_PET が未登録のため）
    public static final RegistryObject<Item> SPK_PET_SPAWN_EGG = ITEMS.register(
            "spk_pet_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(SPK_PET, 0xC0A060, 0xFFFFFF, new Item.Properties())
    );

    // ---- サウンドイベント ----
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SPK_PET_AMBIENT = SOUND_EVENTS.register(
            "entity.spk_pet.ambient",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "entity.spk_pet.ambient"))
    );
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SPK_PET_HURT = SOUND_EVENTS.register(
            "entity.spk_pet.hurt",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "entity.spk_pet.hurt"))
    );
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SPK_PET_DEATH_EXPLODE = SOUND_EVENTS.register(
            "entity.spk_pet.death_explode",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "entity.spk_pet.death_explode"))
    );
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SPK_PET_SQUASH = SOUND_EVENTS.register(
            "entity.spk_pet.squash",
            () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "entity.spk_pet.squash"))
    );

    public Spk() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerAttributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(SPK_PET.get(), SpkPetEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(net.minecraftforge.event.entity.SpawnPlacementRegisterEvent event) {
        event.register(
                SPK_PET.get(),
                net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpkPetEntity::checkSpkPetSpawnRules,
                net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.OR
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) event.accept(SPK_PET_SPAWN_EGG);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    /**
     * カボチャのスピキへの登録：
     * スニーク + 素手でカボチャ（通常 / くり抜き）ブロックを右クリックすると、
     * 32ブロック以内にいる自分のテイム済みスピキのうち最も近い個体に、
     * そのカボチャを登録（上書き）する。登録されたスピキはそのカボチャを守る。
     */
    @SubscribeEvent
    public void onPumpkinRegisterClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        var state = level.getBlockState(event.getPos());
        if (!(state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN))) {
            return;
        }

        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || !event.getItemStack().isEmpty()) {
            return;
        }

        // 野生 / テイム済みを問わず、近隣のスピキを登録対象にする
        // （カボチャの習性は野生のスピキのもの。テイムの有無にかかわらず守りを担う）
        java.util.List<SpkPetEntity> pets = level.getEntitiesOfClass(
                SpkPetEntity.class,
                player.getBoundingBox().inflate(32.0D));

        SpkPetEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (SpkPetEntity pet : pets) {
            double distSq = player.distanceToSqr(pet);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = pet;
            }
        }
        if (nearest == null) {
            return; // 登録できるスピキがいなければバニラの挙動に任せる
        }

        nearest.registerPumpkin(event.getPos());
        if (!level.isClientSide()) {
            level.broadcastEntityEvent(nearest, (byte) 7); // ハートパーティクル
            nearest.playSound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        /** モデルレイヤー登録 */
        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(SpkPetModel.LAYER_LOCATION, SpkPetModel::createBodyLayer);
        }

        /** レンダラー登録 */
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(SPK_PET.get(), SpkPetRenderer::new);
        }
    }
}
