package com.parttime.cservice.standards;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendControllerStandardsTest {

    private static final String API_PREFIX = "/api/worker";

    @Test
    void controllersUseProjectHttpContract() throws IOException {
        Path controllerDir = Path.of("src/main/java/com/parttime/cservice/controller");
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
                    violations.add(path + ":" + (i + 1) + " worker APIs must start with " + API_PREFIX);
                }
                if (line.contains("@PathVariable")) {
                    violations.add(path + ":" + (i + 1) + " @PathVariable is not allowed");
                }
                if (line.contains("@RequestBody Map") || line.contains("@RequestBody List") || line.contains("@RequestBody Set")) {
                    violations.add(path + ":" + (i + 1) + " @RequestBody must use typed cmd");
                }
                if (line.contains("ApiResponse<Map") || line.matches(".*public\\s+Map<.*")) {
                    violations.add(path + ":" + (i + 1) + " Map response is not allowed");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
