package com.examforge.api.academic.entity;

import com.examforge.api.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "careers")
public class Career extends BaseEntity {
    private String name;
}
