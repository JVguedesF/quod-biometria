package com.quodbiometria.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String token;

    @Indexed
    private String userId;

    private LocalDateTime expiryDate;
    private boolean revoked;
    private String keyId;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}