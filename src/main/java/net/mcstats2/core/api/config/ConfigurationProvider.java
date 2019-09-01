package net.mcstats2.core.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationProvider {
    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> providers = new HashMap();

    public ConfigurationProvider() {
    }

    public static ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> provider) {
        return providers.get(provider);
    }

    public abstract void save(Configuration var1, File var2) throws IOException;

    public abstract void save(Configuration var1, Writer var2);

    public abstract Configuration load(File var1) throws IOException;

    public abstract Configuration load(File var1, Configuration var2) throws IOException;

    public abstract Configuration load(Reader var1);

    public abstract Configuration load(Reader var1, Configuration var2);

    public abstract Configuration load(InputStream var1);

    public abstract Configuration load(InputStream var1, Configuration var2);

    public abstract Configuration load(String var1);

    public abstract Configuration load(String var1, Configuration var2);

    static {
        providers.put(YamlConfiguration.class, new YamlConfiguration());
    }
}
