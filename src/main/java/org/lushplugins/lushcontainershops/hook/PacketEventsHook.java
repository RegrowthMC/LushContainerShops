package org.lushplugins.lushcontainershops.hook;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.google.common.collect.HashMultimap;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.projectile.ItemEntityMeta;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.lushlib.utils.BlockPosition;

import java.util.Collection;
import java.util.Set;

public class PacketEventsHook extends Hook implements org.bukkit.event.Listener {
    private final HashMultimap<String, WrapperEntity> chunkEntityMap = HashMultimap.create();

    public PacketEventsHook() {
        super("packetevents");

        EntityLib.init(
            new SpigotEntityLibPlatform(LushContainerShops.getInstance()),
            new APIConfig(PacketEvents.getAPI()));

        LushContainerShops.getInstance().registerListener(this);
    }

    public void loadVisualsInChunk(Chunk chunk) {
        Collection<BlockState> potentialShops = chunk.getTileEntities((block) -> {
            return LushContainerShops.getInstance().getConfigManager().isWhitelistedSign(block.getType());
        }, true);

        String chunkKey = getWorldChunkKey(chunk);
        for (BlockState blockState : potentialShops) {
            if (blockState instanceof Sign sign) {
                ShopSign shop = ShopSign.from(sign);
                if (shop == null) {
                    continue;
                }

                ShopItem product = shop.getProduct();
                if (product == null) {
                    continue;
                }

                BlockPosition shopContainerPosition = shop.getContainerPosition();
                if (shopContainerPosition == null) {
                    continue;
                }

                Block shopContainerBlock = shopContainerPosition.getBlock();
                if (!canDisplayVisualAt(shopContainerBlock.getRelative(BlockFace.UP))) {
                    continue;
                }

                Location location = new Location(
                    shopContainerPosition.x() + 0.5,
                    shopContainerPosition.y() + 1.0,
                    shopContainerPosition.z() + 0.5,
                    0,
                    0
                );

                WrapperEntity item = new WrapperEntity(EntityTypes.ITEM);
                ItemEntityMeta entityMeta = item.getEntityMeta(ItemEntityMeta.class);
                entityMeta.setHasNoGravity(true);
                entityMeta.setItem(ItemStack.builder()
                    .type(SpigotConversionUtil.fromBukkitItemMaterial(product.getMaterial()))
                    .amount(product.getAmount())
                    .build());

                item.spawn(location);
                for (Player player : chunk.getPlayersSeeingChunk()) {
                    item.addViewer(player.getUniqueId());
                }

                this.chunkEntityMap.put(chunkKey, item);
            }
        }
    }

    public void removeVisualsInChunk(Chunk chunk) {
        String chunkKey = getWorldChunkKey(chunk);
        for (WrapperEntity entity : this.chunkEntityMap.get(chunkKey)) {
            for (Player player : chunk.getPlayersSeeingChunk()) {
                entity.removeViewer(player.getUniqueId());
            }
        }

        this.chunkEntityMap.removeAll(getWorldChunkKey(chunk));
    }

    public void reloadVisualsInChunk(Chunk chunk) {
        removeVisualsInChunk(chunk);
        loadVisualsInChunk(chunk);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!LushContainerShops.getInstance().getConfigManager().shouldDisplayVisual()) {
            return;
        }

        Chunk chunk = event.getChunk();
        loadVisualsInChunk(chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!LushContainerShops.getInstance().getConfigManager().shouldDisplayVisual()) {
            return;
        }

        removeVisualsInChunk(event.getChunk());
    }

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        if (!LushContainerShops.getInstance().getConfigManager().shouldDisplayVisual()) {
            return;
        }

        Set<WrapperEntity> entities = this.chunkEntityMap.get(getWorldChunkKey(event.getChunk()));
        for (WrapperEntity entity : entities) {
            entity.addViewer(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerChunkUnload(PlayerChunkUnloadEvent event) {
        if (!LushContainerShops.getInstance().getConfigManager().shouldDisplayVisual()) {
            return;
        }

        Set<WrapperEntity> entities = this.chunkEntityMap.get(getWorldChunkKey(event.getChunk()));
        for (WrapperEntity entity : entities) {
            entity.removeViewer(event.getPlayer().getUniqueId());
        }
    }

    private static boolean canDisplayVisualAt(Block block) {
        Material material = block.getType();
        return material.isAir() || Tag.IMPERMEABLE.isTagged(material);
    }

    private static String getWorldChunkKey(Chunk chunk) {
        return chunk.getWorld().getUID() + "_" + chunk.getX() + "_" + chunk.getZ();
    }
}
