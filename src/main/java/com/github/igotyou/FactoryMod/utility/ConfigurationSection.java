package com.github.igotyou.FactoryMod.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationSection {
    private final Map<String, Object> data;
    private final String name;
    private final String path;

    public ConfigurationSection(Map<String, Object> data, String name, String path) {
        this.data = data != null ? data : new LinkedHashMap<>();
        this.name = name;
        this.path = path;
    }

    public Set<String> getKeys(boolean deep) {
        return data.keySet();
    }

    public ConfigurationSection getConfigurationSection(String path) {
        Object val = get(path);
        if (val instanceof Map) {
            return new ConfigurationSection((Map<String, Object>) val, path, (this.path.isEmpty() ? "" : this.path + ".") + path);
        }
        return null;
    }

    public Object get(String path) {
        if (!path.contains(".")) {
            return data.get(path);
        }
        String[] parts = path.split("\\.", 2);
        Object next = data.get(parts[0]);
        if (next instanceof Map) {
            return new ConfigurationSection((Map<String, Object>) next, parts[0], (this.path.isEmpty() ? "" : this.path + ".") + parts[0]).get(parts[1]);
        }
        return null;
    }

    public String getString(String path) {
        Object val = get(path);
        return val != null ? val.toString() : null;
    }

    public String getString(String path, String def) {
        String val = getString(path);
        return val != null ? val : def;
    }

    public int getInt(String path) {
        return getInt(path, 0);
    }

    public int getInt(String path, int def) {
        Object val = get(path);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean def) {
        Object val = get(path);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return def;
    }

    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    public double getDouble(String path, double def) {
        Object val = get(path);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public List<String> getStringList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            List<String> list = new ArrayList<>();
            for (Object o : (List<?>) val) {
                if (o != null) list.add(o.toString());
            }
            return list;
        }
        return new ArrayList<>();
    }

    public boolean contains(String path) {
        return get(path) != null;
    }

    public boolean isString(String path) {
        return get(path) instanceof String;
    }

    public boolean isList(String path) {
        return get(path) instanceof List;
    }

    public boolean isConfigurationSection(String path) {
        return get(path) instanceof Map;
    }

    public boolean isSet(String path) {
        return contains(path);
    }

    public String getCurrentPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public ConfigurationSection createSection(String path) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        data.put(path, newMap);
        return new ConfigurationSection(newMap, path, (this.path.isEmpty() ? "" : this.path + ".") + path);
    }

    public void set(String path, Object value) {
        data.put(path, value);
    }
}
