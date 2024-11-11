package uk.gov.cshr.config;

import lombok.Getter;

@Getter
public enum Permission {
    READ_IDENTITY("IDENTITY_MANAGER"),
    DELETE_IDENTITY("IDENTITY_DELETE"),
    MANAGE_ROLES("IDENTITY_MANAGE_ROLES"),
    READ_INVITES("IDENTITY_VIEW_INVITES");

    private final String mappedRole;

    Permission(String mappedRole) {
        this.mappedRole = mappedRole;
    }
}
