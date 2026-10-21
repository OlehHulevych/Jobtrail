package com.huleych.jobtrail.tracking.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String email;
    private String passwordHash;
    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name="user_id"))
    private Set<Skill> skills = new HashSet<>();
    public void addSkill(Skill skill){
        skills.add(skill);
    };
    public void RemoveSkill(String name){
        skills.removeIf(s->s.matches(name));
    }
    public Set<Skill> getSkills(){return Set.copyOf(skills);}
}
