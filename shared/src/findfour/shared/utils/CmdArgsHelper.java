package findfour.shared.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import findfour.shared.ArgumentException;

public class CmdArgsHelper {
    private final List<String> flagList;
    private final Map<String, String> argMap;

    public CmdArgsHelper(String[] args) {
        this.flagList = new ArrayList<String>();
        this.argMap = new HashMap<String, String>();

        parse(args);
    }

    public boolean hasFlag(String flag) {
        return flagList.contains(flag);
    }

    public boolean hasArg(String name) {
        return argMap.containsKey(name);
    }

    public String getArg(String name) {
        if (!hasArg(name)) {
            throw new ArgumentException("name", "Could not find the argument");
        }

        return argMap.get(name);
    }

    public String getArg(String name, String defaultValue) {
        return hasArg(name) ? argMap.get(name) : defaultValue;
    }

    public int getArgAsInt(String name) {
        String value = getArg(name);

        // Any formatting exceptions will be mitigated to the caller.
        return Integer.parseInt(value);
    }

    public int getArgAsInt(String name, int defaultValue) {
        if (!hasArg(name)) {
            return defaultValue;
        }

        String value = getArg(name);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void parse(String[] args) {
        // Normally I'd use a for-loop for this, but as I modify the index variable which
        // apparently raises a Checkstyle warning I switched to this construct.
        int i = 0;

        while (i < args.length) {
            String arg = args[i++];

            if (arg.startsWith("--") && arg.length() >= 3) {
                // Argument -> --X value.
                // Advance to the value part of the argument.
                i++;

                // Make sure that index is valid..
                // If the index isn't valid this argument is ignored.
                if (i < args.length) {
                    argMap.put(arg.substring(2), args[i]);
                }
            } else if (arg.startsWith("-") && arg.length() >= 2) {
                // Flag -> -X.
                flagList.add(arg.substring(1));
            }

            // If the argument is neither an argument or a flag it is ignored.
        }
    }

}
