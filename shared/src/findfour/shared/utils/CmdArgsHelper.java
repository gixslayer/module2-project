package findfour.shared.utils;

import java.util.HashMap;
import java.util.Map;

import findfour.shared.ArgumentException;

public class CmdArgsHelper {
    private final Map<String, Flag> registeredFlags;
    private final Map<String, Argument> registeredArgs;
    private final Map<String, String> aliasMapping;

    public CmdArgsHelper() {
        this.registeredFlags = new HashMap<String, Flag>();
        this.registeredArgs = new HashMap<String, Argument>();
        this.aliasMapping = new HashMap<String, String>();
    }

    public void registerFlag(String name, String alias, String description) {
        if (aliasMapping.containsKey(alias)) {
            throw new ArgumentException("alias", "Duplicate alias registration.");
        } else if (registeredArgs.containsKey(name)) {
            throw new ArgumentException("name", "Duplicate name registration.");
        } else if (registeredFlags.containsKey(name)) {
            throw new ArgumentException("name", "Duplicate name registration.");
        }

        Flag flag = new Flag(name, alias, description);

        registeredFlags.put(name, flag);
        aliasMapping.put(alias, name);
    }

    public void registerArg(String name, String alias, String description, String value,
            boolean required) {

    }

    public boolean parse(String[] args) {
        return false;
    }

    public boolean hasFlag(String flag) {
        return false;
    }

    public boolean hasArg(String name) {
        return false;
    }

    public String getArg(String name) {
        return null;
    }

    public int getArgAsInt(String name) {
        return 0;
    }

    private class Flag {
        private final String name;
        private final String alias;
        private final String description;

        Flag(String argName, String argAlias, String argDescription) {
            this.name = argName;
            this.alias = argAlias;
            this.description = argDescription;
        }
    }

    private class Argument {
        private final String name;
        private final String alias;
        private final String description;
        private final String defaultValue;
        private final boolean required;

        Argument(String argName, String argAlias, String desc, String value, boolean argRequired) {
            this.name = argName;
            this.alias = argAlias;
            this.description = desc;
            this.defaultValue = value;
            this.required = argRequired;
        }
    }

}
