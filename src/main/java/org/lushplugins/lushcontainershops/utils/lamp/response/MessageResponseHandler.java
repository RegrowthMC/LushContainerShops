package org.lushplugins.lushcontainershops.utils.lamp.response;

import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.response.ResponseHandler;

public class MessageResponseHandler implements ResponseHandler<BukkitCommandActor, String> {

    @Override
    public void handleResponse(String string, ExecutionContext<BukkitCommandActor> context) {
        ChatColorHandler.sendMessage(context.actor().sender(), string);
    }
}
