package com.parttime.enterprise.standards;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendControllerStandardsTest {

    private static final String API_PREFIX = "/api/enterprise";

    @Test
    void controllersUseProjectHttpContract() throws IOException {
        Path controllerDir = Path.of("src/main/java/com/parttime/enterprise/controller");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(controllerDir)) {
            paths.filter(path -> path.toString().endsWith("Controller.java"))
                    .forEach(path -> collectViolations(path, violations));
        }
        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    private void collectViolations(Path path, List<String> violations) {
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("@PutMapping") || line.contains("@DeleteMapping") || line.contains("@PatchMapping")) {
                    violations.add(path + ":" + (i + 1) + " only @GetMapping/@PostMapping are allowed");
                }
                if (line.matches(".*@(RequestMapping|GetMapping|PostMapping)\\(\\\"/api.*")
                        && !line.contains("\"" + API_PREFIX)) {
                    violations.add(path + ":" + (i + 1) + " enterprise APIs must start with " + API_PREFIX);
                }
                if (line.contains("@PathVariable")) {
                    violations.add(path + ":" + (i + 1) + " @PathVariable is not allowed");
                }
                if (line.matches(".*@RequestBody(\\([^)]*\\))?\\s+(Map|List|Set)\\b.*")) {
                    violations.add(path + ":" + (i + 1) + " @RequestBody must use typed cmd");
                }
                if (line.contains("ApiResponse<Map") || line.matches(".*public\\s+Map<.*")) {
                    violations.add(path + ":" + (i + 1) + " Map response is not allowed");
                }
                if (line.contains("@PostMapping")) {
                    StringBuilder block = new StringBuilder(line);
                    for (int j = i + 1; j < lines.size() && j <= i + 12; j++) {
                        block.append(' ').append(lines.get(j));
                        if (lines.get(j).contains("{")) {
                            break;
                        }
                    }
                    if (block.toString().contains("@RequestParam") && !block.toString().contains("MultipartFile")) {
                        violations.add(path + ":" + (i + 1) + " POST must not use @RequestParam");
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
