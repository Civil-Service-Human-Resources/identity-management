package uk.gov.cshr.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
public class Reset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, length = 40, nullable = false)
    private String code;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ResetStatus resetStatus;

    @Column(nullable = false)
    private Date requestedAt;

    private Date resetAt;

    @Column(length = 150, nullable = false)
    private String email;

}
