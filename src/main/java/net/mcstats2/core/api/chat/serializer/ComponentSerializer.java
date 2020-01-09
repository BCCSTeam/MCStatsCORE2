package net.mcstats2.core.api.chat.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;
import java.util.Set;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.TextComponent;

public class ComponentSerializer implements JsonDeserializer<BaseComponent> {
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Gson gson = (new GsonBuilder()).registerTypeAdapter(BaseComponent.class, new net.md_5.bungee.chat.ComponentSerializer()).registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).create();
    public static final ThreadLocal<Set<BaseComponent>> serializedComponents = new ThreadLocal();

    public ComponentSerializer() {
    }

    public static BaseComponent[] parse(String json) {
        JsonElement jsonElement = JSON_PARSER.parse(json);
        return jsonElement.isJsonArray() ? gson.fromJson(jsonElement, BaseComponent[].class) : new BaseComponent[]{gson.fromJson(jsonElement, BaseComponent.class)};
    }

    public static String toString(BaseComponent component) {
        return gson.toJson(component);
    }

    public static String toString(BaseComponent... components) {
        return components.length == 1 ? gson.toJson(components[0]) : gson.toJson(new TextComponent(components));
    }

    public BaseComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return new TextComponent(json.getAsString());
        } else
            return (BaseComponent)context.deserialize(json, TextComponent.class);
    }
}
