package io.github.sharkzhs83.faraway.upgradedEntity

import com.destroystokyo.paper.event.entity.EnderDragonFlameEvent
import io.github.monun.tap.task.Ticker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.util.Vector

class UpgradedEntity : Listener {

    @EventHandler
    fun onKillEntity(event: EntityDeathEvent) {
        if(event.entity.type != EntityType.PLAYER) {
            val range = 1..5
            val num = range.random()

            if(num == 1) {
                event.drops.add(ItemStack(Material.EMERALD))
            }
        }

        if(event.entityType == EntityType.ENDER_DRAGON) {
            Bukkit.getPluginManager().getPlugin("Faraway")?.config?.set("isDragonDead", true)
            Bukkit.getPluginManager().getPlugin("Faraway")?.saveConfig()
            val item = ItemStack(Material.NETHER_STAR)
            val meta = item.itemMeta
            meta.displayName(Component.text("드래곤의 별").color(TextColor.color(0,150,150)))
            item.itemMeta = meta
            event.drops.add(item)
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
    fun onPlayerEnterEnd(event: PlayerPortalEvent) {

        if(Bukkit.getPluginManager().getPlugin("Faraway")?.config?.getBoolean("isDragonDead")?.equals(null) == true ||
            Bukkit.getPluginManager().getPlugin("Faraway")?.config?.getBoolean("isDragonDead") != true) {

            val scheduler = Bukkit.getScheduler()

            Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                scheduler.scheduleSyncRepeatingTask(it, {

                    if(Bukkit.getPluginManager().getPlugin("Faraway")?.config?.getBoolean("isDragonDead") == true) {
                        Bukkit.getPluginManager().getPlugin("Faraway")?.let { it1 -> scheduler.cancelTasks(it1) }
                    }

                    val range = 1..5
                    val num = range.random()

                    if(num == 1) {
                        for (player in Bukkit.getOnlinePlayers()) {

                            if(player.location.world.name == "world_the_end") {
                                val fireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.FIREBALL)
                                fireball.velocity.zero()
                                fireball.velocity = Vector(0,-2,0)

                                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                    scheduler.scheduleSyncDelayedTask(it, {
                                        val fireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.FIREBALL)
                                        fireball.velocity.zero()
                                        fireball.velocity = Vector(0,-2,0)
                                    },10L)
                                }

                                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                    scheduler.scheduleSyncDelayedTask(it, {
                                        val fireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.FIREBALL)
                                        fireball.velocity.zero()
                                        fireball.velocity = Vector(0,-2,0)
                                    },20L)
                                }

                                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                    scheduler.scheduleSyncDelayedTask(it, {
                                        val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                        dragonFireball.velocity.zero()
                                        dragonFireball.velocity = Vector(0,-2,0)
                                    },30L)
                                }
                            }
                        }
                    }

