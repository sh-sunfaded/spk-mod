package org.spk.spk.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spk.spk.entity.SpkPetEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * カボチャの近くで行動する AI。
 * 周囲にカボチャ（通常 / くり抜き）がある場合、その近く（3〜5ブロック以内）に留まり巡回する。
 * カボチャがない場合は canUse が false となり、自由行動（WaterAvoidingRandomStrollGoal）にフォールバックする。
 */
public class StayNearPumpkinGoal extends Goal {

    private final SpkPetEntity mob;
    private final double speedModifier;
    private final int searchRadius;
    private final int searchYRadius;

    @Nullable
    private BlockPos pumpkinPos = null;
    private int cooldown = 0;

    public StayNearPumpkinGoal(SpkPetEntity mob, double speedModifier, int searchRadius, int searchYRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.searchRadius = searchRadius;
        this.searchYRadius = searchYRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // テイム済みかつオーナーが追従範囲内にいる場合はオーナー追従を優先
        if (this.mob.isTame() && this.mob.getOwner() != null && this.mob.distanceToSqr(this.mob.getOwner()) < 64.0D) {
            return false;
        }

        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        this.pumpkinPos = this.findNearestPumpkin();
        return this.pumpkinPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pumpkinPos == null) {
            return false;
        }
        if (this.mob.isTame() && this.mob.getOwner() != null && this.mob.distanceToSqr(this.mob.getOwner()) < 64.0D) {
            return false;
        }
        // カボチャがまだ存在するか
        BlockState state = this.mob.level().getBlockState(this.pumpkinPos);
        return state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN);
    }

    @Override
    public void start() {
        this.moveToPumpkinArea();
    }

    @Override
    public void tick() {
        if (this.pumpkinPos == null) {
            return;
        }

        double distSq = this.mob.distanceToSqr(this.pumpkinPos.getX() + 0.5D, this.pumpkinPos.getY(), this.pumpkinPos.getZ() + 0.5D);

        // カボチャから離れすぎている（6ブロック以上）場合はカボチャへ引き返す
        if (distSq > 36.0D) {
            if (this.mob.getNavigation().isDone()) {
                this.mob.getNavigation().moveTo(this.pumpkinPos.getX() + 0.5D, this.pumpkinPos.getY(), this.pumpkinPos.getZ() + 0.5D, this.speedModifier);
            }
        } else {
            // カボチャの近く（6ブロック以内）にいる場合：カボチャ周囲 3 ブロック以内のランダム位置を気ままにうろつく
            if (this.mob.getNavigation().isDone() && this.mob.getRandom().nextInt(30) == 0) {
                Vec3 randomPos = DefaultRandomPos.getPosTowards(this.mob, 3, 2, Vec3.atCenterOf(this.pumpkinPos), (float) Math.PI / 2F);
                if (randomPos != null) {
                    this.mob.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, this.speedModifier * 0.8D);
                }
            }
        }
    }

    @Override
    public void stop() {
        this.pumpkinPos = null;
        this.cooldown = 40 + this.mob.getRandom().nextInt(40);
    }

    private void moveToPumpkinArea() {
        if (this.pumpkinPos != null) {
            this.mob.getNavigation().moveTo(this.pumpkinPos.getX() + 0.5D, this.pumpkinPos.getY(), this.pumpkinPos.getZ() + 0.5D, this.speedModifier);
        }
    }

    @Nullable
    private BlockPos findNearestPumpkin() {
        // 登録カボチャ（まだワールドに存在するもの）を最優先で守る。
        // スピキが離れていても登録先へ戻り、近くで行動する。
        BlockPos registered = this.mob.getNearestValidPumpkin();
        if (registered != null) {
            return registered;
        }

        BlockPos currentPos = this.mob.blockPosition();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (BlockPos checkPos : BlockPos.betweenClosed(
                currentPos.offset(-searchRadius, -searchYRadius, -searchRadius),
                currentPos.offset(searchRadius, searchYRadius, searchRadius))) {
            BlockState state = this.mob.level().getBlockState(checkPos);
            if (state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN)) {
                double distSq = currentPos.distSqr(checkPos);
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = checkPos.immutable();
                }
            }
        }
        return nearest;
    }
}
