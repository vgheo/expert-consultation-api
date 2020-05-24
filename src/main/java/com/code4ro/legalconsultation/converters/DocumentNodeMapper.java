package com.code4ro.legalconsultation.converters;

import com.code4ro.legalconsultation.model.dto.documentnode.DocumentNodeCreateDto;
import com.code4ro.legalconsultation.model.dto.documentnode.DocumentNodeDto;
import com.code4ro.legalconsultation.model.persistence.DocumentNode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {NumberOfCommentsMapper.class})
public interface DocumentNodeMapper {

    DocumentNodeDto map(DocumentNode documentNode);

    DocumentNode map(DocumentNodeDto documentNodeDto);

    DocumentNode map(DocumentNodeCreateDto documentNodeCreateDto);
}
