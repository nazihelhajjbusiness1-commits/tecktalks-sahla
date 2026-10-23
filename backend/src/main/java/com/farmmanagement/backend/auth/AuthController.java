package com.farmmanagement.backend.auth;

import com.farmmanagement.backend.auth.dto.AuthResponse;
import com.farmmanagement.backend.auth.dto.LoginRequest;
import com.farmmanagement.backend.auth.dto.RefreshRequest;
import com.farmmanagement.backend.auth.dto.RegisterRequest;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.response.ApiResponse;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import com.farmmanagement.backend.users.UserStatus;
import com.farmmanagement.backend.users.dto.ResetPasswordRequest;
import com.farmmanagement.backend.users.dto.UpdateUserRequest;
import com.farmmanagement.backend.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "User registered successfully",
                                toResponse(user)
                        )
                );
    }

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response
                )
        );
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed", response)
        );
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Logged out", null)
        );
    }

    @GetMapping("/users")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> listUsers() {
        java.util.List<UserResponse> users = authService.listUsers().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    /** Admin: change a user's role and/or active status (partial update). */
    @PatchMapping("/users/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        // Guard against an admin locking themselves out by disabling or
        // demoting their own account.
        User current = authService.getCurrentUser(authentication.getName());
        boolean demotingSelf = request.getRole() != null && request.getRole() != Role.ADMIN;
        boolean disablingSelf = request.getStatus() == UserStatus.DISABLED;
        if (current.getId().equals(id) && (demotingSelf || disablingSelf)) {
            throw new ConflictException(
                    "You cannot disable or change the role of your own account.");
        }

        User updated = userService.updateUser(id, request.getRole(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("User updated", toResponse(updated)));
    }

    /** Admin: set a new password for a user account. */
    @PostMapping("/users/{id}/reset-password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset", null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            Authentication authentication
    ) {
        User user = authService.getCurrentUser(
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user retrieved successfully",
                        toResponse(user)
                )
        );
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
