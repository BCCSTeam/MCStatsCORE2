package net.mcstats2.core.api;

import com.google.common.base.Preconditions;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public abstract class Command {
    private final String name;
    private final String permission;
    private final String[] aliases;

    public Command(String name) {
        this(name, (String)null);
    }

    public Command(String name, String permission, String... aliases) {
        Preconditions.checkArgument(name != null, "name");
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
    }

    public abstract void execute(MCSEntity e, String[] args) throws InterruptedException, ExecutionException, IOException;

    public String getName() {
        return this.name;
    }

    public String getPermission() {
        return this.permission;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public MCSCore getCore() {
        return MCSCore.getInstance();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Command)) {
            return false;
        } else {
            Command other = (Command)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label39: {
                    Object this$name = this.getName();
                    Object other$name = other.getName();
                    if (this$name == null) {
                        if (other$name == null) {
                            break label39;
                        }
                    } else if (this$name.equals(other$name)) {
                        break label39;
                    }

                    return false;
                }

                Object this$permission = this.getPermission();
                Object other$permission = other.getPermission();
                if (this$permission == null) {
                    if (other$permission != null) {
                        return false;
                    }
                } else if (!this$permission.equals(other$permission)) {
                    return false;
                }

                if (!Arrays.deepEquals(this.getAliases(), other.getAliases())) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof Command;
    }

    public int hashCode() {
        int result = 1;
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 0 : $name.hashCode());
        Object $permission = this.getPermission();
        result = result * 59 + ($permission == null ? 0 : $permission.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getAliases());
        return result;
    }

    public String toString() {
        return "Command(name=" + this.getName() + ", permission=" + this.getPermission() + ", aliases=" + Arrays.deepToString(this.getAliases()) + ")";
    }
}
