package net.mcstats2.core.api.chat;

public final class ClickEvent {
    private final Action action;
    private final String value;

    public Action getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return "ClickEvent(action=" + this.getAction() + ", value=" + this.getValue() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ClickEvent)) {
            return false;
        } else {
            ClickEvent other = (ClickEvent)o;
            Object this$action = this.getAction();
            Object other$action = other.getAction();
            if (this$action == null) {
                if (other$action != null) {
                    return false;
                }
            } else if (!this$action.equals(other$action)) {
                return false;
            }

            Object this$value = this.getValue();
            Object other$value = other.getValue();
            if (this$value == null) {
                if (other$value != null) {
                    return false;
                }
            } else if (!this$value.equals(other$value)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        Object $action = this.getAction();
        result = result * 59 + ($action == null ? 43 : $action.hashCode());
        Object $value = this.getValue();
        result = result * 59 + ($value == null ? 43 : $value.hashCode());
        return result;
    }

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public static enum Action {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE;

        private Action() {
        }
    }
}
