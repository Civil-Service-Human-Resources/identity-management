package uk.gov.cshr.domain;

import lombok.Data;
import lombok.ToString;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Entity
@Data
@ToString
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

    private String agencyTokenUid;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "identity_role",
            joinColumns = @JoinColumn(name = "identity_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles;

    public String getLastLoggedInAsDate() {
        return this.lastLoggedIn.atZone(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("dd/MM/y"));
    }

    public Identity() {
    }

    public Identity(String uid,
                    String email,
                    String password,
                    boolean active,
                    boolean locked,
                    Set<Role> roles,
                    Instant lastLoggedIn,
                    boolean deletionNotificationSent,
                    String agencyTokenUid) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
        this.locked = locked;
        this.lastLoggedIn = lastLoggedIn;
        this.deletionNotificationSent = deletionNotificationSent;
        this.agencyTokenUid = agencyTokenUid;
    }
}
