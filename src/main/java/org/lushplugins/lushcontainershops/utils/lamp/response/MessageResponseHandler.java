package org.lushplugins.lushcontainershops.utils.lamp.response;

import org.lushplugins.lushlib.libraries.chatcolor.paper.PaperColor;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.response.ResponseHandler;

public class MessageResponseHandler implements ResponseHandler<BukkitCommandActor, String> {

    @Override
    public void handleResponse(String string, ExecutionContext<BukkitCommandActor> context) {
        PaperColor.handler().sendMessage(context.actor().sender(), string);
    }
}
