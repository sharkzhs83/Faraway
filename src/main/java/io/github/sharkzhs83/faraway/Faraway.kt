package io.github.sharkzhs83.faraway

import io.github.monun.kommand.kommand
import io.github.sharkzhs83.faraway.menu.Menu
import io.github.sharkzhs83.faraway.upgradedEntity.UpgradedEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Chest
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File


class Faraway : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(UpgradedEntity(), this)
        server.pluginManager.registerEvents(Menu(), this)
        saveDefaultConfig()

        this.kommand {
            register("faraway") {
                then("away") {
                    requires { isPlayer }

                    executes {
                        val range = -500 .. 500
                        val x = range.random()
                        val z = range.random()
                        val location : Location = Location(Bukkit.getWorld("world"),x.toDouble(),200.0,z.toDouble())

                        (sender as Player).teleport(location)
                        (sender as Player).addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 1200, 1, false, false))
                    }
                }
                then("rebalance") {

                    executes {
                        for(player in Bukkit.getOnlinePlayers()) {
                            val attack_speed = config.getInt("${player.name} attack_speed")
                            val attack_damage = config.getInt("${player.name} attack_damage")
                            val max_health = config.getInt("${player.name} max_health")
                            val movement_speed = config.getInt("${player.name} movement_speed")

                            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = 4.0

                            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue?.plus(
                                config.getDouble("attack_speed") * attack_speed
                            )!!

                            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 1.0

                            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue?.plus(
                                config.getDouble("attack_damage") * attack_damage
                            )!!

                            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0

                            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.plus(
                                config.getDouble("max_health") * max_health
                            )!!

                            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.1

                            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue?.plus(
                                config.getDouble("movement_speed") * movement_speed
                            )!!
                        }
                    }
                }
            }
        }




        //Events

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            val range = 1..4
            val num = range.random()

            if (num == 1) {

                for (player in Bukkit.getOnlinePlayers()) {
                    val range1 = -50..50
                    val range2 = 35..100

                    val x = player.location.x + range1.random()
                    val y = range2.random()
                    val z = player.location.z + range1.random()

                    val loc: Location = Location(Bukkit.getWorld("world"), x, y.toDouble(), z)

                    val targetBlock = Bukkit.getWorld("world")?.getBlockAt(loc)

                    if (targetBlock != null) {
                        targetBlock.type = Material.CHEST
                    }

                    val chest = targetBlock?.getState() as Chest
                    val inv = chest.inventory

                    val chestRange = 0..26

                    for (i in 1..9) {
                        val chestNum = chestRange.random()
                        val itemRange = 1..9
                        val itemNum = itemRange.random()
                        var item = ItemStack(Material.AIR)

                        if (itemNum == 1) {
                            val amountRange = 1..3
                            val amount = amountRange.random()
                            item = ItemStack(Material.EMERALD)
                            item.amount = amount
                        } else if (itemNum == 2) {
                            val amountRange = 1..2
                            val amount = amountRange.random()
                            item = ItemStack(Material.DIAMOND)
                            item.amount = amount
                        } else if (itemNum == 3) {
                            val amountRange = 1..10
                            val amount = amountRange.random()
                            item = ItemStack(Material.BROWN_DYE)
                            item.amount = amount
                        } else if (itemNum == 4) {
                            val amountRange = 1..5
                            val amount = amountRange.random()
                            item = ItemStack(Material.GOLD_INGOT)
                            item.amount = amount
                        } else if (itemNum == 5) {
                            val amountRange = 1..5
                            val amount = amountRange.random()
                            item = ItemStack(Material.IRON_INGOT)
                            item.amount = amount
                        } else if (itemNum == 6) {
                            val amountRange = 1..5
                            val amount = amountRange.random()
                            item = ItemStack(Material.IRON_INGOT)
                            item.amount = amount
                        } else if (itemNum == 7) {
                            val amountRange = 1..5
                            val amount = amountRange.random()
                            item = ItemStack(Material.IRON_INGOT)
                            item.amount = amount
                        } else if (itemNum == 8) {
                            val amountRange = 1..5
                            val amount = amountRange.random()
                            item = ItemStack(Material.EMERALD)
                            item.amount = amount
                        } else if (itemNum == 9) {
                            val amountRange = 1..1
                            val amount = amountRange.random()

                            val templateRange = 1..17
                            when (templateRange.random()) {
                                1 -> item = ItemStack(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
                                2 -> item = ItemStack(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                3 -> item = ItemStack(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                4 -> item = ItemStack(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE)
                                5 -> item = ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                                6 -> item = ItemStack(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE)
                                7 -> item = ItemStack(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
                                8 -> item = ItemStack(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
                                9 -> item = ItemStack(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE)
                                10 -> item = ItemStack(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                11 -> item = ItemStack(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                                12 -> item = ItemStack(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                13 -> item = ItemStack(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                14 -> item = ItemStack(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
                                15 -> item = ItemStack(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
                                16 -> item = ItemStack(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE)
                                17 -> item = ItemStack(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE)
                            }
                            item.amount = amount
                        }


                        inv.setItem(chestNum, item)
                    }

                    player.sendMessage(Component.text("${x.toInt()},${y.toInt()},${z.toInt()} 근처에 보급이 생성되었습니다!").color(TextColor.color(0, 255, 179)))
                }
            }

            else if (num == 2) {

                Bukkit.broadcast(Component.text("잠시후 한 플레이어가 차가워지거나 뜨거워집니다!").color(TextColor.color(150, 0, 0)))

                val player = Bukkit.getOnlinePlayers().random()
                val numrange = 1..2
                val num = numrange.random()

                Bukkit.getScheduler().scheduleSyncDelayedTask(this,
                    {
                        if (num == 1) {
                            player.fireTicks = 400
                            Bukkit.broadcast(
                                Component.text("${player.name}이(가) 뜨거워졌습니다!").color(TextColor.color(150, 0, 0))
                            )
                        } else if (num == 2) {
                            player.freezeTicks = 1200
                            Bukkit.broadcast(
                                Component.text("${player.name}이(가) 차가워졌습니다!").color(TextColor.color(150, 0, 0))
                            )
                        }


                    }, 60L
                )


            }

            else if (num == 3) {
                for (player in Bukkit.getOnlinePlayers()) {
                    player.world.spawnEntity(player.location, EntityType.LIGHTNING)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this,
                        {
                            Bukkit.broadcast(Component.text("짜잔~").color(TextColor.color(0,255,179)))
                        }, 30L)
                }
            }

            else if (num == 4) {
                for (player in Bukkit.getOnlinePlayers()) {
                    Bukkit.broadcast(Component.text("잠시후 모든 플레이어에게 마녀가 소환됩니다!").color(TextColor.color(0,255,179)))

                    Bukkit.getScheduler().scheduleSyncDelayedTask(this,
                        {
                            for (player in Bukkit.getOnlinePlayers()) {
                                player.world.spawnEntity(player.location, EntityType.WITCH)
                            }

                        }, 60L
                    )

                }
            }
        }
            ,20L, 6000L)

    }



    @EventHandler
    fun onPlayerSetConduit(event: BlockPlaceEvent) {
        if(event.block.type == Material.CONDUIT ) {
            if (config.getBoolean("${event.player.name} is setConduit")){
                event.isCancelled = true
                event.player.sendMessage(Component.text("이미 전달체가 설치 되었습니다!").color(TextColor.color(150,0,0)))
            }
            else if(event.block.world.name != "world") {
                event.player.sendMessage(Component.text("전달체는 오버월드에서만 설치 될 수 있습니다!").color(TextColor.color(150,0,0)))
                event.isCancelled = true
            }
            else {
                Bukkit.broadcast(Component.text("${event.player.name}의 전달체가 설치되었습니다!").color(TextColor.color(0,255,179)))
                event.player.inventory.setItemInMainHand(ItemStack(Material.HEART_OF_THE_SEA))
                config.set(event.player.name, event.block.location)
                config.set("${event.player.name} is setConduit", true)
                saveConfig()

                for (player in Bukkit.getServer().onlinePlayers) {
                    player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, 0.7f, 1.3f)
                }
            }
        }
    }


    @EventHandler
    fun onPlayerBreakConduit(event: BlockBreakEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (config.getLocation(player.name) == event.block.location) {
                Bukkit.broadcast(Component.text("${event.player.name}이 ${player.name}의 전달체를 파괴했습니다!").color(TextColor.color(0,255,179)))
                player.gameMode = GameMode.SPECTATOR
            }
        }
    }


    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {

        event.deathMessage(Component.text("${event.player.name} 개못하네").color(TextColor.color(0,255,179)))

    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageEvent) {
        if(event.entity is Player) {
            if(event.cause != EntityDamageEvent.DamageCause.FALL && event.cause != EntityDamageEvent.DamageCause.FIRE_TICK && event.cause != EntityDamageEvent.DamageCause.DROWNING &&
                event.cause != EntityDamageEvent.DamageCause.FREEZE && event.cause != EntityDamageEvent.DamageCause.HOT_FLOOR && event.cause != EntityDamageEvent.DamageCause.POISON &&
                event.cause != EntityDamageEvent.DamageCause.MAGIC && event.cause != EntityDamageEvent.DamageCause.WORLD_BORDER) {
                if(!config.getInt("${event.entity.name} defend").equals(null)) {
                    event.damage -= config.getInt("${event.entity.name} defend") * config.getDouble("defend") / 4 + 1
                }
            }
        }
    }

    override fun onDisable() {

    }
}