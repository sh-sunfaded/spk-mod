package org.spk.spk.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public class SpkPetEntity extends TamableAnimal {

    // --- 野生時 HP / テイム後 HP ---
    private static final float WILD_HEALTH = 8.0F;
    private static final float TAME_HEALTH = 20.0F;

    // --- テイム中かどうかのフラグ（isInterested 相当、今回は不使用だが予約） ---
    // EntityDataAccessor は TamableAnimal が DATA_FLAGS_ID / DATA_OWNERUUID_ID を既に定義済み

    public SpkPetEntity(EntityType<? extends SpkPetEntity> type, Level level) {
        super(type, level);
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
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
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
    //  インタラクション：テイム / 餌 HP 回復
    // ----------------------------------------------------------------
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            // クライアント側：ハート/煙パーティクル表示のためには true を返す必要あり
            boolean willInteract = this.isOwnedBy(player)
                    || (stack.is(Items.BONE) && !this.isTame());
            return willInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        // --- テイム済み ---
        if (this.isTame()) {
            // 餌で HP 回復
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

        // --- 野生：骨でテイム試行 ---
        if (stack.is(Items.BONE)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(3) == 0
                    && !ForgeEventFactory.onAnimalTame(this, player)) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7); // ハートパーティクル
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6); // 煙パーティクル
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    // ----------------------------------------------------------------
    //  餌判定：肉系食品
    // ----------------------------------------------------------------
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.getItem().isEdible()
                && stack.getFoodProperties(this) != null
                && stack.getFoodProperties(this).isMeat();
    }

    // ----------------------------------------------------------------
    //  サウンド（Wolf の汎用サウンドを流用）
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
