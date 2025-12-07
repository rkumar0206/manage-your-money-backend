package com.rtb.manageyourmoneybackend.common.sync.repository;

import com.rtb.manageyourmoneybackend.common.sync.entity.SyncMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncMetadataRepository extends CrudRepository<SyncMetadata, String> {
}
