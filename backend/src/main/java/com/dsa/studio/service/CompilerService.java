package com.dsa.studio.service;

import com.dsa.studio.dto.response.CompileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

@Service
@Slf4j
public class CompilerService {

    private static final String WORKSPACE_DIR = "target/user-classes";

    public CompilerService() {
        // Ensure workspace directory exists
        try {
            Files.createDirectories(Paths.get(WORKSPACE_DIR));
        } catch (IOException e) {
            log.error("Failed to create workspace directory: {}", e.getMessage());
        }
    }

    public CompileResponse compile(String className, String code) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return CompileResponse.builder()
                    .success(false)
                    .message("JDK System Java Compiler not found! Ensure Spring Boot is running on JDK, not JRE.")
                    .build();
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), StandardCharsets.UTF_8);

        try {
            // Write java source file to temporary workspace
            Path sourcePath = Paths.get(WORKSPACE_DIR, className + ".java");
            Files.writeString(sourcePath, code);

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(Arrays.asList(sourcePath));
            
            // Compilation options - generate debugging symbols (-g) to let JDI inspect local variables
            Iterable<String> options = Arrays.asList("-g", "-d", WORKSPACE_DIR);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    compilationUnits
            );

            boolean success = task.call();
            fileManager.close();

            // Clean up the .java source file after compilation to keep target clean (or keep it for JDI references)
            // Files.deleteIfExists(sourcePath);

            if (success) {
                return CompileResponse.builder()
                        .success(true)
                        .message("Compilation Successful")
                        .build();
            } else {
                StringBuilder errorMsg = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errorMsg.append(String.format("Line %d: %s\n",
                            diagnostic.getLineNumber(),
                            diagnostic.getMessage(Locale.getDefault())));
                }
                return CompileResponse.builder()
                        .success(false)
                        .message("Compilation Failed")
                        .errors(errorMsg.toString())
                        .build();
            }
        } catch (Exception e) {
            log.error("Compilation error: ", e);
            return CompileResponse.builder()
                    .success(false)
                    .message("Internal compiler error: " + e.getMessage())
                    .build();
        }
    }

    public String getWorkspaceDir() {
        return new File(WORKSPACE_DIR).getAbsolutePath();
    }
}
