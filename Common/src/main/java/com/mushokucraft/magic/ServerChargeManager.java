package com.mushokucraft.magic;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModSpells;
import com.mushokucraft.magic.ServerCastManager;
import com.mushokucraft.magic.Spell;
import com.mushokucraft.magic.entity.IMagicProjectile;
import com.mushokucraft.network.CastStartedPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.architectury.networking.NetworkManager;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.PlayerEvent;

public class ServerChargeManager {
    private static final Map<UUID, ChargeData> chargingPlayers = new HashMap<UUID, ChargeData>();
    private static final Map<UUID, SilenceData> activeSilences = new HashMap<UUID, SilenceData>();
    public static final java.util.Set<UUID> buttonHeldPlayers = new java.util.HashSet<>();

    public static void register() {
        TickEvent.PLAYER_POST.register((player) -> {
            if (player instanceof ServerPlayer) {
                ChargeData chargeData;
                ServerPlayer player2 = (ServerPlayer)player;
                SilenceData silence = activeSilences.get(player2.getUUID());
                if (silence != null) {
                    --silence.ticks;
                    if (silence.ticks <= 0) {
                        activeSilences.remove(player2.getUUID());
                        Level level = player2.level();
                        Vec3 look = player2.getLookAngle();
                        DashResult dash = ServerChargeManager.performDash(player2, (Double)MushokuConfig.LONGSWORD_SILENCE_DASH_DISTANCE.get());
                        Vec3 oldEyePos = dash.oldEyePos;
                        Vec3 newEyePos = dash.newEyePos;
                        level.playSound(null, player2.getX(), player2.getY(), player2.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 3.0f, 2.0f);
                        level.playSound(null, oldEyePos.x, oldEyePos.y, oldEyePos.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2.0f, 1.5f);
                        if (level instanceof ServerLevel) {
                            ServerLevel serverLevel = (ServerLevel)level;
                            double distance = oldEyePos.distanceTo(newEyePos);
                            Vec3 dir = newEyePos.subtract(oldEyePos).normalize();
                            for (double d = 0.0; d <= distance; d += 0.25) {
                                Vec3 pos = oldEyePos.add(dir.scale(d));
                                serverLevel.sendParticles((ParticleOptions)ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
                                serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.0);
                            }
                        }
                        for (LivingEntity target : ServerChargeManager.getEntitiesInDashCylinder(player2, level, oldEyePos, newEyePos, look, (Double)MushokuConfig.LONGSWORD_SILENCE_HITBOX_INFLATE.get(), (Double)MushokuConfig.LONGSWORD_SILENCE_HIT_RADIUS_SQ.get())) {
                            boolean wasAlive = target.isAlive();
                            DamageSource source = new DamageSource((Holder)level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.LONGSWORD_SILENCE), (Entity)player2, (Entity)player2);
                            target.hurt(source, ((Double)MushokuConfig.LONGSWORD_SILENCE_DAMAGE.get()).floatValue());
                            if (!(level instanceof ServerLevel)) continue;
                            ServerLevel serverLevel = (ServerLevel)level;
                            serverLevel.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + (double)target.getBbHeight() / 2.0, target.getZ(), 5, 1.0, 1.0, 1.0, 0.0);
                            if (!wasAlive || !target.isDeadOrDying()) continue;
                            target.setInvisible(true);
                            serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, target.getX(), target.getY() + (double)target.getBbHeight() / 2.0, target.getZ(), 30, (double)target.getBbWidth(), (double)target.getBbHeight() / 2.0, (double)target.getBbWidth(), 0.1);
                            serverLevel.sendParticles((ParticleOptions)ParticleTypes.POOF, target.getX(), target.getY() + (double)target.getBbHeight() / 2.0, target.getZ(), 10, (double)target.getBbWidth(), (double)target.getBbHeight() / 2.0, (double)target.getBbWidth(), 0.05);
                        }
                    }
                }
                if ((chargeData = chargingPlayers.get(player2.getUUID())) != null) {
                    Spell spell = ModSpells.SPELLS.get(chargeData.spellId);
                    boolean isChanneled = spell != null && (spell.isChanneled() || chargeData.spellId.getPath().equals("icicle_break"));
                    if (chargeData.ticks < (Integer)MushokuConfig.CHARGE_MAX_TICKS.get() || isChanneled) {
                        PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player2);
                    if (data.consumeMana(chargeData.maxManaDrainPerTick)) {
                        chargeData.totalManaSpent += chargeData.maxManaDrainPerTick;
                        ++chargeData.ticks;
                        float progress = (float)chargeData.ticks / (float)((Integer)MushokuConfig.CHARGE_MAX_TICKS.get()).intValue();
                        float newScale = ((Double)MushokuConfig.CHARGE_MIN_SCALE.get()).floatValue() + progress * (((Double)MushokuConfig.CHARGE_MAX_SCALE.get()).floatValue() - ((Double)MushokuConfig.CHARGE_MIN_SCALE.get()).floatValue());
                        Projectile projectile = chargeData.entity;
                        if (projectile instanceof IMagicProjectile) {
                            IMagicProjectile chargeable = (IMagicProjectile)projectile;
                            chargeable.setChargeScale(newScale);
                        }
                        if (chargeData.ticks % 5 == 0) {
                            ModGameEvents.syncMana(player2, data);
                        }
                    } else {
                        ServerChargeManager.releaseCharge(player2);
                        ModGameEvents.syncMana(player2, data);
                    }
                    }
                }
            }
        });

        PlayerEvent.PLAYER_QUIT.register((player) -> {
            chargingPlayers.remove(player.getUUID());
            activeSilences.remove(player.getUUID());
            buttonHeldPlayers.remove(player.getUUID());
        });
    }

    public static boolean isCharging(ServerPlayer player) {
        return chargingPlayers.containsKey(player.getUUID());
    }

    public static void startCharge(ServerPlayer player, ResourceLocation spellId) {
        buttonHeldPlayers.add(player.getUUID());
        Spell spell = ModSpells.SPELLS.get(spellId);
        if (spell == null) {
            return;
        }
        PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
        float spellMastery = data.getSpellMastery(spellId);
        if (spellMastery < 1.0f) {
            float manaCost = spell.getEffectiveManaCost(data);
            if (data.getMana() < manaCost) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.not_enough_mana"), true);
                return;
            }
            float castReduction = com.mushokucraft.accessory.AccessoryHelper.getCastTimeReduction(player);
            boolean immune = com.mushokucraft.accessory.AccessoryHelper.isFizzleImmune(player);
            float castTimeTicks = spell.getCastTime(spellMastery);
            int effectiveCastTime = Math.max(5, (int)(castTimeTicks * (1.0f - castReduction)));
            int fizzleTick = -1;
            if (effectiveCastTime > 0) {
                if (!immune) {
                    float fizzleChance = spell.getFizzleChance(spellMastery, data);
                    if (player.getRandom().nextFloat() < fizzleChance) {
                        float fizzlePoint = 0.2f + player.getRandom().nextFloat() * 0.7f;
                        fizzleTick = (int)((float)effectiveCastTime * fizzlePoint);
                    }
                }
                ServerCastManager.startCast(player, spell, effectiveCastTime, fizzleTick);
                NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new CastStartedPacket(spell.getId(), effectiveCastTime, fizzleTick));
            } else if (spellId.getPath().equals("longsword_light")) {
                ServerChargeManager.handleLongswordLight(player, data, manaCost);
            } else if (spellId.getPath().equals("longsword_of_silence")) {
                ServerChargeManager.handleLongswordOfSilence(player, data);
            } else {
                ServerChargeManager.handleInstantProjectileCast(player, spell, data, manaCost);
            }
            return;
        }
        if (chargingPlayers.containsKey(player.getUUID())) {
            return;
        }
        float initialCost = spell.getEffectiveManaCost(data);
        if (!data.consumeMana(initialCost)) {
            player.displayClientMessage(Component.translatable("message.mushokucraft.not_enough_mana"), true);
            return;
        }
        Projectile magicProjectile = spell.getProjectileFactory().create(player.level(), player);
        if (magicProjectile instanceof IMagicProjectile) {
            IMagicProjectile chargeable = (IMagicProjectile)magicProjectile;
            chargeable.setCharging(true);
            chargeable.setChargeScale(0.1f);
        }
        if (magicProjectile != null) {
            player.level().addFreshEntity((Entity)magicProjectile);
            float maxManaDrainPerTick = ((Double)MushokuConfig.CHARGE_TOTAL_MANA_DRAIN.get()).floatValue() / (float)((Integer)MushokuConfig.CHARGE_MAX_TICKS.get()).intValue();
            ChargeData chargeData = new ChargeData(spellId, magicProjectile, maxManaDrainPerTick);
            chargeData.totalManaSpent = initialCost;
            chargingPlayers.put(player.getUUID(), chargeData);
            ModGameEvents.syncMana(player, data);
        }
    }

    private static void handleLongswordLight(ServerPlayer player, PlayerMasteryData data, float manaCost) {
        if (data.getActiveStance() != SwordStyle.SWORD_GOD) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.requires_sword_god_stance"), true);
            return;
        }
        if (data.getStanceMastery(SwordStyle.SWORD_GOD) < 0.99f) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.requires_max_mastery"), true);
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof SwordItem)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.requires_sword"), true);
            return;
        }
        if (player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
            return;
        }
        if (!data.consumeMana(manaCost)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.not_enough_mana"), true);
            return;
        }
        Level level = player.level();
        Vec3 look = player.getLookAngle();
        DashResult dash = ServerChargeManager.performDash(player, (Double)MushokuConfig.LONGSWORD_LIGHT_DASH_DISTANCE.get());
        Vec3 oldEyePos = dash.oldEyePos;
        Vec3 newEyePos = dash.newEyePos;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0f, 2.0f);
        level.playSound(null, oldEyePos.x, oldEyePos.y, oldEyePos.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.5f);
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            double distance = oldEyePos.distanceTo(newEyePos);
            Vec3 dir = newEyePos.subtract(oldEyePos).normalize();
            for (double d = 0.0; d <= distance; d += 0.25) {
                Vec3 pos = oldEyePos.add(dir.scale(d));
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        for (LivingEntity target : ServerChargeManager.getEntitiesInDashCylinder(player, level, oldEyePos, newEyePos, look, (Double)MushokuConfig.LONGSWORD_LIGHT_HITBOX_INFLATE.get(), (Double)MushokuConfig.LONGSWORD_LIGHT_HIT_RADIUS_SQ.get())) {
            DamageSource source = new DamageSource((Holder)level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.LONGSWORD_SILENCE), (Entity)player, (Entity)player);
            target.hurt(source, ((Double)MushokuConfig.LONGSWORD_LIGHT_DAMAGE.get()).floatValue());
            if (!(level instanceof ServerLevel)) continue;
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.CRIT, target.getX(), target.getY() + (double)target.getBbHeight() / 2.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.2);
        }
        player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), ((Integer)MushokuConfig.LONGSWORD_LIGHT_COOLDOWN_TICKS.get()).intValue());
        data.setItemCooldownEnd(BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()), System.currentTimeMillis() + (long)((Integer)MushokuConfig.LONGSWORD_LIGHT_COOLDOWN_TICKS.get()).intValue() * 50L);
    }

    private static void handleLongswordOfSilence(ServerPlayer player, PlayerMasteryData data) {
        if (data.getActiveStance() != SwordStyle.SWORD_GOD) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.requires_sword_god_stance"), true);
            return;
        }
        if (!data.hasUnlockedLongswordOfSilence()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.spell_not_unlocked"), true);
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof SwordItem)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
            return;
        }
        float silenceCost = data.getMaxMana() * ((Double)MushokuConfig.LONGSWORD_SILENCE_MANA_COST_PERCENT.get()).floatValue();
        if (!data.consumeMana(silenceCost)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.not_enough_mana"), true);
            return;
        }
        ModGameEvents.syncMana(player, data);
        activeSilences.put(player.getUUID(), new SilenceData((Integer)MushokuConfig.LONGSWORD_SILENCE_CAST_TICKS.get()));
        player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), ((Integer)MushokuConfig.LONGSWORD_SILENCE_COOLDOWN_TICKS.get()).intValue());
        data.setItemCooldownEnd(BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()), System.currentTimeMillis() + (long)((Integer)MushokuConfig.LONGSWORD_SILENCE_COOLDOWN_TICKS.get()).intValue() * 50L);
    }

    private static void handleInstantProjectileCast(ServerPlayer player, Spell spell, PlayerMasteryData data, float manaCost) {
        if (data.consumeMana(manaCost)) {
            if (spell.getSpellAction() != null) {
                spell.getSpellAction().execute(player.level(), player, spell);
            }
            data.addSpellMastery(spell.getId(), ((Double)MushokuConfig.SPELL_MASTERY_PER_CAST.get()).floatValue());
            data.addSchoolMastery(spell.getSchool(), ((Double)MushokuConfig.SCHOOL_MASTERY_PER_CAST.get()).floatValue());
            ManaProgressionManager.applySpellManaGrowth(player, data, manaCost, spell);
            ModGameEvents.syncMana(player, data);
            ModGameEvents.syncMastery(player, data);
        }
    }

    public static void releaseCharge(ServerPlayer player) {
        buttonHeldPlayers.remove(player.getUUID());
        ChargeData chargeData = chargingPlayers.remove(player.getUUID());
        if (chargeData != null) {
            ServerChargeManager.fireCharge(player, chargeData);
        }
    }

    public static void startChanneledSpell(ServerPlayer player, ResourceLocation spellId) {
        Spell spell = ModSpells.SPELLS.get(spellId);
        if (spell == null || chargingPlayers.containsKey(player.getUUID())) return;
        PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
        Projectile magicProjectile = spell.getProjectileFactory().create(player.level(), player);
        if (magicProjectile instanceof IMagicProjectile) {
            IMagicProjectile chargeable = (IMagicProjectile)magicProjectile;
            chargeable.setCharging(true);
            chargeable.setChargeScale(0.1f);
        }
        if (magicProjectile != null) {
            player.level().addFreshEntity((Entity)magicProjectile);
            float maxManaDrainPerTick = ((Double)MushokuConfig.CHARGE_TOTAL_MANA_DRAIN.get()).floatValue() / (float)((Integer)MushokuConfig.CHARGE_MAX_TICKS.get()).intValue();
            chargingPlayers.put(player.getUUID(), new ChargeData(spellId, magicProjectile, maxManaDrainPerTick));
            ModGameEvents.syncMana(player, data);
        }
    }

    private static void fireCharge(ServerPlayer player, ChargeData chargeData) {
        if (chargeData.entity != null && chargeData.entity.isAlive()) {
            Projectile projectile = chargeData.entity;
            if (projectile instanceof IMagicProjectile) {
                IMagicProjectile chargeable = (IMagicProjectile)projectile;
                chargeable.setCharging(false);
                if (chargeData.ticks < 5) {
                    chargeable.setChargeScale(1.0f);
                }
            }
            float progress = Math.min(1.0f, (float)chargeData.ticks / (float)((Integer)MushokuConfig.CHARGE_MAX_TICKS.get()).intValue());
            float chargeScale = ((Double)MushokuConfig.CHARGE_MIN_SCALE.get()).floatValue() + progress * (((Double)MushokuConfig.CHARGE_MAX_SCALE.get()).floatValue() - ((Double)MushokuConfig.CHARGE_MIN_SCALE.get()).floatValue());
            if (chargeData.ticks < 5) {
                chargeScale = 1.0f;
            }
            Vec3 look = player.getLookAngle();
            chargeData.entity.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            Spell spell = ModSpells.SPELLS.get(chargeData.spellId);
            float baseSpeed = spell != null ? spell.getBaseSpeed() : 1.5f;
            float inaccuracy = spell != null ? spell.getBaseInaccuracy() : 1.0f;
            float speedMult = chargeData.spellId.getPath().equals("rockbullet") ? chargeScale : 1.0f;
            chargeData.entity.shoot(look.x, look.y, look.z, baseSpeed * speedMult, inaccuracy);
            PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
            if (spell != null) {
                data.addSpellMastery(chargeData.spellId, ((Double)MushokuConfig.SPELL_MASTERY_PER_CAST.get()).floatValue() + (float)chargeData.ticks / (float)((Integer)MushokuConfig.CHARGE_MAX_TICKS.get()).intValue() * ((Double)MushokuConfig.CHARGE_MASTERY_BONUS_SPELL.get()).floatValue());
                data.addSchoolMastery(spell.getSchool(), ((Double)MushokuConfig.SCHOOL_MASTERY_PER_CAST.get()).floatValue() + (float)chargeData.ticks / (float)((Integer)MushokuConfig.CHARGE_MAX_TICKS.get()).intValue() * ((Double)MushokuConfig.CHARGE_MASTERY_BONUS_SCHOOL.get()).floatValue());
                ManaProgressionManager.applySpellManaGrowth(player, data, chargeData.totalManaSpent, spell);
                ModGameEvents.syncMastery(player, data);
            }
        }
    }

    private static DashResult performDash(ServerPlayer player, double distance) {
        Vec3 feet;
        Level level = player.level();
        Vec3 look = player.getLookAngle();
        if (look.y < 0.0 && player.onGround()) {
            look = new Vec3(look.x, 0.0, look.z).normalize();
        }
        Vec3 dashDest = feet = player.position();
        for (double d = distance; d > 0.0; d -= 0.25) {
            Vec3 testPos = feet.add(look.scale(d));
            AABB testBox = player.getBoundingBox().move(testPos.subtract(feet)).move(0.0, 0.6, 0.0);
            if (!level.noCollision((Entity)player, testBox)) continue;
            dashDest = testPos;
            break;
        }
        Vec3 oldEyePos = player.getEyePosition();
        player.teleportTo(dashDest.x, dashDest.y, dashDest.z);
        player.resetFallDistance();
        return new DashResult(oldEyePos, player.getEyePosition());
    }

    private static List<LivingEntity> getEntitiesInDashCylinder(ServerPlayer player, Level level, Vec3 oldEyePos, Vec3 newEyePos, Vec3 look, double hitBoxInflate, double hitRadiusSq) {
        AABB boundingBox = new AABB(oldEyePos, newEyePos).inflate(hitBoxInflate);
        List<LivingEntity> rawEntities = level.getEntitiesOfClass(LivingEntity.class, boundingBox, e -> e != player && e.isAlive());
        ArrayList<LivingEntity> hitEntities = new ArrayList<LivingEntity>();
        for (LivingEntity target : rawEntities) {
            Vec3 proj;
            Vec3 targetPos = target.position().add(0.0, (double)target.getBbHeight() / 2.0, 0.0);
            double dot = targetPos.subtract(oldEyePos).dot(look);
            if (!(dot > 0.0) || !(dot < oldEyePos.distanceTo(newEyePos) + 1.0) || !((proj = oldEyePos.add(look.scale(dot))).distanceToSqr(targetPos) < hitRadiusSq)) continue;
            hitEntities.add(target);
        }
        return hitEntities;
    }

    private static class ChargeData {
        final ResourceLocation spellId;
        final Projectile entity;
        final float maxManaDrainPerTick;
        int ticks = 0;
        float totalManaSpent = 0.0f;

        ChargeData(ResourceLocation spellId, Projectile entity, float maxManaDrainPerTick) {
            this.spellId = spellId;
            this.entity = entity;
            this.maxManaDrainPerTick = maxManaDrainPerTick;
        }
    }

    private static class DashResult {
        public final Vec3 oldEyePos;
        public final Vec3 newEyePos;

        public DashResult(Vec3 oldEyePos, Vec3 newEyePos) {
            this.oldEyePos = oldEyePos;
            this.newEyePos = newEyePos;
        }
    }

    private static class SilenceData {
        int ticks;

        SilenceData(int ticks) {
            this.ticks = ticks;
        }
    }
}
