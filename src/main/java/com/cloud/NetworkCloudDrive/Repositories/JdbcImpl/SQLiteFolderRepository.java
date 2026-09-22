package com.cloud.NetworkCloudDrive.Repositories.JdbcImpl;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SQLiteFolderRepository extends JpaRepository<FolderMetadata, Long> {
    List<FolderMetadata> findAllByPathContainsIgnoreCase(String path);

    boolean existsFolderMetadataByName(String name);

    List<FolderMetadata> findAllByUserid(Long userid);

    void deleteAllByUserid(Long userid);

    Page<FolderMetadata> findAllByUseridAndLastUpdatedNotNullOrderByLastUpdatedDesc(long userId, Pageable pageable);

    List<FolderMetadata> findAllByUseridAndMarked(Long userid, boolean marked);
}
