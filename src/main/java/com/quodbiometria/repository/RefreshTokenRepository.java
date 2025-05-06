package com.quodbiometria.repository;

import com.quodbiometria.model.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUserIdAndRevokedFalse(String userId);
    void deleteByUserId(String userId);
    @SuppressWarnings("unused")
    Optional<RefreshToken> findByUserIdAndToken(String userId, String token);
}