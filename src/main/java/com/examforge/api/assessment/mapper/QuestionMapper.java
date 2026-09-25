package com.examforge.api.assessment.mapper;

import java.util.List;

import com.examforge.api.assessment.dto.OptionRequest;
import com.examforge.api.assessment.dto.OptionResponse;
import com.examforge.api.assessment.dto.QuestionRequest;
import com.examforge.api.assessment.dto.QuestionResponse;
import com.examforge.api.assessment.entity.Option;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.assessment.generation.GeneratedOption;
import com.examforge.api.assessment.generation.GeneratedQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface QuestionMapper {

    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponses(List<Question> questions);

    OptionResponse toResponse(Option option);

    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "options", ignore = true)
    Question toEntity(QuestionRequest request);

    @Mapping(target = "question", ignore = true)
    @Mapping(target = "position", ignore = true)
    Option toEntity(OptionRequest request);

    List<Option> toOptionEntities(List<OptionRequest> requests);

    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "options", ignore = true)
    Question fromGenerated(GeneratedQuestion generated);

    @Mapping(target = "question", ignore = true)
    @Mapping(target = "position", ignore = true)
    Option fromGenerated(GeneratedOption generated);

    List<Option> fromGeneratedOptions(List<GeneratedOption> generated);
}
