package net.mcstats2.core.api.chat;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.mcstats2.core.api.ChatColor;

public final class ComponentBuilder {
    private BaseComponent current;
    private final List<BaseComponent> parts = new ArrayList();

    public ComponentBuilder(ComponentBuilder original) {
        this.current = original.current.duplicate();
        Iterator var2 = original.parts.iterator();

        while(var2.hasNext()) {
            BaseComponent baseComponent = (BaseComponent)var2.next();
            this.parts.add(baseComponent.duplicate());
        }

    }

    public ComponentBuilder(String text) {
        this.current = new TextComponent(text);
    }

    public ComponentBuilder(BaseComponent component) {
        this.current = component.duplicate();
    }

    public ComponentBuilder append(BaseComponent component) {
        return this.append(component, ComponentBuilder.FormatRetention.ALL);
    }

    public ComponentBuilder append(BaseComponent component, ComponentBuilder.FormatRetention retention) {
        this.parts.add(this.current);
        BaseComponent previous = this.current;
        this.current = component.duplicate();
        this.current.copyFormatting(previous, retention, false);
        return this;
    }

    public ComponentBuilder append(BaseComponent[] components) {
        return this.append(components, ComponentBuilder.FormatRetention.ALL);
    }

    public ComponentBuilder append(BaseComponent[] components, ComponentBuilder.FormatRetention retention) {
        Preconditions.checkArgument(components.length != 0, "No components to append");
        BaseComponent previous = this.current;
        BaseComponent[] var4 = components;
        int var5 = components.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            BaseComponent component = var4[var6];
            this.parts.add(this.current);
            this.current = component.duplicate();
            this.current.copyFormatting(previous, retention, false);
        }

        return this;
    }

    public ComponentBuilder append(String text) {
        return this.append(text, ComponentBuilder.FormatRetention.ALL);
    }

    public ComponentBuilder appendLegacy(String text) {
        return this.append(TextComponent.fromLegacyText(text));
    }

    public ComponentBuilder append(String text, ComponentBuilder.FormatRetention retention) {
        this.parts.add(this.current);
        BaseComponent old = this.current;
        this.current = new TextComponent(text);
        this.current.copyFormatting(old, retention, false);
        return this;
    }

    public ComponentBuilder append(ComponentBuilder.Joiner joiner) {
        return joiner.join(this, ComponentBuilder.FormatRetention.ALL);
    }

    public ComponentBuilder append(ComponentBuilder.Joiner joiner, ComponentBuilder.FormatRetention retention) {
        return joiner.join(this, retention);
    }

    public ComponentBuilder color(ChatColor color) {
        this.current.setColor(color);
        return this;
    }

    public ComponentBuilder bold(boolean bold) {
        this.current.setBold(bold);
        return this;
    }

    public ComponentBuilder italic(boolean italic) {
        this.current.setItalic(italic);
        return this;
    }

    public ComponentBuilder underlined(boolean underlined) {
        this.current.setUnderlined(underlined);
        return this;
    }

    public ComponentBuilder strikethrough(boolean strikethrough) {
        this.current.setStrikethrough(strikethrough);
        return this;
    }

    public ComponentBuilder obfuscated(boolean obfuscated) {
        this.current.setObfuscated(obfuscated);
        return this;
    }

    public ComponentBuilder insertion(String insertion) {
        this.current.setInsertion(insertion);
        return this;
    }

    public ComponentBuilder event(ClickEvent clickEvent) {
        this.current.setClickEvent(clickEvent);
        return this;
    }

    public ComponentBuilder event(HoverEvent hoverEvent) {
        this.current.setHoverEvent(hoverEvent);
        return this;
    }

    public ComponentBuilder reset() {
        return this.retain(ComponentBuilder.FormatRetention.NONE);
    }

    public ComponentBuilder retain(ComponentBuilder.FormatRetention retention) {
        this.current.retain(retention);
        return this;
    }

    public int size() {
        return this.parts.size();
    }

    public BaseComponent[] create() {
        BaseComponent[] result = this.parts.toArray(new BaseComponent[this.parts.size() + 1]);
        result[this.parts.size()] = this.current;
        return result;
    }

    public interface Joiner {
        ComponentBuilder join(ComponentBuilder var1, ComponentBuilder.FormatRetention var2);
    }

    public static enum FormatRetention {
        NONE,
        FORMATTING,
        EVENTS,
        ALL;

        private FormatRetention() {
        }
    }
}
