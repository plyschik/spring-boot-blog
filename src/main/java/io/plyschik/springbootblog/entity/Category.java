package io.plyschik.springbootblog.entity;

import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@NamedNativeQuery(
    name = "Category.findTop5WithPostsCount",
    query = "SELECT c.id, c.name, COUNT(p.id) AS postsCount " +
            "FROM categories c " +
            "LEFT JOIN posts p ON c.id = p.category_id " +
            "GROUP BY c.id " +
            "ORDER BY postsCount DESC " +
            "LIMIT 5",
    resultSetMapping = "Category.findTop5WithPostsCountMapping"
)
@SqlResultSetMapping(
    name = "Category.findTop5WithPostsCountMapping",
    classes = @ConstructorResult(
        targetClass = CategoryWithPostsCount.class,
        columns = {
            @ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "name", type = String.class),
            @ColumnResult(name = "postsCount", type = Long.class)
        }
    )
)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank
    @Length(min = 2, max = 30)
    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Post> posts;

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
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

        Category category = (Category) object;

        return id.equals(category.id) && name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
