package com.farmmanagement.backend.users;

/** Account state. DISABLED users keep their data but cannot authenticate. */
public enum UserStatus {
    ACTIVE,
    DISABLED
}
