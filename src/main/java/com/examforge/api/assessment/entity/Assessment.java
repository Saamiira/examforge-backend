package com.examforge.api.assessment.entity;

import java.util.ArrayList;
import java.util.List;

import com.examforge.api.academic.entity.Course;
import com.examforge.api.common.entity.BaseEntity;
import com.examforge.api.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "assessments", indexes = {
        @Index(name = "idx_assessments_author", columnList = "author_id"),
        @Index(name = "idx_assessments_course", columnList = "course_id")
})
public class Assessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(length = 500)
    private String failureReason;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    public void addQuestion(Question question) {
        question.setAssessment(this);
        question.setPosition(questions.size() + 1);
        questions.add(question);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setAssessment(null);
    }

    public boolean isAuthoredBy(Long userId) {
        return author != null && author.getId().equals(userId);
    }
}
