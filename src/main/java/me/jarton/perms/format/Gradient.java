package me.jarton.perms.format;

public final class Gradient {

    private Gradient() {}

    public static String apply(String input, String[] hexStops, String formatCodes) {
        if (input == null || input.isEmpty() || hexStops.length < 2) return "";
        int len = input.length();
        int segments = hexStops.length - 1;
        StringBuilder builder = new StringBuilder(len * (15 + formatCodes.length()));
        for (int i = 0; i < len; i++) {
            double t = (double) i / Math.max(1, len - 1);
            int seg = Math.min((int) (t * segments), segments - 1);
            double segT = (t * segments) - seg;
            int[] start = rgb(hexStops[seg]);
            int[] end = rgb(hexStops[seg + 1]);
            int r = interpolate(start[0], end[0], segT);
            int g = interpolate(start[1], end[1], segT);
            int b = interpolate(start[2], end[2], segT);
            builder.append(ChatColour.hexColour(String.format("%02x%02x%02x", r, g, b)));
            builder.append(formatCodes);
            builder.append(input.charAt(i));
        }
        return builder.toString();
    }

    private static int[] rgb(String hex) {
        String n = hex.startsWith("#") ? hex.substring(1) : hex;
        return new int[]{
            Integer.parseInt(n.substring(0, 2), 16),
            Integer.parseInt(n.substring(2, 4), 16),
            Integer.parseInt(n.substring(4, 6), 16)
        };
    }

    private static int interpolate(int start, int end, double ratio) {
        return (int) Math.round(start + ((end - start) * ratio));
    }
}
