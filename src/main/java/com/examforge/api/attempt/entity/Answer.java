package com.examforge.api.attempt.entity;

import com.examforge.api.assessment.entity.Option;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "answers", uniqueConstraints = @UniqueConstraint(
        name = "uk_answers_attempt_question", columnNames = {"attempt_id", "question_id"}))
public class Answer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private Option selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    public Answer(Question question, Option selectedOption) {
        this.question = question;
        this.selectedOption = selectedOption;
        this.correct = selectedOption != null && selectedOption.isCorrect();
    }
}
