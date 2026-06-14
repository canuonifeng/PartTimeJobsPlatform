package com.parttime.enterprise.standards;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendMapperStandardsTest {

    @Test
    void mappersDoNotUseAnnotationSql() throws IOException {
        Path mapperDir = Path.of("src/main/java/com/parttime/enterprise/mapper");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(mapperDir)) {
            paths.filter(path -> path.toString().endsWith("Mapper.java"))
                    .forEach(path -> collectViolations(path, violations));
        }
        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    private void collectViolations(Path path, List<String> violations) {
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("@Select") || line.contains("@Update")
                        || line.contains("@Insert") || line.contains("@Delete")) {
                    violations.add(path + ":" + (i + 1) + " SQL annotations must be moved to mapper XML");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
