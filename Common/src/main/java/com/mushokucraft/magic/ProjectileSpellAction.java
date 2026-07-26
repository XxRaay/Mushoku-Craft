package com.mushokucraft.magic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Default Strategy for projectile-based spells.
 */
public class ProjectileSpellAction implements SpellAction {
    @Override
    public void execute(Level level, ServerPlayer player, Spell spell) {
        if (spell.isChanneled()) {
            if (ServerChargeManager.buttonHeldPlayers.contains(player.getUUID())) {
                ServerChargeManager.startChanneledSpell(player, spell.getId());
            }
            return;
        }

        if (spell.getProjectileFactory() != null) {
            Projectile projectile = spell.getProjectileFactory().create(level, player);
            Vec3 look = player.getLookAngle();
            projectile.shoot(look.x, look.y, look.z, spell.getBaseSpeed(), spell.getBaseInaccuracy());
            level.addFreshEntity(projectile);
        }
    }
}