                    else if(num == 2) {
                        for (player in Bukkit.getOnlinePlayers()) {
                            if(player.location.world.name == "world_the_end") {
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITCH)
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITCH)
                            }
                        }
                    }

                    else if(num == 3) {
                        for (player in Bukkit.getOnlinePlayers()) {
                            if(player.location.world.name == "world_the_end") {
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.SKELETON)
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.SKELETON)
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.SKELETON)
                            }
                        }
                    }

                    else if(num == 4) {
                        for (player in Bukkit.getOnlinePlayers()) {

                            if(player.location.world.name == "world_the_end") {
                                val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                dragonFireball.velocity.zero()
                                dragonFireball.velocity = Vector(0,-2,0)

                                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                    scheduler.scheduleSyncDelayedTask(it, {
                                        val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                        dragonFireball.velocity.zero()
                                        dragonFireball.velocity = Vector(0,-2,0)
                                    },20L)
                                }

                                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                    scheduler.scheduleSyncDelayedTask(it, {
                                        val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                        dragonFireball.velocity.zero()
                                        dragonFireball.velocity = Vector(0,-2,0)
                                    },40L)
                                }

                                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                    scheduler.scheduleSyncDelayedTask(it, {
                                        val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                        dragonFireball.velocity.zero()
                                        dragonFireball.velocity = Vector(0,-2,0)
                                    },60L)
                                }
                            }
                        }
                    }

                    else if(num == 5) {
                        for (player in Bukkit.getOnlinePlayers()) {
                            if(player.location.world.name == "world_the_end") {
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITHER_SKELETON)
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITHER_SKELETON)
                                player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITHER_SKELETON)
                            }
                        }
                    }

                }, 10L, 200L)
            }

        }
    }


    @EventHandler
    fun onDragonRespawn(event: EntitySpawnEvent) {
        if(event.entityType == EntityType.ENDER_DRAGON) {
            Bukkit.getPluginManager().getPlugin("Faraway")?.config?.set("isDragonDead", false)
            Bukkit.getPluginManager().getPlugin("Faraway")?.saveConfig()

            (event.entity as LivingEntity).getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 500.0
            (event.entity as LivingEntity).health = 500.0

            if(Bukkit.getPluginManager().getPlugin("Faraway")?.config?.getBoolean("isDragonDead")?.equals(null) == true ||
                Bukkit.getPluginManager().getPlugin("Faraway")?.config?.getBoolean("isDragonDead") != true) {

                val scheduler = Bukkit.getScheduler()

                Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                    scheduler.scheduleSyncRepeatingTask(it, {

                        if(Bukkit.getPluginManager().getPlugin("Faraway")?.config?.getBoolean("isDragonDead") == true) {
                            Bukkit.getPluginManager().getPlugin("Faraway")?.let { it1 -> scheduler.cancelTasks(it1) }
                        }

                        val range = 1..5
                        val num = range.random()

                        if(num == 1) {
                            for (player in Bukkit.getOnlinePlayers()) {

                                if(player.location.world.name == "world_the_end") {
                                    val fireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.FIREBALL)
                                    fireball.velocity.zero()
                                    fireball.velocity = Vector(0,-2,0)

                                    Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                        scheduler.scheduleSyncDelayedTask(it, {
                                            val fireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.FIREBALL)
                                            fireball.velocity.zero()
                                            fireball.velocity = Vector(0,-2,0)
                                        },10L)
                                    }

                                    Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                        scheduler.scheduleSyncDelayedTask(it, {
                                            val fireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.FIREBALL)
                                            fireball.velocity.zero()
                                            fireball.velocity = Vector(0,-2,0)
                                        },20L)
                                    }

                                    Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                        scheduler.scheduleSyncDelayedTask(it, {
                                            val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                            dragonFireball.velocity.zero()
                                            dragonFireball.velocity = Vector(0,-2,0)
                                        },30L)
                                    }
                                }
                            }
                        }

                        else if(num == 2) {
                            for (player in Bukkit.getOnlinePlayers()) {
                                if(player.location.world.name == "world_the_end") {
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITCH)
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITCH)
                                }
                            }
                        }

                        else if(num == 3) {
                            for (player in Bukkit.getOnlinePlayers()) {
                                if(player.location.world.name == "world_the_end") {
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.SKELETON)
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.SKELETON)
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.SKELETON)
                                }
                            }
                        }

                        else if(num == 4) {
                            for (player in Bukkit.getOnlinePlayers()) {

                                if(player.location.world.name == "world_the_end") {
                                    val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                    dragonFireball.velocity.zero()
                                    dragonFireball.velocity = Vector(0,-2,0)

                                    Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                        scheduler.scheduleSyncDelayedTask(it, {
                                            val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                            dragonFireball.velocity.zero()
                                            dragonFireball.velocity = Vector(0,-2,0)
                                        },20L)
                                    }

                                    Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                        scheduler.scheduleSyncDelayedTask(it, {
                                            val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                            dragonFireball.velocity.zero()
                                            dragonFireball.velocity = Vector(0,-2,0)
                                        },40L)
                                    }

                                    Bukkit.getPluginManager().getPlugin("Faraway")?.let {
                                        scheduler.scheduleSyncDelayedTask(it, {
                                            val dragonFireball = player.world.spawnEntity(player.location.add(0.0,15.0,0.0), EntityType.DRAGON_FIREBALL)
                                            dragonFireball.velocity.zero()
                                            dragonFireball.velocity = Vector(0,-2,0)
                                        },60L)
                                    }
                                }
                            }
                        }

                        else if(num == 5) {
                            for (player in Bukkit.getOnlinePlayers()) {
                                if(player.location.world.name == "world_the_end") {
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITHER_SKELETON)
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITHER_SKELETON)
                                    player.world.spawnEntity(player.location.add(0.0,5.0,0.0), EntityType.WITHER_SKELETON)
                                }
                            }
                        }

                    }, 10L, 200L)
                }

            }
        }
    }
}