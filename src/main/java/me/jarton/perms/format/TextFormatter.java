package me.jarton.perms.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextFormatter {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
        "(?is)\\[gradient:(#[a-f0-9]{6}(?:,#[a-f0-9]{6})*)(?::([a-z]*))?](.*?)\\[/gradient]"
    );

    public String format(String input) {
        if (input == null || input.isBlank()) return "";
        return ChatColour.colorize(applyGradients(input));
    }

    private String applyGradients(String input) {
        Matcher m = GRADIENT_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder(input.length());
        while (m.find()) {
            String[] stops = m.group(1).split(",");
            String flags = m.group(2) != null ? m.group(2) : "";
            String codes = (flags.contains("b") ? "§l" : "") + (flags.contains("i") ? "§o" : "");
            m.appendReplacement(sb, Matcher.quoteReplacement(Gradient.apply(m.group(3), stops, codes)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
