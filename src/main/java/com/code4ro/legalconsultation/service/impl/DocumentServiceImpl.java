package com.code4ro.legalconsultation.service.impl;

import com.code4ro.legalconsultation.converters.DocumentConsolidatedMapper;
import com.code4ro.legalconsultation.converters.UserMapper;
import com.code4ro.legalconsultation.model.dto.DocumentConsolidatedDto;
import com.code4ro.legalconsultation.model.dto.DocumentMetadataDto;
import com.code4ro.legalconsultation.model.dto.DocumentViewDto;
import com.code4ro.legalconsultation.model.dto.UserDto;
import com.code4ro.legalconsultation.model.persistence.*;
import com.code4ro.legalconsultation.service.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentConsolidatedService documentConsolidatedService;
    private final DocumentMetadataService documentMetadataService;
    private final PDFService pdfService;
    private final DocumentNodeService documentNodeService;
    private final StorageApi storageApi;
    private final UserService userService;
    private final UserMapper userMapperService;
    private final DocumentConsolidatedMapper documentConsolidatedMapper;
    private final CommentService commentService;

    @Autowired
    public DocumentServiceImpl(final DocumentConsolidatedService documentConsolidatedService,
                               final DocumentMetadataService documentMetadataService,
                               final PDFService pdfService,
                               final DocumentNodeService documentNodeService,
                               final StorageApi storageApi,
                               final UserService userService,
                               final UserMapper userMapperService,
                               final DocumentConsolidatedMapper documentConsolidatedMapper,
                               final CommentService commentService) {
        this.documentConsolidatedService = documentConsolidatedService;
        this.documentMetadataService = documentMetadataService;
        this.pdfService = pdfService;
        this.documentNodeService = documentNodeService;
        this.storageApi = storageApi;
        this.userService = userService;
        this.userMapperService = userMapperService;
        this.documentConsolidatedMapper = documentConsolidatedMapper;
        this.commentService = commentService;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DocumentMetadata> fetchAll(Pageable pageable) {
        return documentMetadataService.fetchAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentMetadataDto fetchOne(final UUID id) {
        return documentMetadataService.fetchOne(id);
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentConsolidatedDto fetchConsolidatedByMetadataId(final UUID id) {
        final DocumentConsolidated document = documentConsolidatedService.getByDocumentMetadataId(id);
        return documentConsolidatedMapper.map(document);
    }

    @Transactional
    @Override
    public DocumentConsolidated create(final DocumentViewDto document) {

        DocumentMetadata metadata = documentMetadataService.build(document);
        metadata.setFilePath(document.getFilePath());
        final String pdfContent = pdfService.readAsString(storageApi.loadFile(document.getFilePath()));
        final DocumentNode documentNode = documentNodeService.parse(pdfContent);
        final DocumentConfiguration documentConfiguration = new DocumentConfiguration(true, true);

        return documentConsolidatedService.saveOne(new DocumentConsolidated(metadata, documentNode, documentConfiguration));
    }

    @Transactional
    @Override
    public DocumentConsolidated update(final UUID id,
                                       final DocumentViewDto document) {
        final DocumentConsolidated consolidated = documentConsolidatedService.getEntity(id);

        // TODO delete current file from storage and the document node

        //update the metadata
        DocumentMetadata metadata = documentMetadataService.build(document);
        metadata.setFilePath(document.getFilePath());
        metadata.setId(consolidated.getDocumentMetadata().getId());

        final String pdfContent = pdfService.readAsString(storageApi.loadFile(document.getFilePath()));
        final DocumentNode documentNode = documentNodeService.parse(pdfContent);

        consolidated.setDocumentMetadata(metadata);
        consolidated.setDocumentNode(documentNode);
        return documentConsolidatedService.saveOne(consolidated);
    }

    @Transactional
    @Override
    public void deleteById(final UUID id) {
        documentConsolidatedService.getEntity(id);
        documentConsolidatedService.deleteById(id);
    }

    @Override
    public void assignUsers(final UUID id, final Set<UUID> userIds) {
        final List<User> users = userService.findByIds(userIds);
        final DocumentConsolidated documentConsolidated = documentConsolidatedService.getByDocumentMetadataId(id);
        documentConsolidated.setAssignedUsers(users);

        documentConsolidatedService.saveOne(documentConsolidated);
    }

    @Override
    public List<UserDto> getAssignedUsers(final UUID id) {
        final DocumentConsolidated documentConsolidated = documentConsolidatedService.getByDocumentMetadataId(id);
        final List<User> assignedUsers = documentConsolidated.getAssignedUsers();

        return assignedUsers.stream().map(userMapperService::map).collect(Collectors.toList());
    }
}
