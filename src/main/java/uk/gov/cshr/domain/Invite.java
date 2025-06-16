package uk.gov.cshr.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Setter
@Getter
@Entity
public class Invite implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, length = 40, nullable = false)
    private String code;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private InviteStatus status;

    @OneToOne(optional = false)
    private Identity inviter;

    @Column(nullable = false)
    private Date invitedAt;

    private Date acceptedAt;

    @Column(length = 150, nullable = false)
    private String forEmail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "invite_role",
            joinColumns = @JoinColumn(name = "invite_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> forRoles;

    @Override
    public String toString() {
        return "Invite{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", inviter=" + inviter +
                ", invitedAt=" + invitedAt +
                ", acceptedAt=" + acceptedAt +
                ", forEmail='" + forEmail + '\'' +
                ", forRoles=" + forRoles +
                '}';
    }

}
