package org.spk.spk.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class SpkPetEntity extends TamableAnimal {

    // --- 野生時 HP / テイム後 HP ---
    private static final float WILD_HEALTH = 8.0F;
    private static final float TAME_HEALTH = 20.0F;

    // --- 餌付け・誘引アイテム（カボチャ / くり抜かれたカボチャ） ---
    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.PUMPKIN, Items.CARVED_PUMPKIN);

    // --- 虹色スピン演出用定数 ---
    public static final byte EVENT_RAINBOW_SPIN = 68;
    private static final int RAINBOW_SPIN_DURATION = 60; // 3秒間 (60 ticks)
    private int rainbowSpinTicks = 0;

    public SpkPetEntity(EntityType<? extends SpkPetEntity> type, Level level) {
        super(type, level);
    }

    // ----------------------------------------------------------------
    //  カボチャ判定
    // ----------------------------------------------------------------
    public static boolean isPumpkin(ItemStack stack) {
        return stack.is(Items.PUMPKIN) || stack.is(Items.CARVED_PUMPKIN);
    }

    // ----------------------------------------------------------------
    //  野生スポーンルール：日中の外、地面、カボチャの周囲
    // ----------------------------------------------------------------
    public static boolean checkSpkPetSpawnRules(
            EntityType<SpkPetEntity> entityType,
            net.minecraft.world.level.LevelAccessor level,
            net.minecraft.world.entity.MobSpawnType spawnType,
            net.minecraft.core.BlockPos pos,
            net.minecraft.util.RandomSource random) {

        // スポーンエッグやコマンドの場合は常に許可
        if (spawnType == net.minecraft.world.entity.MobSpawnType.SPAWN_EGG
                || spawnType == net.minecraft.world.entity.MobSpawnType.COMMAND) {
            return true;
        }

        // 外（空が見える）
        if (!level.canSeeSky(pos)) {
            return false;
        }

        // 日中（昼間：暗くない）
        if (level.getSkyDarken() >= 4) {
            return false;
        }

        // 明るさ十分
        if (level.getRawBrightness(pos, 0) <= 8) {
            return false;
        }

        // 地面が動物スポーン可能ブロック（草ブロック等）
        if (!level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.ANIMALS_SPAWNABLE_ON)) {
            return false;
        }

        // カボチャの周り（周囲12ブロック、上下4ブロック以内にカボチャがあるか）
        int radius = 12;
        int yRadius = 4;
        for (net.minecraft.core.BlockPos checkPos : net.minecraft.core.BlockPos.betweenClosed(
                pos.offset(-radius, -yRadius, -radius),
                pos.offset(radius, yRadius, radius))) {
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(checkPos);
            if (state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                    || state.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN)) {
                return true;
            }
        }

        return false;
    }

    // ----------------------------------------------------------------
    //  属性
    // ----------------------------------------------------------------
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, WILD_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    // ----------------------------------------------------------------
    //  AI
    // ----------------------------------------------------------------
    @Override
    protected void registerGoals() {
        // 移動系
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // 手持ちカボチャに近づく (canScare = false: 急に動いても逃げない)
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, TEMPT_INGREDIENT, false));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        // カボチャの周りで行動（カボチャがなければ自由行動へ）
        this.goalSelector.addGoal(7, new org.spk.spk.entity.ai.StayNearPumpkinGoal(this, 1.0D, 16, 4));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        // ターゲット系
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    // ----------------------------------------------------------------
    //  テイム時に HP を上書き（Wolf と同等）
    // ----------------------------------------------------------------
    @Override
    public void setTame(boolean tame) {
        super.setTame(tame);
        if (tame) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(TAME_HEALTH);
            this.setHealth(TAME_HEALTH);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(WILD_HEALTH);
        }
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    // ----------------------------------------------------------------
    //  虹色スピンイベントの発火・管理
    // ----------------------------------------------------------------
    public void triggerRainbowSpin() {
        this.rainbowSpinTicks = RAINBOW_SPIN_DURATION;
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, EVENT_RAINBOW_SPIN);
            this.navigation.stop();
        }
    }

    public boolean isRainbowSpinning() {
        return this.rainbowSpinTicks > 0;
    }

    public int getRainbowSpinTicks() {
        return this.rainbowSpinTicks;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_RAINBOW_SPIN) {
            this.triggerRainbowSpin();
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.rainbowSpinTicks > 0) {
            this.rainbowSpinTicks--;
            if (this.level().isClientSide) {
                // クライアント側：虹色 Dust パーティクルとグローパーティクルをキラキラ放出
                float hue = ((this.tickCount * 12.0F) % 360.0F) / 360.0F;
                float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth() * 1.6D;
                double py = this.getY() + this.random.nextDouble() * this.getBbHeight() + 0.2D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth() * 1.6D;
                this.level().addParticle(new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), 1.2F),
                        px, py, pz, 0.0D, 0.06D, 0.0D);
                if (this.random.nextFloat() < 0.35F) {
                    this.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0.0D, 0.03D, 0.0D);
                }
            }
        }
    }

    // ----------------------------------------------------------------
    //  インタラクション：カボチャでテイム / 虹色スピン発火
    // ----------------------------------------------------------------
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            // クライアント側：テイム済み時のカボチャ、または野生時のカボチャで腕振り＆イベントトリガー
            boolean willInteract = (this.isOwnedBy(player) && isPumpkin(stack))
                    || (this.isOwnedBy(player) && this.isFood(stack) && this.getHealth() < this.getMaxHealth())
                    || (isPumpkin(stack) && !this.isTame());
            return willInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        // --- テイム済み ---
        if (this.isTame()) {
            // カボチャを与える：HPの成否にかかわらず常に消費して虹色スピンイベントを発火！
            if (isPumpkin(stack) && this.isOwnedBy(player)) {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(4.0F);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.gameEvent(GameEvent.EAT, this);
                this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F);
                this.triggerRainbowSpin();
                return InteractionResult.SUCCESS;
            }

            // 既存の肉系食品でも HP 回復
            if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
                float nutrition = stack.getFoodProperties(this).getNutrition();
                this.heal(nutrition);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.gameEvent(GameEvent.EAT, this);
                return InteractionResult.SUCCESS;
            }

            // 素手クリックは何もしない（お座り実装なし）
            return super.mobInteract(player, hand);
        }

        // --- 野生：カボチャでテイム試行 ---
        if (isPumpkin(stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(3) == 0
                    && !ForgeEventFactory.onAnimalTame(this, player)) {
                // 餌付け成功！
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7); // ハートパーティクル
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F);
                this.triggerRainbowSpin(); // 成功時のみ虹色スピンイベント発火！
            } else {
                // 餌付け失敗（このイベントは行われない）
                this.level().broadcastEntityEvent(this, (byte) 6); // 煙パーティクル
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    // ----------------------------------------------------------------
    //  HSV -> RGB 変換ユーティリティ
    // ----------------------------------------------------------------
    public static float[] hsvToRgb(float hue, float saturation, float value) {
        int h = (int)(hue * 6.0F);
        float f = hue * 6.0F - (float)h;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - f * saturation);
        float t = value * (1.0F - (1.0F - f) * saturation);

        return switch (h % 6) {
            case 0 -> new float[]{value, t, p};
            case 1 -> new float[]{q, value, p};
            case 2 -> new float[]{p, value, t};
            case 3 -> new float[]{p, q, value};
            case 4 -> new float[]{t, p, value};
            case 5 -> new float[]{value, p, q};
            default -> new float[]{1.0F, 1.0F, 1.0F};
        };
    }

    // ----------------------------------------------------------------
    //  餌判定：肉系食品（カボチャ以外の追加餌）
    // ----------------------------------------------------------------
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.getItem().isEdible()
                && stack.getFoodProperties(this) != null
                && stack.getFoodProperties(this).isMeat();
    }

    // ----------------------------------------------------------------
    //  サウンド（Wolf / Cat の汎用サウンドを流用）
    // ----------------------------------------------------------------
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTame() && this.getHealth() < 10.0F
                ? SoundEvents.WOLF_WHINE
                : SoundEvents.CAT_PURR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    // ----------------------------------------------------------------
    //  繁殖なし
    // ----------------------------------------------------------------
    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    // ----------------------------------------------------------------
    //  セーブ / ロード（TamableAnimal が Owner / Sitting を処理）
    // ----------------------------------------------------------------
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }
}
