package com.mushokucraft.magic.companion;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class SummonCompanionManager {
    public static final String TAG_FAMILIAR = "mushokucraft_familiar";
    public static final String TAG_OWNER_PREFIX = "mushokucraft_owner_";

    public static void init() {
        // Re-attach goals when familiars are loaded from saved chunks
        EntityEvent.ADD.register((entity, level) -> {
            if (!level.isClientSide() && entity instanceof Mob mob) {
                if (mob.getTags().contains(TAG_FAMILIAR)) {
                    UUID ownerUUID = getOwnerUUID(mob);
                    if (ownerUUID != null) {
                        attachGoals(mob, ownerUUID);
                    }
                }
            }
            return EventResult.pass();
        });

        // Continuous server-side companion AI loop (handles goal-based mobs AND Brain-based mobs like Warden)
        TickEvent.PLAYER_POST.register(player -> {
            if (player.level().isClientSide() || !player.isAlive()) return;

            AABB searchBox = player.getBoundingBox().inflate(32.0);
            List<Mob> familiars = player.level().getEntitiesOfClass(Mob.class, searchBox,
                    m -> m.isAlive() && m.getTags().contains(TAG_FAMILIAR) && player.getUUID().equals(getOwnerUUID(m)));

            if (familiars.isEmpty()) return;

            // Determine priority enemy target:
            // 1. Who the player is fighting (last attacked)
            // 2. Who attacked the player (last attacker)
            // 3. Any hostile monster within 14 blocks of player (excluding other familiars)
            LivingEntity combatTarget = null;
            if (player.getLastHurtMob() != null && player.getLastHurtMob().isAlive() && !(player.getLastHurtMob() instanceof Player)) {
                combatTarget = player.getLastHurtMob();
            } else if (player.getLastHurtByMob() != null && player.getLastHurtByMob().isAlive() && !(player.getLastHurtByMob() instanceof Player)) {
                combatTarget = player.getLastHurtByMob();
            } else {
                List<Monster> hostiles = player.level().getEntitiesOfClass(
                        Monster.class,
                        player.getBoundingBox().inflate(14.0),
                        h -> h.isAlive() && !h.getTags().contains(TAG_FAMILIAR) && !familiars.contains(h)
                );
                if (!hostiles.isEmpty()) {
                    combatTarget = hostiles.get(0);
                }
            }

            for (Mob familiar : familiars) {
                // Absolute pacification towards owner
                pacifyTowardsOwner(familiar, player);

                // Command attack or follow
                if (combatTarget != null) {
                    commandAttackTarget(familiar, combatTarget);
                } else {
                    clearAttackTargets(familiar);
                    tickFollowOwner(familiar, player);
                }
            }
        });

        // Protect owner and familiars from friendly fire and coordinate attack/defense
        EntityEvent.LIVING_HURT.register((livingEntity, damageSource, amount) -> {
            Entity attacker = damageSource.getEntity();

            // 1. Familiar attacking owner -> cancel damage & pacify
            if (attacker instanceof Mob familiar && familiar.getTags().contains(TAG_FAMILIAR)) {
                UUID ownerUUID = getOwnerUUID(familiar);
                if (ownerUUID != null && livingEntity.getUUID().equals(ownerUUID)) {
                    familiar.setTarget(null);
                    if (livingEntity instanceof Player owner) {
                        pacifyTowardsOwner(familiar, owner);
                    }
                    return EventResult.interruptFalse();
                }
            }

            // 2. Owner attacking familiar -> cancel damage (prevent accidental friendly fire) & pacify
            if (attacker instanceof Player player && livingEntity instanceof Mob familiar && familiar.getTags().contains(TAG_FAMILIAR)) {
                UUID ownerUUID = getOwnerUUID(familiar);
                if (ownerUUID != null && player.getUUID().equals(ownerUUID)) {
                    pacifyTowardsOwner(familiar, player);
                    return EventResult.interruptFalse();
                }
            }

            // 3. Owner gets hurt by enemy -> nearby familiars retaliate
            if (livingEntity instanceof Player owner && attacker instanceof LivingEntity hostileAttacker && !(hostileAttacker instanceof Player)) {
                commandNearbyFamiliars(owner, hostileAttacker);
            }

            // 4. Owner attacks enemy -> nearby familiars join attack
            if (attacker instanceof Player owner && livingEntity instanceof LivingEntity victim && !(victim instanceof Player)) {
                commandNearbyFamiliars(owner, victim);
            }

            return EventResult.pass();
        });
    }

    public static void makeCompanion(Entity entity, Player player) {
        if (entity instanceof TamableAnimal tamable) {
            tamable.tame(player);
        }

        if (entity instanceof Mob mob) {
            mob.addTag(TAG_FAMILIAR);
            mob.addTag(TAG_OWNER_PREFIX + player.getUUID().toString());
            mob.setPersistenceRequired();

            Component origName = mob.getType().getDescription();
            mob.setCustomName(Component.translatable("tooltip.mushokucraft.familiar_name_format", origName));
            mob.setCustomNameVisible(true);

            pacifyTowardsOwner(mob, player);
            attachGoals(mob, player.getUUID());
        }
    }

    public static void pacifyTowardsOwner(Mob familiar, Player owner) {
        // Standard mob targeting
        if (familiar.getTarget() == owner) {
            familiar.setTarget(null);
        }
        if (familiar.getLastHurtByMob() == owner) {
            familiar.setLastHurtByMob(null);
        }

        // Neutral mobs (Zombified Piglins, Iron Golems, Endermen, Bees, etc.)
        if (familiar instanceof NeutralMob neutral) {
            if (owner.getUUID().equals(neutral.getPersistentAngerTarget())) {
                neutral.stopBeingAngry();
                neutral.setPersistentAngerTarget(null);
            }
        }

        // Brain-based mobs (Warden, Piglins, etc.)
        Brain<?> brain = familiar.getBrain();
        if (brain != null) {
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) &&
                    brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == owner) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (brain.hasMemoryValue(MemoryModuleType.ROAR_TARGET) &&
                    brain.getMemory(MemoryModuleType.ROAR_TARGET).orElse(null) == owner) {
                brain.eraseMemory(MemoryModuleType.ROAR_TARGET);
            }
            if (brain.hasMemoryValue(MemoryModuleType.DISTURBANCE_LOCATION)) {
                brain.eraseMemory(MemoryModuleType.DISTURBANCE_LOCATION);
            }
            if (brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }
        }

        // Warden specific anger management: clear anger towards owner on every tick
        if (familiar instanceof Warden warden) {
            warden.clearAnger(owner);
        }
    }

    public static void commandAttackTarget(Mob familiar, LivingEntity target) {
        if (target == null || !target.isAlive() || target.getTags().contains(TAG_FAMILIAR)) return;

        // Standard goal navigation & attack
        if (familiar.getTarget() != target) {
            familiar.setTarget(target);
        }

        // Warden specific targeting & roaring
        if (familiar instanceof Warden warden) {
            Brain<Warden> brain = warden.getBrain();
            if (brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) != target) {
                warden.setAttackTarget(target);
            }
        } else if (familiar.getBrain() != null) {
            familiar.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
        }
    }

    public static void clearAttackTargets(Mob familiar) {
        if (familiar.getTarget() != null && !familiar.getTarget().isAlive()) {
            familiar.setTarget(null);
        }
        if (familiar instanceof Warden warden) {
            LivingEntity at = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (at != null && !at.isAlive()) {
                warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
            }
        }
    }

    public static void tickFollowOwner(Mob familiar, Player owner) {
        double distSq = familiar.distanceToSqr(owner);

        // Teleport if too far (> 24 blocks)
        if (distSq > 24.0 * 24.0) {
            teleportNearOwner(familiar, owner);
            return;
        }

        // Move towards owner if farther than 4.5 blocks
        if (distSq > 4.5 * 4.5) {
            familiar.getNavigation().moveTo(owner, 1.25);
            if (familiar instanceof Warden warden) {
                warden.getBrain().setMemory(
                        MemoryModuleType.WALK_TARGET,
                        new WalkTarget(new EntityTracker(owner, false), 1.25f, 3)
                );
            }
        } else if (distSq < 3.0 * 3.0) {
            familiar.getNavigation().stop();
            if (familiar instanceof Warden warden) {
                warden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        }
    }

    public static void teleportNearOwner(Mob mob, Player owner) {
        BlockPos base = owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            int ox = mob.getRandom().nextInt(5) - 2;
            int oz = mob.getRandom().nextInt(5) - 2;
            BlockPos targetPos = base.offset(ox, 0, oz);

            if (mob.level().getBlockState(targetPos).isAir() && mob.level().getBlockState(targetPos.below()).isSolid()) {
                mob.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                mob.getNavigation().stop();

                if (mob.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            12, 0.3, 0.3, 0.3, 0.05);
                    sl.playSound(null, targetPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.NEUTRAL, 0.8f, 1.2f);
                }
                return;
            }
        }
    }

    private static java.lang.reflect.Field goalSelectorField;
    private static java.lang.reflect.Field targetSelectorField;

    static {
        try {
            for (java.lang.reflect.Field f : Mob.class.getDeclaredFields()) {
                if (net.minecraft.world.entity.ai.goal.GoalSelector.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    if (goalSelectorField == null) {
                        goalSelectorField = f;
                    } else if (targetSelectorField == null) {
                        targetSelectorField = f;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static net.minecraft.world.entity.ai.goal.GoalSelector getGoalSelector(Mob mob) {
        try {
            if (goalSelectorField != null) return (net.minecraft.world.entity.ai.goal.GoalSelector) goalSelectorField.get(mob);
        } catch (Exception ignored) {}
        return null;
    }

    private static net.minecraft.world.entity.ai.goal.GoalSelector getTargetSelector(Mob mob) {
        try {
            if (targetSelectorField != null) return (net.minecraft.world.entity.ai.goal.GoalSelector) targetSelectorField.get(mob);
        } catch (Exception ignored) {}
        return null;
    }

    public static UUID getOwnerUUID(Mob mob) {
        for (String tag : mob.getTags()) {
            if (tag.startsWith(TAG_OWNER_PREFIX)) {
                try {
                    return UUID.fromString(tag.substring(TAG_OWNER_PREFIX.length()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    private static void attachGoals(Mob mob, UUID ownerUUID) {
        net.minecraft.world.entity.ai.goal.GoalSelector gs = getGoalSelector(mob);
        net.minecraft.world.entity.ai.goal.GoalSelector ts = getTargetSelector(mob);
        if (gs != null) {
            gs.addGoal(1, new FamiliarFollowGoal(mob, ownerUUID, 1.25, 7.0f, 2.5f, 24.0f));
        }
        if (ts != null) {
            ts.addGoal(1, new FamiliarDefendOwnerGoal(mob, ownerUUID));
            ts.addGoal(2, new FamiliarAttackOwnerTargetGoal(mob, ownerUUID));
        }
    }

    private static void commandNearbyFamiliars(Player owner, LivingEntity target) {
        if (owner.level().isClientSide() || target == null || !target.isAlive()) return;
        AABB box = owner.getBoundingBox().inflate(24.0);
        List<Mob> familiars = owner.level().getEntitiesOfClass(Mob.class, box,
                m -> m.getTags().contains(TAG_FAMILIAR) && owner.getUUID().equals(getOwnerUUID(m)));

        for (Mob familiar : familiars) {
            commandAttackTarget(familiar, target);
        }
    }

    // Goal: Follow Owner and Teleport when too far away
    public static class FamiliarFollowGoal extends Goal {
        private final Mob mob;
        private final UUID ownerUUID;
        private final double speedModifier;
        private final float startDistanceSq;
        private final float stopDistanceSq;
        private final float teleportDistanceSq;

        public FamiliarFollowGoal(Mob mob, UUID ownerUUID, double speed, float startDist, float stopDist, float tpDist) {
            this.mob = mob;
            this.ownerUUID = ownerUUID;
            this.speedModifier = speed;
            this.startDistanceSq = startDist * startDist;
            this.stopDistanceSq = stopDist * stopDist;
            this.teleportDistanceSq = tpDist * tpDist;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || !owner.isAlive() || owner.isSpectator()) return false;
            return mob.distanceToSqr(owner) > startDistanceSq;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = getOwner();
            if (owner == null || !owner.isAlive() || owner.isSpectator()) return false;
            return mob.distanceToSqr(owner) > stopDistanceSq;
        }

        @Override
        public void tick() {
            Player owner = getOwner();
            if (owner == null) return;

            // Clear target if accidentally set to owner
            if (mob.getTarget() != null && mob.getTarget().getUUID().equals(ownerUUID)) {
                mob.setTarget(null);
            }

            mob.getLookControl().setLookAt(owner, 10.0f, mob.getMaxHeadXRot());
            double distSq = mob.distanceToSqr(owner);

            if (distSq >= teleportDistanceSq) {
                SummonCompanionManager.teleportNearOwner(mob, owner);
            } else if (distSq > stopDistanceSq) {
                mob.getNavigation().moveTo(owner, speedModifier);
            } else {
                mob.getNavigation().stop();
            }
        }

        private Player getOwner() {
            return mob.level().getPlayerByUUID(ownerUUID);
        }
    }

    // Goal: Defend Owner from attackers
    public static class FamiliarDefendOwnerGoal extends TargetGoal {
        private final UUID ownerUUID;
        private LivingEntity attacker;

        public FamiliarDefendOwnerGoal(Mob mob, UUID ownerUUID) {
            super(mob, false);
            this.ownerUUID = ownerUUID;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = this.mob.level().getPlayerByUUID(ownerUUID);
            if (owner == null) return false;
            this.attacker = owner.getLastHurtByMob();
            if (this.attacker == null || !this.attacker.isAlive() || this.attacker.getUUID().equals(ownerUUID)) {
                return false;
            }
            return canAttack(this.attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            SummonCompanionManager.commandAttackTarget(this.mob, this.attacker);
            super.start();
        }
    }

    // Goal: Attack whatever Owner is attacking
    public static class FamiliarAttackOwnerTargetGoal extends TargetGoal {
        private final UUID ownerUUID;
        private LivingEntity victim;

        public FamiliarAttackOwnerTargetGoal(Mob mob, UUID ownerUUID) {
            super(mob, false);
            this.ownerUUID = ownerUUID;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = this.mob.level().getPlayerByUUID(ownerUUID);
            if (owner == null) return false;
            this.victim = owner.getLastHurtMob();
            if (this.victim == null || !this.victim.isAlive() || this.victim.getUUID().equals(ownerUUID)) {
                return false;
            }
            return canAttack(this.victim, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            SummonCompanionManager.commandAttackTarget(this.mob, this.victim);
            super.start();
        }
    }
}
