package com.rtb.manageyourmoneybackend.exportimport.controller;

import com.rksdev.security.web.CurrentUserId;
import com.rtb.manageyourmoneybackend.exportimport.service.DataExportService;
import com.rtb.manageyourmoneybackend.exportimport.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExportImportController {

    private final DataExportService dataExportService;
    private final DataImportService dataImportService;

    @PostMapping("/export")
    public ResponseEntity<Void> requestDataExport(@CurrentUserId Long userId, @RequestParam String email) {
        dataExportService.triggerExportAsync(userId, email);
        return ResponseEntity.accepted().build(); // HTTP 202
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> importData(@CurrentUserId Long userId, @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        dataImportService.importUserData(userId, file);
        return ResponseEntity.ok().build();
    }
}
