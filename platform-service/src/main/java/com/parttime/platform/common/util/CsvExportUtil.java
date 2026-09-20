package com.parttime.platform.common.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static byte[] export(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('﻿');
        sb.append(String.join(",", headers.stream().map(CsvExportUtil::escape).toList())).append("\r\n");
        if (rows != null) {
            for (List<Object> row : rows) {
                sb.append(String.join(",", row.stream().map(v -> escape(v == null ? "" : String.valueOf(v))).toList())).append("\r\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void write(OutputStream out, List<String> headers, List<List<Object>> rows) throws IOException {
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write('﻿');
            writer.write(String.join(",", headers.stream().map(CsvExportUtil::escape).toList()));
            writer.write("\r\n");
            if (rows != null) {
                for (List<Object> row : rows) {
                    writer.write(String.join(",", row.stream().map(v -> escape(v == null ? "" : String.valueOf(v))).toList()));
                    writer.write("\r\n");
                }
            }
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }
}
