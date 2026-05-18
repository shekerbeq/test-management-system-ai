package kz.testmanagement.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_provider_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiProviderSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 10;
}
