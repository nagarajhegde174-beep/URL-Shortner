package com.dsa.studio.controller;

import com.dsa.studio.dto.request.CompileRequest;
import com.dsa.studio.dto.response.ApiResponse;
import com.dsa.studio.dto.response.CompileResponse;
import com.dsa.studio.service.CompilerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compiler")
@RequiredArgsConstructor
@Tag(name = "Compiler", description = "Java source compilation endpoints")
public class CompilerController {

    private final CompilerService compilerService;

    @PostMapping("/compile")
    @Operation(summary = "Compile user Java code with debugging information enabled")
    public ResponseEntity<ApiResponse<CompileResponse>> compile(@Valid @RequestBody CompileRequest request) {
        CompileResponse response = compilerService.compile(request.getClassName(), request.getCode());
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("Compilation successful", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Compilation failed", response));
        }
    }
}
