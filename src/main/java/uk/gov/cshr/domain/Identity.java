package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Entity
@AllArgsConstructor
public class Identity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 36)
    private String uid;

    @Column(unique = true, length = 150)
    @Email
    private String email;

    @Column(length = 100)
    private String password;

    private Instant lastLoggedIn;

    private boolean deletionNotificationSent;

    private boolean active;

    private boolean locked;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "identity_role",
            joinColumns = @JoinColumn(name = "identity_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles;

    public Identity() {
    }

    public Identity(String uid, String email, String password, boolean active, boolean locked, Set<Role> roles, Instant lastLoggedIn, boolean deletionNotificationSent) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
        this.locked = locked;
        this.lastLoggedIn = lastLoggedIn;
        this.deletionNotificationSent = deletionNotificationSent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Instant getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Instant lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public boolean isDeletionNotificationSent() {
        return deletionNotificationSent;
    }

    public void setDeletionNotificationSent(boolean deletionNotificationSent) {
        this.deletionNotificationSent = deletionNotificationSent;
    }

    @Override
    public String toString() {
        return "Identity{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", lastLoggedIn=" + lastLoggedIn +
                ", active=" + active +
                ", locked=" + locked +
                ", roles=" + roles +
                '}';
    }
}
