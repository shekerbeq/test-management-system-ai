package kz.testmanagement.aigenerator.controller;

import kz.testmanagement.aigenerator.repository.AiProviderSettingRepository;
import kz.testmanagement.core.entity.AiProviderSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AiSettingsController {

    private final AiProviderSettingRepository repository;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<AiProviderSetting> save(@RequestBody AiProviderSetting setting) {
        return ResponseEntity.ok(repository.save(setting));
    }
}
