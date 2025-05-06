package com.quodbiometria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.quodbiometria.config.JwtAuthenticationFilter;
import com.quodbiometria.service.UserService;
import com.quodbiometria.util.JwtUtil;

@DataMongoTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "jwt.secret=test-jwt-secret-key-for-testing-purposes-only",
        "spring.security.user.name=test-admin",
        "spring.security.user.password=test-password"
})
class QuodBiometriaApplicationTests {

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testValidToken() {
        // Simulate a valid token and test the authentication filter

        // Add your logic here to test the valid token
        // For example, you might want to call jwtAuthenticationFilter with the token
        // and assert that the user is authenticated
    }

    @Test
    void testInvalidToken() {
        // Simulate an invalid token and test the authentication filter
        String invalidToken = "invalid.jwt.token";
        // Add your logic here to test the invalid token
        // For example, you might want to call jwtAuthenticationFilter with the token
        // and assert that an exception is thrown or the user is not authenticated
    }
}