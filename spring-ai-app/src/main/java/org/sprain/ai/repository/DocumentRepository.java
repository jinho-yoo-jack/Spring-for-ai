package org.sprain.ai.repository;

import org.sprain.ai.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByFilenameContaining(String filename);

    List<DocumentEntity> findByContentType(String contentType);

    List<DocumentEntity> findByContentContaining(String content);

}
