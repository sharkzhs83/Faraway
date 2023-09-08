package io.github.sharkzhs83.faraway

import io.github.monun.invfx.InvFX
import io.github.monun.invfx.openFrame
import io.github.sharkzhs83.faraway.upgradedEntity.UpgradedEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin


class Faraway : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(UpgradedEntity(), this)
        saveDefaultConfig()
    }


    @EventHandler
    fun onPlayerSetConduit(event: BlockPlaceEvent) {
        if(event.block.type == Material.CONDUIT ) {
            if (config.getBoolean("${event.player.name} is setConduit")){
                event.isCancelled = true
                event.player.sendMessage(Component.text("이미 전달체가 설치 되었습니다!").color(TextColor.color(150,0,0)))
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
    fun onPlayerInteract(event: PlayerInteractEvent) {

        val conduitItem = ItemStack(Material.CONDUIT)
        val conduitMeta = conduitItem.itemMeta
        conduitMeta.displayName(Component.text("전달체 지급").color(TextColor.color(0,255,179)))
        conduitItem.itemMeta = conduitMeta

        val compassItem = ItemStack(Material.COMPASS)
        val compassMeta = compassItem.itemMeta
        val lore = ArrayList<Component>()
        lore.add(Component.text("에메랄드를 바쳐 에메랄드 갯수 만큼 블록 내의 전달체를 탐지합니다.").color(TextColor.color(0,190,0)))
        lore.add(Component.text("(에메랄드의 갯수는 왼손에 있는 에메랄드의 갯수로 간주합니다.)").color(TextColor.color(169,169,169)))
        lore.add(Component.text("(모든 y좌표에 대해 탐지합니다.)").color(TextColor.color(169,169,169)))
        compassMeta.displayName(Component.text("전달체 탐지").color(TextColor.color(0,150,230)))
        compassMeta.lore(lore)
        compassItem.itemMeta = compassMeta

        val shopItem = ItemStack(Material.EMERALD)
        val shopMeta = shopItem.itemMeta
        shopMeta.displayName(Component.text("상점").color(TextColor.color(128,255,0)))
        shopItem.itemMeta = shopMeta

        if (event.action.isRightClick && event.player.inventory.itemInMainHand == ItemStack(Material.HEART_OF_THE_SEA)) {
            val menuFrame = InvFX.frame(3, Component.text("바다의 심장").color(TextColor.color(0,150,230))) {

                //전달체 지급
                slot(4, 1) {
                    item = conduitItem
                    onClick { clickEvent ->
                        if (clickEvent.isLeftClick) {
                            if(config.getBoolean("${clickEvent.whoClicked.name} get Conduit")) {
                                clickEvent.whoClicked.sendMessage(Component.text("더 이상 전달체를 획득 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                event.player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                            }
                            else {
                                clickEvent.whoClicked.inventory.setItemInMainHand(ItemStack(Material.CONDUIT))
                                config.set("${clickEvent.whoClicked.name} get Conduit", true)
                                saveConfig()
                            }
                            event.player.closeInventory()
                        }
                    }
                }

                //전달체 탐지
                slot(2,1) {
                    item = compassItem
                    onClick { clickEvent ->
                        if(clickEvent.isLeftClick) {
                            if(clickEvent.whoClicked.inventory.itemInOffHand.type == Material.EMERALD) {
                                val amount = clickEvent.whoClicked.inventory.itemInOffHand.amount

                                var isExist = false
                                var playerBlock : Block = clickEvent.whoClicked.location.block
                                val presentBlock : Block = clickEvent.whoClicked.location.block
                                var detectBlockLocX = presentBlock.x
                                var detectBlockLocZ = presentBlock.z
                                var detectBlockLocY : Int

                                for (player in Bukkit.getServer().onlinePlayers) {
                                    for (k : Int in -63..320) {
                                        detectBlockLocY = k
                                        for(i : Int in amount * -1..amount) {
                                            detectBlockLocX = presentBlock.x
                                            detectBlockLocX += i
                                            for (j : Int in amount * -1.. amount) {
                                                detectBlockLocZ = presentBlock.z
                                                detectBlockLocZ += j
                                                if(detectBlockLocX == config.getLocation(player.name)?.x?.toInt() && detectBlockLocZ == config.getLocation(player.name)?.z?.toInt()
                                                    && detectBlockLocY == config.getLocation(player.name)?.y?.toInt()) {
                                                    clickEvent.whoClicked.sendMessage(Component.text("주변 ${amount}칸에 ${player.name}의 전달체가 있습니다!").color(TextColor.color(0,150,230)))
                                                    isExist = true
                                                    player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 0.7f, 1.3f)
                                                    player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                                                }
                                            }
                                        }
                                    }
                                    event.player.closeInventory()
                                }
                                if (!isExist) {
                                    clickEvent.whoClicked.sendMessage(Component.text("주변 ${amount}칸에 전달체가 없습니다!").color(TextColor.color(150,0,0)))
                                    event.player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                    event.player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                                }
                            }
                            else {
                                clickEvent.whoClicked.sendMessage(Component.text("에메랄드가 없습니다!").color(TextColor.color(150,0,0)))
                                event.player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                event.player.closeInventory()
                            }
                        }
                    }
                }

                //상점
                slot(6,1) {
                    item = shopItem
                    onClick {clickEvent ->
                        if(clickEvent.isLeftClick) {


                            val shopFrame = InvFX.frame(3, Component.text("상점")) {


                                val swordItem = ItemStack(Material.NETHERITE_SWORD)
                                val swordMeta = swordItem.itemMeta
                                val swordLore = ArrayList<Component>()
                                swordLore.add(Component.text("☆☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                when (config.getInt("${clickEvent.whoClicked.name} attack_speed")) {
                                    1 -> { swordLore.clear()
                                        swordLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                    }
                                    2 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    3 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    4 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    5 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    6 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    7 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                    8 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                    9 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                    10 -> { swordLore.clear()
                                        swordLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                }
                                swordMeta.lore(swordLore)
                                //최대 강화시 10
                                swordMeta.displayName(Component.text("공격 속도").color(TextColor.color(0,255,179)))
                                swordItem.itemMeta = swordMeta

                                val axeItem = ItemStack(Material.NETHERITE_AXE)
                                val axeMeta = axeItem.itemMeta
                                val axeLore = ArrayList<Component>()
                                axeLore.add(Component.text("☆☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                when (config.getInt("${clickEvent.whoClicked.name} attack_damage")) {
                                    1 -> { axeLore.clear()
                                        axeLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                    }
                                    2 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    3 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    4 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    5 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    6 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    7 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                    8 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                    9 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                    10 -> { axeLore.clear()
                                        axeLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                }
                                axeMeta.lore(axeLore)
                                //최대 강화시 대미지 10추가
                                axeMeta.displayName(Component.text("공격력").color(TextColor.color(0,255,179)))
                                axeItem.itemMeta = axeMeta


                                val totemItem = ItemStack(Material.TOTEM_OF_UNDYING)
                                val totemMeta = totemItem.itemMeta
                                val totemLore = ArrayList<Component>()
                                totemLore.add(Component.text("☆☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                when (config.getInt("${clickEvent.whoClicked.name} max_health")) {
                                    1 -> { totemLore.clear()
                                        totemLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                    }
                                    2 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    3 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    4 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    5 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    6 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    7 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                    8 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                    9 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                    10 -> { totemLore.clear()
                                        totemLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                }
                                totemMeta.lore(totemLore)
                                //최대 강화시 체력60
                                totemMeta.displayName(Component.text("최대 체력").color(TextColor.color(0,255,179)))
                                totemItem.itemMeta = totemMeta

                                val chestItem = ItemStack(Material.NETHERITE_CHESTPLATE)
                                val chestMeta = chestItem.itemMeta
                                val chestLore = ArrayList<Component>()
                                chestLore.add(Component.text("☆☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                when (config.getInt("${clickEvent.whoClicked.name} defend")) {
                                    1 -> { chestLore.clear()
                                        chestLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                    }
                                    2 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    3 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    4 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    5 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    6 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    7 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                    8 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                    9 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                    10 -> { chestLore.clear()
                                        chestLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                }
                                //최대 강화시 받는 데미지 2.5줄임
                                chestMeta.lore(chestLore)
                                chestMeta.displayName(Component.text("방어력").color(TextColor.color(0,255,179)))
                                chestItem.itemMeta = chestMeta

                                val bootsItem = ItemStack(Material.NETHERITE_BOOTS)
                                val bootsMeta = bootsItem.itemMeta
                                val bootsLore = ArrayList<Component>()
                                //최대 강화시 2.5
                                bootsLore.add(Component.text("☆☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                when (config.getInt("${clickEvent.whoClicked.name} movement_speed")) {
                                    1 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                    }
                                    2 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    3 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    4 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    5 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    6 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                    7 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                    8 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                    9 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                    10 -> { bootsLore.clear()
                                        bootsLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                }
                                bootsMeta.lore(bootsLore)
                                bootsMeta.displayName(Component.text("이동 속도").color(TextColor.color(0,255,179)))
                                bootsItem.itemMeta = bootsMeta


                                //가독성 개쓰레기

                                slot(0,1) {
                                    item = swordItem

                                    onClick { clickEvent2 ->

                                        val player = clickEvent2.whoClicked as Player

                                        if(clickEvent2.isLeftClick) {


                                            var emeraldAmount = 0
                                            for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                if (emerald != null) {
                                                    if(emerald.type == Material.EMERALD) {
                                                        emeraldAmount += emerald.amount
                                                    }
                                                }
                                            }

                                            if(emeraldAmount < 12) {
                                                if(config.getInt("${clickEvent2.whoClicked.name} attack_speed") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                }
                                                else {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("에메랄드가 부족합니다! 필요량 : 12개").color(TextColor.color(150,0,0)))
                                                }
                                            }
                                            else {

                                                if (config.getInt("${clickEvent2.whoClicked.name} attack_speed").equals(null)) {
                                                    config.set("${clickEvent2.whoClicked.name} attack_speed", 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} attack_speed")) {
                                                        1 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                                        }
                                                        2 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue?.plus(
                                                        0.6
                                                    )!!
                                                    item!!.lore(swordLore)
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)

                                                    //에메랄드 제거

                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }



                                                }
                                                else if(config.getInt("${clickEvent2.whoClicked.name} attack_speed") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                    player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                                }
                                                else {
                                                    config.set("${clickEvent2.whoClicked.name} attack_speed", config.getInt("${clickEvent2.whoClicked.name} attack_speed") + 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} attack_speed")) {
                                                        1 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        2 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { swordLore.clear()
                                                            swordLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    item!!.lore(swordLore)
                                                    player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue?.plus(
                                                        0.6
                                                    )!!

                                                    //에메랄드 제거
                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                            }


                                        }
                                    }
                                }

                                slot(0,2) {
                                    item = ItemStack(Material.EMERALD)
                                    val itemLore = ArrayList<Component>()
                                    itemLore.add(Component.text("현재 공격속도: ${4 + config.getInt("${clickEvent.whoClicked.name} attack_speed") * 0.6}").color(
                                        TextColor.color(0,150,230)))
                                    item!!.lore(itemLore)
                                }

                                slot(2,1) {
                                    item = axeItem

                                    onClick { clickEvent2 ->

                                        val player = clickEvent2.whoClicked as Player

                                        if(clickEvent2.isLeftClick) {


                                            var emeraldAmount = 0
                                            for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                if (emerald != null) {
                                                    if(emerald.type == Material.EMERALD) {
                                                        emeraldAmount += emerald.amount
                                                    }
                                                }
                                            }

                                            if(emeraldAmount < 12) {
                                                if(config.getInt("${clickEvent2.whoClicked.name} attack_damage") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                }
                                                else {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("에메랄드가 부족합니다! 필요량 : 12개").color(TextColor.color(150,0,0)))
                                                }
                                            }

                                            else {
                                                if (config.getInt("${clickEvent2.whoClicked.name} attack_damage").equals(null)) {
                                                    config.set("${clickEvent2.whoClicked.name} attack_damage", 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} attack_damage")) {
                                                        1 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                                        }
                                                        2 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue?.plus(
                                                        1
                                                    )!!
                                                    item!!.lore(axeLore)

                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                                else if(config.getInt("${clickEvent2.whoClicked.name} attack_damage") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                    player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                                }
                                                else {
                                                    config.set("${clickEvent2.whoClicked.name} attack_damage", config.getInt("${clickEvent2.whoClicked.name} attack_damage") + 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} attack_damage")) {
                                                        1 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        2 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { axeLore.clear()
                                                            axeLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    item!!.lore(axeLore)
                                                    player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue?.plus(
                                                        1
                                                    )!!

                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }

                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                            }



                                        }
                                    }
                                }

                                slot(2,2) {
                                    item = ItemStack(Material.EMERALD)
                                    val itemLore = ArrayList<Component>()
                                    itemLore.add(Component.text("현재 공격력: ${1 + config.getInt("${clickEvent.whoClicked.name} attack_damage")}").color(
                                        TextColor.color(0,150,230)))
                                    item!!.lore(itemLore)
                                    }


                                slot(4,1) {
                                    item = totemItem

                                    onClick { clickEvent2 ->

                                        val player = clickEvent2.whoClicked as Player

                                        var emeraldAmount = 0
                                        for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                            if (emerald != null) {
                                                if(emerald.type == Material.EMERALD) {
                                                    emeraldAmount += emerald.amount
                                                }
                                            }
                                        }

                                        if(emeraldAmount < 12) {
                                            if(config.getInt("${clickEvent2.whoClicked.name} max_health") == 10) {
                                                clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                            }
                                            else {
                                                clickEvent2.whoClicked.sendMessage(Component.text("에메랄드가 부족합니다! 필요량 : 12개").color(TextColor.color(150,0,0)))
                                            }
                                        }
                                        else {
                                            if(clickEvent2.isLeftClick) {
                                                if (config.getInt("${clickEvent2.whoClicked.name} max_health").equals(null)) {
                                                    config.set("${clickEvent2.whoClicked.name} max_health", 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} max_health")) {
                                                        1 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                                        }
                                                        2 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.plus(
                                                        4
                                                    )!!
                                                    item!!.lore(totemLore)

                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }

                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                                else if(config.getInt("${clickEvent2.whoClicked.name} max_health") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                    player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                                }
                                                else {
                                                    config.set("${clickEvent2.whoClicked.name} max_health", config.getInt("${clickEvent2.whoClicked.name} max_health") + 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} max_health")) {
                                                        1 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        2 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { totemLore.clear()
                                                            totemLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    item!!.lore(totemLore)
                                                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue?.plus(
                                                        4
                                                    )!!

                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }

                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                            }
                                        }
                                    }
                                }

                                slot(4,2) {
                                    item = ItemStack(Material.EMERALD)
                                    val itemLore = ArrayList<Component>()
                                    itemLore.add(Component.text("현재 최대 체력: ${20 + config.getInt("${clickEvent.whoClicked.name} max_health") * 4}").color(
                                        TextColor.color(0,150,230)))
                                    item!!.lore(itemLore)
                                }

                                slot(6,1) {
                                    item = chestItem

                                    onClick { clickEvent2 ->

                                        val player = clickEvent2.whoClicked as Player

                                        var emeraldAmount = 0
                                        for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                            if (emerald != null) {
                                                if(emerald.type == Material.EMERALD) {
                                                    emeraldAmount += emerald.amount
                                                }
                                            }
                                        }

                                        if(emeraldAmount < 12) {
                                            if(config.getInt("${clickEvent2.whoClicked.name} defend") == 10) {
                                                clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                            }
                                            else {
                                                clickEvent2.whoClicked.sendMessage(Component.text("에메랄드가 부족합니다! 필요량 : 12개").color(TextColor.color(150,0,0)))
                                            }
                                        }
                                        else {
                                            if(clickEvent2.isLeftClick) {
                                                if (config.getInt("${clickEvent2.whoClicked.name} defend").equals(null)) {
                                                    config.set("${clickEvent2.whoClicked.name} defend", 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} defend")) {
                                                        1 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                                        }
                                                        2 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    item!!.lore(chestLore)
                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                                else if(config.getInt("${clickEvent2.whoClicked.name} defend") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                    player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                                }
                                                else {
                                                    config.set("${clickEvent2.whoClicked.name} defend", config.getInt("${clickEvent2.whoClicked.name} defend") + 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} defend")) {
                                                        1 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        2 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { chestLore.clear()
                                                            chestLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    item!!.lore(chestLore)
                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                            }
                                        }
                                    }
                                }

                                slot(6,2) {
                                    item = ItemStack(Material.EMERALD)
                                    val itemLore = ArrayList<Component>()
                                    itemLore.add(Component.text("현재 방어력: ${config.getInt("${clickEvent.whoClicked.name} defend")}").color(
                                        TextColor.color(0,150,230)))
                                    itemLore.add(Component.text("방어력은 플레이어가 최종 받는 데미지를 감소시킵니다.").color(
                                        TextColor.color(169,169,169)))
                                    item!!.lore(itemLore)
                                }

                                slot(8,1) {
                                    item = bootsItem

                                    onClick { clickEvent2 ->

                                        val player = clickEvent2.whoClicked as Player

                                        var emeraldAmount = 0
                                        for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                            if (emerald != null) {
                                                if(emerald.type == Material.EMERALD) {
                                                    emeraldAmount += emerald.amount
                                                }
                                            }
                                        }

                                        if(emeraldAmount < 12) {
                                            if(config.getInt("${clickEvent2.whoClicked.name} movement_speed") == 10) {
                                                clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                            }
                                            else {
                                                clickEvent2.whoClicked.sendMessage(Component.text("에메랄드가 부족합니다! 필요량 : 12개").color(TextColor.color(150,0,0)))
                                            }
                                        }
                                        else {
                                            if(clickEvent2.isLeftClick) {
                                                if (config.getInt("${clickEvent2.whoClicked.name} movement_speed").equals(null)) {
                                                    config.set("${clickEvent2.whoClicked.name} movement_speed", 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} movement_speed")) {
                                                        1 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))
                                                        }
                                                        2 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue?.plus(
                                                        0.015
                                                    )!!
                                                    item!!.lore(bootsLore)
                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                                else if(config.getInt("${clickEvent2.whoClicked.name} movement_speed") == 10) {
                                                    clickEvent2.whoClicked.sendMessage(Component.text("더 이상 업그레이드 할 수 없습니다!").color(TextColor.color(150,0,0)))
                                                    player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.1f)
                                                }
                                                else {
                                                    config.set("${clickEvent2.whoClicked.name} movement_speed", config.getInt("${clickEvent2.whoClicked.name} movement_speed") + 1)
                                                    saveConfig()
                                                    when (config.getInt("${clickEvent.whoClicked.name} attack_damage")) {
                                                        1 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★☆☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        2 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★☆☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        3 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★☆☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        4 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★☆☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        5 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★☆☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        6 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★☆☆☆☆").color(TextColor.color(0,255,179)))}
                                                        7 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★☆☆☆").color(TextColor.color(0,255,179)))}
                                                        8 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★★☆☆").color(TextColor.color(0,255,179)))}
                                                        9 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★★★☆").color(TextColor.color(0,255,179)))}
                                                        10 -> { bootsLore.clear()
                                                            bootsLore.add(Component.text("★★★★★★★★★★").color(TextColor.color(0,255,179)))}
                                                    }
                                                    item!!.lore(bootsLore)
                                                    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue?.plus(
                                                        0.015
                                                    )!!

                                                    var lastAmount = 12
                                                    for (emerald in clickEvent2.whoClicked.inventory.contents) {
                                                        if (emerald != null) {
                                                            if(emerald.type == Material.EMERALD) {
                                                                if(emerald.amount < 12) {
                                                                    lastAmount -= emerald.amount
                                                                    emerald.amount = 0
                                                                }
                                                                else {
                                                                    emerald.amount -= 12
                                                                    break
                                                                }

                                                                if(lastAmount == 0) {
                                                                    break
                                                                }
                                                            }
                                                        }
                                                    }
                                                    player.playSound(event.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f)
                                                }
                                            }
                                        }
                                    }
                                }

                                slot(8,2) {
                                    item = ItemStack(Material.EMERALD)
                                    val itemLore = ArrayList<Component>()
                                    itemLore.add(Component.text("현재 이동속도: ${0.1 + config.getInt("${clickEvent.whoClicked.name} movement_speed") * 0.015}").color(
                                        TextColor.color(0,150,230)))
                                    item!!.lore(itemLore)
                                }
                            }

                            event.player.openFrame(shopFrame)


                        }
                    }
                }
            }
            event.player.openFrame(menuFrame)
        }
    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageEvent) {
        if(event.entity is Player) {
            if(event.cause != EntityDamageEvent.DamageCause.FALL && event.cause != EntityDamageEvent.DamageCause.FIRE_TICK && event.cause != EntityDamageEvent.DamageCause.DROWNING &&
                event.cause != EntityDamageEvent.DamageCause.FREEZE && event.cause != EntityDamageEvent.DamageCause.HOT_FLOOR && event.cause != EntityDamageEvent.DamageCause.POISON &&
                event.cause != EntityDamageEvent.DamageCause.MAGIC && event.cause != EntityDamageEvent.DamageCause.WORLD_BORDER) {
                if(!config.getInt("${event.entity.name} defend").equals(null)) {
                    event.damage -= config.getInt("${event.entity.name} defend") / 4 + 1
                }
            }
        }
    }

    override fun onDisable() {

    }
}