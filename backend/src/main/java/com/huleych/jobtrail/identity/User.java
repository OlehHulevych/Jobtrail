package com.huleych.jobtrail.identity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;
    @Column(nullable = false)
    private String passwordHash;
    @CreationTimestamp
    private LocalDate createdAt;
    @UpdateTimestamp
    private LocalDate updatedAt;
    @ElementCollection
    @CollectionTable(name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
    private Set<Skill> skills = new HashSet<>();

    protected User() {

    }

    public User(String email, String passwordHash, Set<Skill> skills) {
        if (email == null) throw new IllegalArgumentException("Email is required");
        if (passwordHash == null) throw new IllegalArgumentException("Password is required");
        this.email = new Email(email);
        this.passwordHash = passwordHash;
        if (skills != null) this.skills.addAll(skills);
    }

    public void addSkill(Skill skill) {
        if (skill == null) throw new IllegalArgumentException("Skill is required");
        skills.add(skill);
    }

    public void removeSkill(String name) {
        if (name == null) throw new IllegalArgumentException("name of skill is required");
        skills.removeIf(s -> s.matches(name));
    }

    public Set<Skill> getSkills() {
        return Set.copyOf(skills);
    }



    public void changePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null) throw new IllegalArgumentException("new password is required");
        this.passwordHash = newPasswordHash;
    }

    public void changeEmail(String newEmail) {
        email = new Email(newEmail);
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }
}
