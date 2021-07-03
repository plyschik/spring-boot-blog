package io.plyschik.springbootblog.entity;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@RequiredArgsConstructor
@ToString(exclude = {"posts"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Email
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String password;

    @Column(name = "first_name", nullable = false, length = 30)
    @NotBlank
    @Length(min = 2, max = 30)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 30)
    @NotBlank
    @Length(min = 2, max = 30)
    private String lastName;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Post> posts;

    public String fullName() {
        return String.format("%s %s", firstName, lastName);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) return false;

        User user = (User) object;

        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return 562048007;
    }
}
