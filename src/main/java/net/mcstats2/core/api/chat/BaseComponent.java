package net.mcstats2.core.api.chat;

import net.mcstats2.core.api.ChatColor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseComponent {
    BaseComponent parent;
    private ChatColor color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private String insertion;
    private List<BaseComponent> extra;
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;

    BaseComponent(BaseComponent old) {
        this.copyFormatting(old, ComponentBuilder.FormatRetention.ALL, true);
        if (old.getExtra() != null) {
            Iterator var2 = old.getExtra().iterator();

            while(var2.hasNext()) {
                BaseComponent extra = (BaseComponent)var2.next();
                this.addExtra(extra.duplicate());
            }
        }

    }

    public void copyFormatting(BaseComponent component) {
        this.copyFormatting(component, ComponentBuilder.FormatRetention.ALL, true);
    }

    public void copyFormatting(BaseComponent component, boolean replace) {
        this.copyFormatting(component, ComponentBuilder.FormatRetention.ALL, replace);
    }

    public void copyFormatting(BaseComponent component, ComponentBuilder.FormatRetention retention, boolean replace) {
        if (retention == ComponentBuilder.FormatRetention.EVENTS || retention == ComponentBuilder.FormatRetention.ALL) {
            if (replace || this.clickEvent == null) {
                this.setClickEvent(component.getClickEvent());
            }

            if (replace || this.hoverEvent == null) {
                this.setHoverEvent(component.getHoverEvent());
            }
        }

        if (retention == ComponentBuilder.FormatRetention.FORMATTING || retention == ComponentBuilder.FormatRetention.ALL) {
            if (replace || this.color == null) {
                this.setColor(component.getColorRaw());
            }

            if (replace || this.bold == null) {
                this.setBold(component.isBoldRaw());
            }

            if (replace || this.italic == null) {
                this.setItalic(component.isItalicRaw());
            }

            if (replace || this.underlined == null) {
                this.setUnderlined(component.isUnderlinedRaw());
            }

            if (replace || this.strikethrough == null) {
                this.setStrikethrough(component.isStrikethroughRaw());
            }

            if (replace || this.obfuscated == null) {
                this.setObfuscated(component.isObfuscatedRaw());
            }

            if (replace || this.insertion == null) {
                this.setInsertion(component.getInsertion());
            }
        }

    }

    public void retain(ComponentBuilder.FormatRetention retention) {
        if (retention == ComponentBuilder.FormatRetention.FORMATTING || retention == ComponentBuilder.FormatRetention.NONE) {
            this.setClickEvent((ClickEvent)null);
            this.setHoverEvent((HoverEvent)null);
        }

        if (retention == ComponentBuilder.FormatRetention.EVENTS || retention == ComponentBuilder.FormatRetention.NONE) {
            this.setColor((ChatColor)null);
            this.setBold((Boolean)null);
            this.setItalic((Boolean)null);
            this.setUnderlined((Boolean)null);
            this.setStrikethrough((Boolean)null);
            this.setObfuscated((Boolean)null);
            this.setInsertion((String)null);
        }

    }

    public abstract BaseComponent duplicate();

    /** @deprecated */
    @Deprecated
    public BaseComponent duplicateWithoutFormatting() {
        BaseComponent component = this.duplicate();
        component.retain(ComponentBuilder.FormatRetention.NONE);
        return component;
    }

    public static String toLegacyText(BaseComponent... components) {
        StringBuilder builder = new StringBuilder();
        BaseComponent[] var2 = components;
        int var3 = components.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            BaseComponent msg = var2[var4];
            builder.append(msg.toLegacyText());
        }

        return builder.toString();
    }

    public static String toPlainText(BaseComponent... components) {
        StringBuilder builder = new StringBuilder();
        BaseComponent[] var2 = components;
        int var3 = components.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            BaseComponent msg = var2[var4];
            builder.append(msg.toPlainText());
        }

        return builder.toString();
    }

    public ChatColor getColor() {
        if (this.color == null) {
            return this.parent == null ? ChatColor.WHITE : this.parent.getColor();
        } else {
            return this.color;
        }
    }

    public ChatColor getColorRaw() {
        return this.color;
    }

    public boolean isBold() {
        if (this.bold != null) {
            return this.bold;
        } else {
            return this.parent != null && this.parent.isBold();
        }
    }

    public Boolean isBoldRaw() {
        return this.bold;
    }

    public boolean isItalic() {
        if (this.italic != null) {
            return this.italic;
        } else {
            return this.parent != null && this.parent.isItalic();
        }
    }

    public Boolean isItalicRaw() {
        return this.italic;
    }

    public boolean isUnderlined() {
        if (this.underlined != null) {
            return this.underlined;
        } else {
            return this.parent != null && this.parent.isUnderlined();
        }
    }

    public Boolean isUnderlinedRaw() {
        return this.underlined;
    }

    public boolean isStrikethrough() {
        if (this.strikethrough != null) {
            return this.strikethrough;
        } else {
            return this.parent != null && this.parent.isStrikethrough();
        }
    }

    public Boolean isStrikethroughRaw() {
        return this.strikethrough;
    }

    public boolean isObfuscated() {
        if (this.obfuscated != null) {
            return this.obfuscated;
        } else {
            return this.parent != null && this.parent.isObfuscated();
        }
    }

    public Boolean isObfuscatedRaw() {
        return this.obfuscated;
    }

    public void setExtra(List<BaseComponent> components) {
        BaseComponent component;
        for(Iterator var2 = components.iterator(); var2.hasNext(); component.parent = this) {
            component = (BaseComponent)var2.next();
        }

        this.extra = components;
    }

    public void addExtra(String text) {
        this.addExtra((BaseComponent)(new TextComponent(text)));
    }

    public void addExtra(BaseComponent component) {
        if (this.extra == null) {
            this.extra = new ArrayList();
        }

        component.parent = this;
        this.extra.add(component);
    }

    public boolean hasFormatting() {
        return this.color != null || this.bold != null || this.italic != null || this.underlined != null || this.strikethrough != null || this.obfuscated != null || this.insertion != null || this.hoverEvent != null || this.clickEvent != null;
    }

    public String toPlainText() {
        StringBuilder builder = new StringBuilder();
        this.toPlainText(builder);
        return builder.toString();
    }

    void toPlainText(StringBuilder builder) {
        if (this.extra != null) {
            Iterator var2 = this.extra.iterator();

            while(var2.hasNext()) {
                BaseComponent e = (BaseComponent)var2.next();
                e.toPlainText(builder);
            }
        }

    }

    public String toLegacyText() {
        StringBuilder builder = new StringBuilder();
        this.toLegacyText(builder);
        return builder.toString();
    }

    void toLegacyText(StringBuilder builder) {
        if (this.extra != null) {
            Iterator var2 = this.extra.iterator();

            while(var2.hasNext()) {
                BaseComponent e = (BaseComponent)var2.next();
                e.toLegacyText(builder);
            }
        }

    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public void setUnderlined(Boolean underlined) {
        this.underlined = underlined;
    }

    public void setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    public void setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public void setInsertion(String insertion) {
        this.insertion = insertion;
    }

    public void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public void setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }

    public String toString() {
        return "BaseComponent(color=" + this.getColor() + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", strikethrough=" + this.strikethrough + ", obfuscated=" + this.obfuscated + ", insertion=" + this.getInsertion() + ", extra=" + this.getExtra() + ", clickEvent=" + this.getClickEvent() + ", hoverEvent=" + this.getHoverEvent() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof BaseComponent)) {
            return false;
        } else {
            BaseComponent other = (BaseComponent)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label143: {
                    Object this$parent = this.parent;
                    Object other$parent = other.parent;
                    if (this$parent == null) {
                        if (other$parent == null) {
                            break label143;
                        }
                    } else if (this$parent.equals(other$parent)) {
                        break label143;
                    }

                    return false;
                }

                Object this$color = this.getColor();
                Object other$color = other.getColor();
                if (this$color == null) {
                    if (other$color != null) {
                        return false;
                    }
                } else if (!this$color.equals(other$color)) {
                    return false;
                }

                Object this$bold = this.bold;
                Object other$bold = other.bold;
                if (this$bold == null) {
                    if (other$bold != null) {
                        return false;
                    }
                } else if (!this$bold.equals(other$bold)) {
                    return false;
                }

                label122: {
                    Object this$italic = this.italic;
                    Object other$italic = other.italic;
                    if (this$italic == null) {
                        if (other$italic == null) {
                            break label122;
                        }
                    } else if (this$italic.equals(other$italic)) {
                        break label122;
                    }

                    return false;
                }

                label115: {
                    Object this$underlined = this.underlined;
                    Object other$underlined = other.underlined;
                    if (this$underlined == null) {
                        if (other$underlined == null) {
                            break label115;
                        }
                    } else if (this$underlined.equals(other$underlined)) {
                        break label115;
                    }

                    return false;
                }

                Object this$strikethrough = this.strikethrough;
                Object other$strikethrough = other.strikethrough;
                if (this$strikethrough == null) {
                    if (other$strikethrough != null) {
                        return false;
                    }
                } else if (!this$strikethrough.equals(other$strikethrough)) {
                    return false;
                }

                Object this$obfuscated = this.obfuscated;
                Object other$obfuscated = other.obfuscated;
                if (this$obfuscated == null) {
                    if (other$obfuscated != null) {
                        return false;
                    }
                } else if (!this$obfuscated.equals(other$obfuscated)) {
                    return false;
                }

                label94: {
                    Object this$insertion = this.getInsertion();
                    Object other$insertion = other.getInsertion();
                    if (this$insertion == null) {
                        if (other$insertion == null) {
                            break label94;
                        }
                    } else if (this$insertion.equals(other$insertion)) {
                        break label94;
                    }

                    return false;
                }

                label87: {
                    Object this$extra = this.getExtra();
                    Object other$extra = other.getExtra();
                    if (this$extra == null) {
                        if (other$extra == null) {
                            break label87;
                        }
                    } else if (this$extra.equals(other$extra)) {
                        break label87;
                    }

                    return false;
                }

                Object this$clickEvent = this.getClickEvent();
                Object other$clickEvent = other.getClickEvent();
                if (this$clickEvent == null) {
                    if (other$clickEvent != null) {
                        return false;
                    }
                } else if (!this$clickEvent.equals(other$clickEvent)) {
                    return false;
                }

                Object this$hoverEvent = this.getHoverEvent();
                Object other$hoverEvent = other.getHoverEvent();
                if (this$hoverEvent == null) {
                    if (other$hoverEvent != null) {
                        return false;
                    }
                } else if (!this$hoverEvent.equals(other$hoverEvent)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof BaseComponent;
    }

    public int hashCode() {
        int result = 1;
        Object $parent = this.parent;
        result = result * 59 + ($parent == null ? 43 : $parent.hashCode());
        Object $color = this.getColor();
        result = result * 59 + ($color == null ? 43 : $color.hashCode());
        Object $bold = this.bold;
        result = result * 59 + ($bold == null ? 43 : $bold.hashCode());
        Object $italic = this.italic;
        result = result * 59 + ($italic == null ? 43 : $italic.hashCode());
        Object $underlined = this.underlined;
        result = result * 59 + ($underlined == null ? 43 : $underlined.hashCode());
        Object $strikethrough = this.strikethrough;
        result = result * 59 + ($strikethrough == null ? 43 : $strikethrough.hashCode());
        Object $obfuscated = this.obfuscated;
        result = result * 59 + ($obfuscated == null ? 43 : $obfuscated.hashCode());
        Object $insertion = this.getInsertion();
        result = result * 59 + ($insertion == null ? 43 : $insertion.hashCode());
        Object $extra = this.getExtra();
        result = result * 59 + ($extra == null ? 43 : $extra.hashCode());
        Object $clickEvent = this.getClickEvent();
        result = result * 59 + ($clickEvent == null ? 43 : $clickEvent.hashCode());
        Object $hoverEvent = this.getHoverEvent();
        result = result * 59 + ($hoverEvent == null ? 43 : $hoverEvent.hashCode());
        return result;
    }

    public BaseComponent() {
    }

    public String getInsertion() {
        return this.insertion;
    }

    public List<BaseComponent> getExtra() {
        return this.extra;
    }

    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }
}
