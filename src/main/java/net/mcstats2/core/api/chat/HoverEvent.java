package net.mcstats2.core.api.chat;

import java.util.Arrays;

public final class HoverEvent {
    private final HoverEvent.Action action;
    private final BaseComponent[] value;

    public HoverEvent.Action getAction() {
        return this.action;
    }

    public BaseComponent[] getValue() {
        return this.value;
    }

    public String toString() {
        return "HoverEvent(action=" + this.getAction() + ", value=" + Arrays.deepToString(this.getValue()) + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof HoverEvent)) {
            return false;
        } else {
            HoverEvent other = (HoverEvent)o;
            Object this$action = this.getAction();
            Object other$action = other.getAction();
            if (this$action == null) {
                if (other$action == null) {
                    return Arrays.deepEquals(this.getValue(), other.getValue());
                }
            } else if (this$action.equals(other$action)) {
                return Arrays.deepEquals(this.getValue(), other.getValue());
            }

            return false;
        }
    }

    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        Object $action = this.getAction();
        result = result * 59 + ($action == null ? 43 : $action.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getValue());
        return result;
    }

    public HoverEvent(HoverEvent.Action action, BaseComponent[] value) {
        this.action = action;
        this.value = value;
    }

    public static enum Action {
        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY;

        private Action() {
        }
    }
}
