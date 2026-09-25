package com.examforge.api.assessment.entity;

import java.util.ArrayList;
import java.util.List;

import com.examforge.api.common.entity.BaseEntity;
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
@Table(name = "questions", indexes = @Index(name = "idx_questions_assessment", columnList = "assessment_id"))
public class Question extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(length = 120)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private int position;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Option> options = new ArrayList<>();

    public void addOption(Option option) {
        option.setQuestion(this);
        option.setPosition(options.size() + 1);
        options.add(option);
    }

    public void replaceOptions(List<Option> newOptions) {
        options.clear();
        newOptions.forEach(this::addOption);
    }
}
