package org.lushplugins.lushcontainershops;

import org.bukkit.plugin.PluginManager;
import org.lushplugins.lushcontainershops.command.ContainerShopsCommand;
import org.lushplugins.lushcontainershops.config.ConfigManager;
import org.lushplugins.lushcontainershops.hook.PacketEventsHook;
import org.lushplugins.lushcontainershops.listener.ContainerListener;
import org.lushplugins.lushcontainershops.listener.SignListener;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.EquipmentContextParameter;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.RayTraceContextParameter;
import org.lushplugins.lushcontainershops.utils.lamp.response.MessageResponseHandler;
import org.lushplugins.lushlib.libraries.jackson.databind.ObjectMapper;
import org.lushplugins.lushlib.plugin.SpigotPlugin;
import org.lushplugins.lushlib.serializer.JacksonHelper;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.Optional;

public class LushContainerShops extends SpigotPlugin {
    private static final ObjectMapper JACKSON_MAPPER = JacksonHelper.addCustomSerializers(new ObjectMapper());
    private static LushContainerShops plugin;

    private ConfigManager configManager;
    private PacketEventsHook packetEventsHook = null;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager();
        this.configManager.reloadConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("packetevents") != null) {
            this.packetEventsHook = new PacketEventsHook();
        }

        //noinspection Convert2MethodRef
//        addHook("packetevents", () -> new PacketEventsHook());

        registerListener(new ContainerListener());
        registerListener(new SignListener());

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this)
            .parameterTypes(parameters -> {
                parameters.addContextParameterFactory(new EquipmentContextParameter());
                parameters.addContextParameterFactory(new RayTraceContextParameter());
            })
            .responseHandler(String.class, new MessageResponseHandler())
            .build();
        lamp.register(new ContainerShopsCommand());
    }

    @Override
    public void onDisable() {
        // Shutdown impl
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Optional<PacketEventsHook> getPacketEventsHook() {
        return Optional.ofNullable(packetEventsHook);
    }

    public static ObjectMapper getJacksonMapper() {
        return JACKSON_MAPPER;
    }

    public static LushContainerShops getInstance() {
        return plugin;
    }
}
