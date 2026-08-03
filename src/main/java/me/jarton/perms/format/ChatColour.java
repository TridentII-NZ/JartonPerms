package me.jarton.perms.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatColour {

    private static final char COLOR_CHAR = '§';
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#([a-f0-9]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)&x(&[a-f0-9]){6}");
    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");

    private ChatColour() {}

    public static String colorize(String input) {
        if (input == null || input.isBlank()) return "";
        return translateLegacyColors(replaceLegacyHexColors(replaceHexColors(input)));
    }

    static String hexColour(String hex) {
        String normalized = hex.startsWith("#") ? hex.substring(1) : hex;
        StringBuilder builder = new StringBuilder(14);
        builder.append(COLOR_CHAR).append('x');
        for (int i = 0; i < normalized.length(); i++) {
            builder.append(COLOR_CHAR).append(normalized.charAt(i));
        }
        return builder.toString();
    }

    private static String replaceHexColors(String input) {
        Matcher m = HEX_COLOR_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder(input.length());
        while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement(hexColour(m.group(1))));
        m.appendTail(sb);
        return sb.toString();
    }

    private static String replaceLegacyHexColors(String input) {
        Matcher m = LEGACY_HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder(input.length());
        while (m.find()) {
            String hex = m.group().replace("&x", "").replace("&", "");
            m.appendReplacement(sb, Matcher.quoteReplacement(hexColour(hex)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String translateLegacyColors(String input) {
        Matcher m = ALT_COLOR_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder(input.length());
        while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement(COLOR_CHAR + m.group(1).toLowerCase()));
        m.appendTail(sb);
        return sb.toString();
    }
}
