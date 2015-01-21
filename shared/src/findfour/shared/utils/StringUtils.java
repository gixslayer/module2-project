package findfour.shared.utils;

import findfour.shared.ArgumentException;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils {
    private static final char QUOTE = '\"';

    public static String extractCommand(String str, char delimiter) {
        if (stringContains(str, delimiter)) {
            return str.substring(0, str.indexOf(delimiter));
        } else {
            return str;
        }
    }

    public static String[] extractArgs(String str, char delimiter, boolean allowQuotes) {
        if (!stringContains(str, delimiter)) {
            return new String[0];
        } else {
            String argString = str.substring(str.indexOf(delimiter) + 1);
            List<String> buffer = new ArrayList<String>();
            boolean insideQuotes = false;
            StringBuilder argBuffer = new StringBuilder();

            for (char c : argString.toCharArray()) {
                if (c == QUOTE) {
                    insideQuotes = !insideQuotes;

                    if (!allowQuotes) {
                        argBuffer.append(QUOTE);
                    }
                } else if (c == delimiter) {
                    if (insideQuotes && allowQuotes) {
                        argBuffer.append(delimiter);
                    } else {
                        buffer.add(argBuffer.toString());
                        argBuffer = new StringBuilder();
                    }
                } else {
                    argBuffer.append(c);
                }
            }

            if (argBuffer.length() != 0) {
                buffer.add(argBuffer.toString());
            }

            return buffer.toArray(new String[0]);
            //return (String[]) buffer.toArray();
        }
    }

    public static boolean tryParseInt(String value, Reference<Integer> result) {
        try {
            int i = Integer.parseInt(value);

            result.set(i);

            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ArgumentException("value", "Not a valid integer");
        }
    }

    // Why doesn't the Java library contain a function for this???
    private static boolean stringContains(String str, char c) {
        for (char strChar : str.toCharArray()) {
            if (strChar == c) {
                return true;
            }
        }

        return false;
    }
}
