package views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * BaseView — shared input/output helpers for every console view.
 *
 * Per project conventions:
 *  - all view methods are static
 *  - all views extend BaseView
 *  - all views share this BufferedReader (don't open another one)
 *  - view methods declare {@code throws IOException}
 *
 * Subclasses do not need to override anything; they reuse {@link #IN}
 * and {@link #prompt} / {@link #separator}.
 */
public abstract class BaseView {

    /** Shared input stream — do NOT close it; closing System.in breaks subsequent views. */
    protected static final BufferedReader IN =
            new BufferedReader(new InputStreamReader(System.in));

    /**
     * Print a prompt and return the user's trimmed reply.
     */
    protected static String prompt(String message) throws IOException {
        System.out.print(message);
        String line = IN.readLine();
        return (line == null) ? "" : line.trim();
    }

    /**
     * Print a divider line for visual separation.
     */
    protected static void separator() {
        System.out.println("------------------------------------------------------------");
    }

    /**
     * Print a heading bracketed by separators.
     */
    protected static void heading(String title) {
        separator();
        System.out.println("  " + title);
        separator();
    }

    /**
     * Try to parse an int; on failure return the supplied fallback.
     */
    protected static int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    protected static double parseDoubleOr(String s, double fallback) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}