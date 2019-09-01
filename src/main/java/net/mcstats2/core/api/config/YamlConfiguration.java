package net.mcstats2.core.api.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YamlConfiguration extends ConfigurationProvider {
    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
        protected Yaml initialValue() {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(FlowStyle.BLOCK);
            return new Yaml(options);
        }
    };

    public void save(Configuration config, File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        Throwable var4 = null;

        try {
            this.save(config, (Writer)writer);
        } catch (Throwable var13) {
            var4 = var13;
            throw var13;
        } finally {
            if (writer != null) {
                if (var4 != null) {
                    try {
                        writer.close();
                    } catch (Throwable var12) {
                        var4.addSuppressed(var12);
                    }
                } else {
                    writer.close();
                }
            }

        }

    }

    public void save(Configuration config, Writer writer) {
        ((Yaml)this.yaml.get()).dump(config.self, writer);
    }

    public Configuration load(File file) throws IOException {
        return this.load((File)file, (Configuration)null);
    }

    public Configuration load(File file, Configuration defaults) throws IOException {
        FileReader reader = new FileReader(file);
        Throwable var4 = null;

        Configuration var5;
        try {
            var5 = this.load((Reader)reader, defaults);
        } catch (Throwable var14) {
            var4 = var14;
            throw var14;
        } finally {
            if (reader != null) {
                if (var4 != null) {
                    try {
                        reader.close();
                    } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                    }
                } else {
                    reader.close();
                }
            }

        }

        return var5;
    }

    public Configuration load(Reader reader) {
        return this.load((Reader)reader, (Configuration)null);
    }

    public Configuration load(Reader reader, Configuration defaults) {
        Map<String, Object> map = (Map)((Yaml)this.yaml.get()).loadAs(reader, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap();
        }

        return new Configuration((Map)map, defaults);
    }

    public Configuration load(InputStream is) {
        return this.load((InputStream)is, (Configuration)null);
    }

    public Configuration load(InputStream is, Configuration defaults) {
        Map<String, Object> map = (Map)((Yaml)this.yaml.get()).loadAs(is, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap();
        }

        return new Configuration((Map)map, defaults);
    }

    public Configuration load(String string) {
        return this.load((String)string, (Configuration)null);
    }

    public Configuration load(String string, Configuration defaults) {
        Map<String, Object> map = (Map)((Yaml)this.yaml.get()).loadAs(string, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap();
        }

        return new Configuration((Map)map, defaults);
    }

    YamlConfiguration() {
    }
}
