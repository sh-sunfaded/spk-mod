package org.spk.spk.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import org.spk.spk.Spk;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

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

    // --- カボチャ攻撃モーション（Blockbench「ホバギ突進」）用定数 ---
    // クライアント（SpkPetModel）と共有する
    public static final byte EVENT_PUMPKIN_ATTACK = 69;
    public static final float PUMPKIN_ATTACK_DURATION_TICKS = 41.0F; // 2.04167秒
    public static final float PUMPKIN_APPEAR_TICK = 7.5F;            // カボチャ出現（0.375秒）
    public static final float PUMPKIN_HIDE_TICK = 40.0F;             // カボチャ消失（2.0秒）
    public static final float PUMPKIN_THRUST_START_Z = -1.1F;        // 突き出し開始（px）
    public static final float PUMPKIN_THRUST_END_Z = -9.1F;          // 突き出し終了（px）
    public static final float PUMPKIN_THRUST_END_TICK = 35.0F;       // 突き出し完了時刻
    public static final float PUMPKIN_STRIKE_TICK = 10.0F;           // カボチャが届く打撃予定時刻
    private float pumpkinAttackStartTick = -PUMPKIN_ATTACK_DURATION_TICKS;
    @Nullable
    private LivingEntity pumpkinStrikeTarget = null; // カボチャで打つ予定の相手
    private int pumpkinStrikeTick = 0;

    // --- ホバギ（カボチャ）登録 ---
    // スピキは近隣のカボチャの座標を記録する。ワールドに存在する限り保持され、
    // 記録したカボチャの近くで行動し、その防衛（敵モブ・村人の排除）も行う。
    private static final double PUMPKIN_NEAR_RANGE = 16.0D;   // 防衛を行う距離（スピキと登録カボチャの距離）
    private static final int PUMPKIN_MEMORY_LIMIT = 16;       // 記録できるカボチャの上限
    private static final int PUMPKIN_SCAN_RADIUS = 8;         // 自動記録の探索半径（横）
    private static final int PUMPKIN_SCAN_Y_RADIUS = 2;       // 自動記録の探索半径（縦）
    private final java.util.List<BlockPos> registeredPumpkins = new java.util.ArrayList<>(); // 新しいものが先頭

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
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
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
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true) {
            // カボチャ攻撃モーション（約2秒）と重ならないよう、打撃間隔を延ばす
            @Override
            protected int getAttackInterval() {
                return 30;
            }
        });
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
        // 登録カボチャの防衛：カボチャに近づく敵モブ・村人
        this.targetSelector.addGoal(4, new org.spk.spk.entity.ai.GuardPumpkinTargetGoal(this));
    }

    // ----------------------------------------------------------------
    //  ホバギ（カボチャ）の登録・管理
    // ----------------------------------------------------------------
    /**
     * カボチャの座標をスピキに登録する。
     * 重複登録はせず、上限に達したら一番古い記録を破棄する（新しいものほど先頭）。
     */
    public void registerPumpkin(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        this.registeredPumpkins.remove(immutable);
        while (this.registeredPumpkins.size() >= PUMPKIN_MEMORY_LIMIT) {
            this.registeredPumpkins.remove(this.registeredPumpkins.size() - 1);
        }
        this.registeredPumpkins.add(0, immutable);
    }

    /**
     * 登録済みカボチャの座標一覧（操作不可）。
     */
    public java.util.List<BlockPos> getRegisteredPumpkins() {
        return java.util.Collections.unmodifiableList(this.registeredPumpkins);
    }

    /**
     * まだワールドに存在する（ブロックが残っている）登録カボチャのみを返す。
     */
    public java.util.List<BlockPos> getValidPumpkins() {
        java.util.List<BlockPos> valid = new java.util.ArrayList<>(this.registeredPumpkins.size());
        for (BlockPos pos : this.registeredPumpkins) {
            var state = this.level().getBlockState(pos);
            if (state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                    || state.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN)) {
                valid.add(pos);
            }
        }
        return valid;
    }

    /**
     * 有効な登録カボチャのうち、スピキから最も近いものを返す。
     */
    @Nullable
    public BlockPos getNearestValidPumpkin() {
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos pos : this.getValidPumpkins()) {
            double distSq = this.distanceToSqr(
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = pos;
            }
        }
        return best;
    }

    /**
     * 防衛対象となるカボチャを返す。
     * スピキが登録カボチャの近くにいない場合は null（＝攻撃は行わない）。
     */
    @Nullable
    public BlockPos getNearestGuardPumpkin() {
        BlockPos nearest = this.getNearestValidPumpkin();
        if (nearest == null) {
            return null;
        }
        double distSq = this.distanceToSqr(
                nearest.getX() + 0.5D, nearest.getY() + 0.5D, nearest.getZ() + 0.5D);
        return distSq <= PUMPKIN_NEAR_RANGE * PUMPKIN_NEAR_RANGE ? nearest : null;
    }

    /**
     * 登録の維持（サーバー側・定期実行）：
     * ワールドから消えたカボチャの記録を破棄し、近隣のカボチャを新たに記録する。
     */
    private void maintainPumpkinRegistrations() {
        // ワールドから消えたカボチャの記録を破棄
        this.registeredPumpkins.removeIf(pos -> {
            var state = this.level().getBlockState(pos);
            return !state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                    && !state.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN);
        });

        // 近隣のカボチャを新たに記録
        BlockPos center = this.blockPosition();
        for (BlockPos checkPos : BlockPos.betweenClosed(
                center.offset(-PUMPKIN_SCAN_RADIUS, -PUMPKIN_SCAN_Y_RADIUS, -PUMPKIN_SCAN_RADIUS),
                center.offset(PUMPKIN_SCAN_RADIUS, PUMPKIN_SCAN_Y_RADIUS, PUMPKIN_SCAN_RADIUS))) {
            var state = this.level().getBlockState(checkPos);
            if ((state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                    || state.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN))
                    && !this.registeredPumpkins.contains(checkPos)) {
                this.registerPumpkin(checkPos);
            }
        }
    }

    /**
     * 攻撃（ターゲット設定）は、カボチャがスピキに登録されている場合のみ許可する。
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.getNearestGuardPumpkin() == null) {
            return;
        }
        super.setTarget(target);
    }

    /**
     * 周囲の最寄りカボチャを探す。
     */
    @Nullable
    public BlockPos findNearestPumpkin(int radius, int yRadius) {
        BlockPos currentPos = this.blockPosition();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (BlockPos checkPos : BlockPos.betweenClosed(
                currentPos.offset(-radius, -yRadius, -radius),
                currentPos.offset(radius, yRadius, radius))) {
            var state = this.level().getBlockState(checkPos);
            if (state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                    || state.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN)) {
                double distSq = currentPos.distSqr(checkPos);
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = checkPos.immutable();
                }
            }
        }
        return nearest;
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

    // ----------------------------------------------------------------
    //  カボチャ攻撃モーションの発火・管理
    // ----------------------------------------------------------------
    public void triggerPumpkinAttack() {
        this.pumpkinAttackStartTick = this.tickCount;
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, EVENT_PUMPKIN_ATTACK);
        }
    }

    public boolean isPumpkinAttacking() {
        return this.tickCount - this.pumpkinAttackStartTick < PUMPKIN_ATTACK_DURATION_TICKS;
    }

    public float getPumpkinAttackStartTick() {
        return this.pumpkinAttackStartTick;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_PUMPKIN_ATTACK) {
            this.triggerPumpkinAttack();
        } else if (id == EVENT_RAINBOW_SPIN) {
            this.triggerRainbowSpin();
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ----------------------------------------------------------------
    //  近接攻撃：カボチャ突進モーションを再生（威力は狼の噛みつき相当）
    //  当たり判定はカボチャ（モーションで前に突き出される位置）
    // ----------------------------------------------------------------
    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.level().isClientSide) {
            return false;
        }
        this.playSound(Spk.SPK_PET_SQUASH.get(), 1.0F, 0.8F);
        this.triggerPumpkinAttack();
        // 打撃はカボチャが出現して届いた瞬間（aiStep）に判定する
        if (target instanceof LivingEntity living) {
            this.pumpkinStrikeTarget = living;
            this.pumpkinStrikeTick = (int) (this.pumpkinAttackStartTick + PUMPKIN_STRIKE_TICK);
        }
        return false;
    }

    /**
     * 攻撃モーション中のカボチャの当たり判定（ワールド座標）。
     * モーション中でない / カボチャがまだ出ていない場合は null。
     */
    @Nullable
    public AABB getPumpkinHitbox() {
        float t = this.tickCount - this.pumpkinAttackStartTick;
        if (t < PUMPKIN_APPEAR_TICK || t >= PUMPKIN_HIDE_TICK) {
            return null;
        }
        // 突き出し（px）の進行: 0 → PUMPKIN_THRUST_END_TICK で z を進める
        float zf = Mth.lerp(Math.min(t / PUMPKIN_THRUST_END_TICK, 1.0F),
                PUMPKIN_THRUST_START_Z, PUMPKIN_THRUST_END_Z);
        double origin = -0.6D + zf;                                       // pumpkin ボーンのz（px）
        double fwd = (-origin - 2.0D) / 16.0D;                             // スピキ中心からカボチャ中心までの前方距離
        float yawRad = this.yBodyRot * ((float) Math.PI / 180.0F);
        double cx = this.getX() - Math.sin(yawRad) * fwd;
        double cz = this.getZ() + Math.cos(yawRad) * fwd;
        double cy = this.getY() + 0.35D;
        return new AABB(cx - 0.25D, cy - 0.35D, cz - 0.25D,
                        cx + 0.25D, cy + 0.35D, cz + 0.25D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // 登録の維持：消えたカボチャの記録破棄 + 近隣カボチャの自動記録（5秒毎）
        if (!this.level().isClientSide && this.tickCount % 100 == 0) {
            this.maintainPumpkinRegistrations();
        }
        // カボチャの一撃：カボチャが届いた瞬間にダメージを与える
        if (!this.level().isClientSide && this.pumpkinStrikeTarget != null && this.tickCount >= this.pumpkinStrikeTick) {
            LivingEntity strikeTarget = this.pumpkinStrikeTarget;
            this.pumpkinStrikeTarget = null;
            AABB pumpkinBox = this.getPumpkinHitbox();
            if (pumpkinBox != null && strikeTarget.isAlive() && !strikeTarget.isRemoved()
                    && pumpkinBox.intersects(strikeTarget.getBoundingBox())) {
                if (strikeTarget.hurt(this.damageSources().mobAttack(this),
                        (float) ((int) this.getAttributeValue(Attributes.ATTACK_DAMAGE)))) {
                    this.doEnchantDamageEffects(this, strikeTarget);
                }
            }
        }
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
                this.playSound(Spk.SPK_PET_SQUASH.get(), 1.0F, 1.0F);
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
                // テイムした場所の近くにあるカボチャを登録
                BlockPos tamePumpkin = this.findNearestPumpkin(16, 4);
                if (tamePumpkin != null) {
                    this.registerPumpkin(tamePumpkin);
                }
                this.level().broadcastEntityEvent(this, (byte) 7); // ハートパーティクル
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F);
                this.playSound(Spk.SPK_PET_SQUASH.get(), 1.0F, 1.0F);
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
    //  サウンド
    // ----------------------------------------------------------------
    @Override
    protected SoundEvent getAmbientSound() {
        return Spk.SPK_PET_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Spk.SPK_PET_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        DamageSource lastDamage = this.getLastDamageSource();
        if (lastDamage != null && lastDamage.is(DamageTypeTags.IS_EXPLOSION)) {
            return Spk.SPK_PET_DEATH_EXPLODE.get();
        }
        return null;
    }

    @Override
    protected float getSoundVolume() {
        DamageSource lastDamage = this.getLastDamageSource();
        if (lastDamage != null && lastDamage.is(DamageTypeTags.IS_EXPLOSION)) {
            return 2.0F;
        }
        return super.getSoundVolume();
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
        // 登録カボチャの座標を保存（ワールドに存在する限り保持）
        List<BlockPos> pumpkins = this.registeredPumpkins;
        if (!pumpkins.isEmpty()) {
            long[] coords = new long[pumpkins.size()];
            for (int i = 0; i < pumpkins.size(); i++) {
                coords[i] = pumpkins.get(i).asLong();
            }
            tag.putLongArray("RegisteredPumpkins", coords);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.registeredPumpkins.clear();
        if (tag.contains("RegisteredPumpkins")) {
            for (long coord : tag.getLongArray("RegisteredPumpkins")) {
                this.registerPumpkin(BlockPos.of(coord));
            }
        } else if (tag.contains("RegisteredPumpkin")) {
            // 旧形式（単一登録）からの移行
            this.registeredPumpkins.add(BlockPos.of(tag.getLong("RegisteredPumpkin")));
        }
    }
}
