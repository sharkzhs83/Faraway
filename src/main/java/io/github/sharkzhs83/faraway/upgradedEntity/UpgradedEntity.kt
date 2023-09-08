package io.github.sharkzhs83.faraway.upgradedEntity

import com.destroystokyo.paper.event.entity.EnderDragonFlameEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.util.Vector

class UpgradedEntity : Listener {

    @EventHandler
    fun onKillEntity(event: EntityDeathEvent) {
        val range = 1..5
        val num = range.random()


        if(num == 1) {
            event.drops.add(ItemStack(Material.EMERALD))
        }
    }



    //스켈레톤
    @EventHandler
    fun onEntityShootBow(event: EntityShootBowEvent) {
        if (event.entityType == EntityType.SKELETON) {
           event.projectile = event.entity.launchProjectile(Fireball::class.java)
        }
    }


    //스켈레톤 / 좀비 / 크리퍼
    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (event.entityType == EntityType.FIREBALL) {
            val entity = event.entity as Fireball
            entity.world.spawnEntity(entity.location, EntityType.ZOMBIE, true)
        }
        else if (event.entityType == EntityType.CREEPER) {
            event.entity.world.spawnEntity(event.entity.location, EntityType.PRIMED_TNT)
        } else if (event.entityType == EntityType.PRIMED_TNT) {
            event.entity.world.spawnEntity(event.entity.location, EntityType.CREEPER)
        }
    }

    //마녀
    @EventHandler
    fun onProjectileLaunch(event: ProjectileHitEvent) {
        if(event.entity.shooter != null)
            if((event.entity.shooter as Entity).type == EntityType.WITCH){
                if(event.hitBlock != null) {
                    event.hitBlock!!.world.spawnEntity(event.hitBlock!!.location.add(0.0,1.5,0.0), EntityType.ZOMBIE)
                }
        }
    }


    //슬라임 / 블레이즈 / 마그마 큐브 / 위더 스켈레톤
    @EventHandler
    fun onEntityDamaged(event: EntityDamageByEntityEvent) {
        if(event.entity is Player) {
            if(event.damager.type == EntityType.SLIME) {
                (event.entity as Player).addPotionEffect(PotionEffect(PotionEffectType.SLOW, 100, 5, false, false))
            }
            else if(event.damager.type == EntityType.BLAZE) {
                (event.entity as Player).addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 2, false, false))
            }
            else if(event.damager.type == EntityType.SMALL_FIREBALL) {
                (event.entity as Player).addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 2, false, false))
            }
            else if(event.damager.type == EntityType.MAGMA_CUBE) {
                (event.entity as Player).fireTicks = 40
            }else if(event.damager.type == EntityType.WITHER_SKELETON) {
                (event.entity as Player).freezeTicks = 100
            }
            else if(event.damager.type == EntityType.PHANTOM) {
                val range = 1..2
                val num = range.random()
                if(num == 1) {
                    event.damager.world.spawnEntity(event.damager.location, EntityType.PHANTOM)
                }
            }

        }
    }

    //드래곤
    @EventHandler
    fun onDragonChangePhase(event: EnderDragonChangePhaseEvent) {
        if(event.newPhase.name == "SEARCH_FOR_BREATH_ATTACK_TARGET") {
            for (player in event.entity.world.players) {

                if(player.world.name == "world_the_end") {
                    val fireball = player.world.spawnEntity(player.location.add(0.0, 10.0, 0.0), EntityType.FIREBALL)
                    fireball.velocity.zero()
                    fireball.velocity = Vector(0,-2, 0)

                    val scheduler : BukkitScheduler = Bukkit.getScheduler()

                    Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {val fireball = player.world.spawnEntity(player.location.add(0.0, 10.0, 0.0), EntityType.FIREBALL)
                        fireball.velocity = Vector(0,-2, 0)}, 20L) }

                    Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {val fireball = player.world.spawnEntity(player.location.add(0.0, 10.0, 0.0), EntityType.FIREBALL)
                        fireball.velocity.zero()
                        fireball.velocity = Vector(0,-2, 0)}, 40L) }

                    Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {val dragonFireball = player.world.spawnEntity(player.location.add(0.0, 10.0, 0.0), EntityType.DRAGON_FIREBALL)
                        dragonFireball.velocity.zero()
                        dragonFireball.velocity = Vector(0,-2, 0)}, 60L) }
                }
            }
        }
        else if(event.newPhase.name == "FLY_TO_PORTAL") {
            for (player in event.entity.world.players) {
                if(player.world.name == "world_the_end") {
                    val witch = player.world.spawnEntity(player.location.add(0.0, 10.0, 0.0), EntityType.WITCH)
                    witch.velocity.zero()
                    witch.velocity = Vector(0,-1, 0)
                }
            }
        }
        else if(event.newPhase.name == "LAND_ON_PORTAL") {
            for (player in event.entity.world.players) {
                if(player.world.name == "world_the_end") {
                    val witch = player.world.spawnEntity(player.location.add(0.0, 4.0, 0.0), EntityType.SKELETON)
                    witch.velocity.zero()
                    witch.velocity = Vector(0,-1, 0)
                }
            }
        }
    }

    //드래곤
    @EventHandler
    fun onEnderDragonFlame(event: EnderDragonFlameEvent) {
        val range = 1..3
        val num = range.random()
        val scheduler : BukkitScheduler = Bukkit.getScheduler()

        Bukkit.broadcast(Component.text("fdas"))

        if(num == 1) {

            event.entity.world.spawnEntity(event.entity.location, EntityType.ZOMBIE)

            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.ZOMBIE)}, 10L) }
            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.ZOMBIE)}, 20L) }
            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.ZOMBIE)}, 30L) }
            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.ZOMBIE)}, 40L) }


        }
        else if(num == 2) {
            event.entity.world.spawnEntity(event.entity.location, EntityType.SKELETON)
            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.SKELETON)}, 10L) }
        }
        else if(num == 3) {
            event.entity.world.spawnEntity(event.entity.location, EntityType.CREEPER)
            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.CREEPER)}, 10L) }
            Bukkit.getPluginManager().getPlugin("Faraway")?.let { scheduler.scheduleSyncDelayedTask(it, {event.entity.world.spawnEntity(event.entity.location.add(0.0,1.5,0.0), EntityType.CREEPER)}, 20L) }
        }
    }
}