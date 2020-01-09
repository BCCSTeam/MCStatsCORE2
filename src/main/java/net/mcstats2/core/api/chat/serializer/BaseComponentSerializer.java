package net.mcstats2.core.api.chat.serializer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.ClickEvent;
import net.mcstats2.core.api.chat.HoverEvent;
import net.mcstats2.core.api.chat.ClickEvent.Action;

public class BaseComponentSerializer {
    public BaseComponentSerializer() {
    }

    protected void deserialize(JsonObject object, BaseComponent component, JsonDeserializationContext context) {
        if (object.has("color")) {
            component.setColor(ChatColor.valueOf(object.get("color").getAsString().toUpperCase(Locale.ROOT)));
        }

        if (object.has("bold")) {
            component.setBold(object.get("bold").getAsBoolean());
        }

        if (object.has("italic")) {
            component.setItalic(object.get("italic").getAsBoolean());
        }

        if (object.has("underlined")) {
            component.setUnderlined(object.get("underlined").getAsBoolean());
        }

        if (object.has("strikethrough")) {
            component.setStrikethrough(object.get("strikethrough").getAsBoolean());
        }

        if (object.has("obfuscated")) {
            component.setObfuscated(object.get("obfuscated").getAsBoolean());
        }

        if (object.has("insertion")) {
            component.setInsertion(object.get("insertion").getAsString());
        }

        if (object.has("extra")) {
            component.setExtra(Arrays.asList(context.deserialize(object.get("extra"), BaseComponent[].class)));
        }

        JsonObject event;
        if (object.has("clickEvent")) {
            event = object.getAsJsonObject("clickEvent");
            component.setClickEvent(new ClickEvent(Action.valueOf(event.get("action").getAsString().toUpperCase(Locale.ROOT)), event.get("value").getAsString()));
        }

        if (object.has("hoverEvent")) {
            event = object.getAsJsonObject("hoverEvent");
            BaseComponent[] res;
            if (event.get("value").isJsonArray()) {
                res = (BaseComponent[])context.deserialize(event.get("value"), BaseComponent[].class);
            } else {
                res = new BaseComponent[]{(BaseComponent)context.deserialize(event.get("value"), BaseComponent.class)};
            }

            component.setHoverEvent(new HoverEvent(net.mcstats2.core.api.chat.HoverEvent.Action.valueOf(event.get("action").getAsString().toUpperCase(Locale.ROOT)), res));
        }

    }

    protected void serialize(JsonObject object, BaseComponent component, JsonSerializationContext context) {
        boolean first = false;
        if (ComponentSerializer.serializedComponents.get() == null) {
            first = true;
            ComponentSerializer.serializedComponents.set(Collections.newSetFromMap(new IdentityHashMap()));
        }

        try {
            Preconditions.checkArgument(!((Set) ComponentSerializer.serializedComponents.get()).contains(component), "Component loop");
            ((Set) ComponentSerializer.serializedComponents.get()).add(component);
            if (component.getColorRaw() != null) {
                object.addProperty("color", component.getColorRaw().getName());
            }

            if (component.isBoldRaw() != null) {
                object.addProperty("bold", component.isBoldRaw());
            }

            if (component.isItalicRaw() != null) {
                object.addProperty("italic", component.isItalicRaw());
            }

            if (component.isUnderlinedRaw() != null) {
                object.addProperty("underlined", component.isUnderlinedRaw());
            }

            if (component.isStrikethroughRaw() != null) {
                object.addProperty("strikethrough", component.isStrikethroughRaw());
            }

            if (component.isObfuscatedRaw() != null) {
                object.addProperty("obfuscated", component.isObfuscatedRaw());
            }

            if (component.getInsertion() != null) {
                object.addProperty("insertion", component.getInsertion());
            }

            if (component.getExtra() != null) {
                object.add("extra", context.serialize(component.getExtra()));
            }

            JsonObject hoverEvent;
            if (component.getClickEvent() != null) {
                hoverEvent = new JsonObject();
                hoverEvent.addProperty("action", component.getClickEvent().getAction().toString().toLowerCase(Locale.ROOT));
                hoverEvent.addProperty("value", component.getClickEvent().getValue());
                object.add("clickEvent", hoverEvent);
            }

            if (component.getHoverEvent() != null) {
                hoverEvent = new JsonObject();
                hoverEvent.addProperty("action", component.getHoverEvent().getAction().toString().toLowerCase(Locale.ROOT));
                hoverEvent.add("value", context.serialize(component.getHoverEvent().getValue()));
                object.add("hoverEvent", hoverEvent);
            }
        } finally {
            ((Set) ComponentSerializer.serializedComponents.get()).remove(component);
            if (first) {
                ComponentSerializer.serializedComponents.set(null);
            }

        }

    }
}
