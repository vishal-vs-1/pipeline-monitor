package com.vigilant.vigilant_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String password;

    private String provider;

    private String providerId;

    @Convert(converter = com.vigilant.vigilant_backend.config.AttributeEncryptor.class)
    private String githubToken;

    @Convert(converter = com.vigilant.vigilant_backend.config.AttributeEncryptor.class)
    private String providerRefreshToken;

    private java.time.Instant providerTokenExpiresAt;
}
