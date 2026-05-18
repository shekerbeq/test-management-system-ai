package kz.testmanagement.aigenerator.controller;

import kz.testmanagement.aigenerator.repository.PromptTemplateRepository;
import kz.testmanagement.core.entity.PromptTemplate;
import kz.testmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateRepository repository;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<List<PromptTemplate>> list(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(repository.findByOwnerIdOrSystemTemplateTrue(userId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<PromptTemplate> save(@RequestBody PromptTemplate template, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        template.setOwnerId(currentUserId(authentication));
        template.setSystemTemplate(admin && template.isSystemTemplate());
        return ResponseEntity.ok(repository.save(template));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = currentUserId(authentication);
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return repository.findById(id)
                .filter(template -> admin || userId.equals(template.getOwnerId()))
                .map(template -> {
                    repository.delete(template);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Long currentUserId(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пайдаланушы табылмады"))
                .getId();
    }
}
