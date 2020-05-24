package com.code4ro.legalconsultation.converters;

import com.code4ro.legalconsultation.model.dto.DocumentConsolidatedDto;
import com.code4ro.legalconsultation.model.persistence.DocumentConsolidated;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {DocumentMetadataMapper.class, DocumentNodeMapper.class})
public interface DocumentConsolidatedMapper {

    DocumentConsolidatedDto map(DocumentConsolidated documentConsolidated);

}
