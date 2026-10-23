package com.farmmanagement.backend.users;

import com.farmmanagement.backend.auth.Role;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /** Admin partial-update of a user's role and/or status. */
    @Transactional
    public User updateUser(Long id, Role role, UserStatus status) {
        User user = getUserById(id);
        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }
        return userRepository.save(user);
    }

    /** Admin reset of a user's password to a new (raw) value. */
    @Transactional
    public User resetPassword(Long id, String newRawPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newRawPassword));
        return userRepository.save(user);
    }

    /**
     * Admin delete of a user account. A user who has activity on record
     * (deliveries weighed/graded, settlements confirmed, payments recorded…) is
     * referenced by foreign keys and cannot be hard-deleted without destroying
     * that history — those should be deactivated instead. We flush so the FK
     * violation surfaces here as a clear conflict rather than a generic error.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "This user has records on the platform and can't be deleted. "
                            + "Deactivate the account instead.");
        }
    }
}
