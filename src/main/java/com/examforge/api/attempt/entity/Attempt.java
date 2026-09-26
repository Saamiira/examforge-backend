package com.examforge.api.attempt.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.examforge.api.assessment.entity.Assessment;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attempts", indexes = {
        @Index(name = "idx_attempts_student", columnList = "student_id"),
        @Index(name = "idx_attempts_assessment", columnList = "assessment_id")
})
public class Attempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant submittedAt;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    private Integer correctCount;

    private Integer totalQuestions;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    public Attempt(User student, Assessment assessment) {
        this.student = student;
        this.assessment = assessment;
        this.startedAt = Instant.now();
    }

    public void addAnswer(Answer answer) {
        answer.setAttempt(this);
        answers.add(answer);
    }

    public void submit(BigDecimal score, int correctCount, int totalQuestions) {
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = Instant.now();
        this.score = score;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
    }

    public boolean isSubmitted() {
        return status == AttemptStatus.SUBMITTED;
    }

    public boolean belongsTo(Long userId) {
        return student != null && student.getId().equals(userId);
    }
}
