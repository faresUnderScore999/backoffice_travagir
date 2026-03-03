package java_project.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvUtils {

    private CsvUtils() {
    }

    public static String escape(String value) {
        if (value == null) return "";
        boolean mustQuote = value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"");
        String escaped = value.replace("\"", "\"\"");
        return mustQuote ? '"' + escaped + '"' : escaped;
    }

    public static void write(Path file, List<String> headers, List<List<String>> rows) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            if (headers != null && !headers.isEmpty()) {
                w.write(String.join(",", headers.stream().map(CsvUtils::escape).toList()));
                w.newLine();
            }
            if (rows == null) return;
            for (List<String> row : rows) {
                if (row == null) {
                    w.newLine();
                    continue;
                }
                w.write(String.join(",", row.stream().map(CsvUtils::escape).toList()));
                w.newLine();
            }
        }
    }
}
