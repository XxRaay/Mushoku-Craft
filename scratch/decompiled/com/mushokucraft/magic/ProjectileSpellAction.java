/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mushokucraft.magic;

import com.mushokucraft.magic.Spell;
import com.mushokucraft.magic.SpellAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ProjectileSpellAction
implements SpellAction {
    @Override
    public void execute(Level level, ServerPlayer player, Spell spell) {
        if (spell.getProjectileFactory() != null) {
            Projectile projectile = spell.getProjectileFactory().create(level, player);
            Vec3 look = player.getLookAngle();
            projectile.shoot(look.x, look.y, look.z, spell.getBaseSpeed(), spell.getBaseInaccuracy());
            level.addFreshEntity((Entity)projectile);
        }
    }
}

