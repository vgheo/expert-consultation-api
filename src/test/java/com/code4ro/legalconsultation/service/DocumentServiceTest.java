package com.code4ro.legalconsultation.service;

import com.code4ro.legalconsultation.converters.DocumentConsolidatedMapper;
import com.code4ro.legalconsultation.model.dto.DocumentConsolidatedDto;
import com.code4ro.legalconsultation.model.dto.DocumentMetadataDto;
import com.code4ro.legalconsultation.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.model.persistence.User;
import com.code4ro.legalconsultation.service.api.CommentService;
import com.code4ro.legalconsultation.service.impl.DocumentConsolidatedService;
import com.code4ro.legalconsultation.service.impl.DocumentMetadataService;
import com.code4ro.legalconsultation.service.impl.DocumentServiceImpl;
import com.code4ro.legalconsultation.service.impl.UserService;
import com.code4ro.legalconsultation.util.RandomObjectFiller;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    @Mock
    private DocumentConsolidatedService documentConsolidatedService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentMetadataService documentMetadataService;
    @Mock
    private CommentService commentService;
    @Mock
    private DocumentConsolidatedMapper documentConsolidatedMapper;

    @Captor
    private ArgumentCaptor<DocumentConsolidated> documentConsolidatedArgumentCaptor;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    public void getDocument() {
        final UUID uuid = UUID.randomUUID();
        when(documentMetadataService.fetchOne(any(UUID.class))).thenReturn(new DocumentMetadataDto());

        documentService.fetchOne(uuid);

        verify(documentMetadataService).fetchOne(uuid);
    }

    @Test
    public void getDocumentConsolidated() {
        final UUID uuid = UUID.randomUUID();
        final DocumentConsolidated document = new DocumentConsolidated();
        final DocumentNode documentNode = new DocumentNode();
        documentNode.setId(UUID.randomUUID());
        document.setDocumentNode(documentNode);

        when(documentConsolidatedService.getByDocumentMetadataId(any(UUID.class))).thenReturn(document);
        when(documentConsolidatedMapper.map(any())).thenReturn(new DocumentConsolidatedDto());

        documentService.fetchConsolidatedByMetadataId(uuid);

        verify(documentConsolidatedService).getByDocumentMetadataId(uuid);
    }

    @Test
    public void getAllDocuments() {
        documentService.fetchAll(Pageable.unpaged());

        verify(documentMetadataService).fetchAll(Pageable.unpaged());
    }

    @Test
    public void deleteDocument() {
        final UUID uuid = UUID.randomUUID();

        documentService.deleteById(uuid);

        verify(documentConsolidatedService).deleteById(uuid);
    }

    @Test
    public void assignUsers() {
        final DocumentMetadata metadata = RandomObjectFiller.createAndFillWithBaseEntity(DocumentMetadata.class);
        final DocumentConsolidated documentConsolidated = new DocumentConsolidated();
        documentConsolidated.setId(UUID.randomUUID());
        documentConsolidated.setDocumentMetadata(metadata);

        final User user1 = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        final User user2 = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        final User user3 = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        final List<User> assignedUsers = Arrays.asList(user1, user2, user3);
        final Set<UUID> assignedUsersIds = assignedUsers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        when(documentConsolidatedService.getByDocumentMetadataId(documentConsolidated.getDocumentMetadata().getId()))
                .thenReturn(documentConsolidated);
        when(userService.findByIds(assignedUsersIds)).thenReturn(assignedUsers);

        documentService.assignUsers(documentConsolidated.getDocumentMetadata().getId(), assignedUsersIds);

        verify(documentConsolidatedService).saveOne(documentConsolidatedArgumentCaptor.capture());
        final DocumentConsolidated capturedDocument = documentConsolidatedArgumentCaptor.getValue();
        assertThat(capturedDocument.getAssignedUsers()).hasSize(3);
        assertThat(capturedDocument.getAssignedUsers()).containsAll(assignedUsers);
    }
}
