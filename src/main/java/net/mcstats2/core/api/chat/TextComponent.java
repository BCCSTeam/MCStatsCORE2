package net.mcstats2.core.api.chat;

import net.mcstats2.core.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextComponent extends BaseComponent {
    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private String text;

    public static BaseComponent[] fromLegacyText(String message) {
        return fromLegacyText(message, ChatColor.WHITE);
    }

    public static BaseComponent[] fromLegacyText(String message, ChatColor defaultColor) {
        ArrayList<BaseComponent> components = new ArrayList();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher(message);

        for(int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            TextComponent old;
            if (c == 167) {
                ++i;
                if (i >= message.length()) {
                    break;
                }

                c = message.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    c = (char)(c + 32);
                }

                ChatColor format = ChatColor.getByChar(c);
                if (format != null) {
                    if (builder.length() > 0) {
                        old = component;
                        component = new TextComponent(component);
                        old.setText(builder.toString());
                        builder = new StringBuilder();
                        components.add(old);
                    }

                    switch(format) {
                        case BOLD:
                            component.setBold(true);
                            break;
                        case ITALIC:
                            component.setItalic(true);
                            break;
                        case UNDERLINE:
                            component.setUnderlined(true);
                            break;
                        case STRIKETHROUGH:
                            component.setStrikethrough(true);
                            break;
                        case MAGIC:
                            component.setObfuscated(true);
                            break;
                        case RESET:
                            format = defaultColor;
                        default:
                            component = new TextComponent();
                            component.setColor(format);
                    }
                }
            } else {
                int pos = message.indexOf(32, i);
                if (pos == -1) {
                    pos = message.length();
                }

                if (matcher.region(i, pos).find()) {
                    if (builder.length() > 0) {
                        old = component;
                        component = new TextComponent(component);
                        old.setText(builder.toString());
                        builder = new StringBuilder();
                        components.add(old);
                    }

                    old = component;
                    component = new TextComponent(component);
                    String urlString = message.substring(i, pos);
                    component.setText(urlString);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, urlString.startsWith("http") ? urlString : "http://" + urlString));
                    components.add(component);
                    i += pos - i - 1;
                    component = old;
                } else {
                    builder.append(c);
                }
            }
        }

        component.setText(builder.toString());
        components.add(component);
        return (BaseComponent[])components.toArray(new BaseComponent[components.size()]);
    }

    public TextComponent() {
        this.text = "";
    }

    public TextComponent(TextComponent textComponent) {
        super(textComponent);
        this.setText(textComponent.getText());
    }

    public TextComponent(BaseComponent... extras) {
        this.setText("");
        this.setExtra(new ArrayList(Arrays.asList(extras)));
    }

    public BaseComponent duplicate() {
        return new TextComponent(this);
    }

    protected void toPlainText(StringBuilder builder) {
        builder.append(this.text);
        super.toPlainText(builder);
    }

    protected void toLegacyText(StringBuilder builder) {
        builder.append(this.getColor());
        if (this.isBold()) {
            builder.append(ChatColor.BOLD);
        }

        if (this.isItalic()) {
            builder.append(ChatColor.ITALIC);
        }

        if (this.isUnderlined()) {
            builder.append(ChatColor.UNDERLINE);
        }

        if (this.isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }

        if (this.isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }

        builder.append(this.text);
        super.toLegacyText(builder);
    }

    public String toString() {
        return String.format("TextComponent{text=%s, %s}", this.text, super.toString());
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextComponent(String text) {
        this.text = text;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TextComponent)) {
            return false;
        } else {
            TextComponent other = (TextComponent)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                Object this$text = this.getText();
                Object other$text = other.getText();
                if (this$text == null) {
                    if (other$text != null) {
                        return false;
                    }
                } else if (!this$text.equals(other$text)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof TextComponent;
    }

    public int hashCode() {
        int result = super.hashCode();
        Object $text = this.getText();
        result = result * 59 + ($text == null ? 43 : $text.hashCode());
        return result;
    }
}
