package io.plyschik.springbootblog.entity;

import io.plyschik.springbootblog.dto.PostCountByYearAndMonthDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@NamedNativeQuery(
    name = "count_posts_by_year_and_month",
    query = "SELECT YEAR(p.created_at) AS year, MONTH(p.created_at) AS month, COUNT(*) AS count " +
            "FROM posts p " +
            "GROUP BY year, month " +
            "ORDER BY year DESC, month DESC",
    resultSetMapping = "count_posts_by_year_and_month_mapping"
)
@SqlResultSetMapping(
    name = "count_posts_by_year_and_month_mapping",
    classes = @ConstructorResult(
        targetClass = PostCountByYearAndMonthDto.class,
        columns = {
            @ColumnResult(name = "year", type = Integer.class),
            @ColumnResult(name = "month", type = Integer.class),
            @ColumnResult(name = "count", type = Integer.class)
        }
    )
)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank
    @Length(min = 2, max = 120)
    @Column(nullable = false, length = 120)
    private String title;

    @NotBlank
    @Length(min = 4, max = 65535)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotBlank
    @Length(min = 4, max = 65535)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentRaw;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "posts_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post")
    private List<Comment> comments;

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Post post = (Post) object;

        return id.equals(post.id) && title.equals(post.title) && content.equals(post.content) && createdAt.equals(post.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, createdAt);
    }
}
