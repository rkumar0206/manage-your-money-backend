package com.rtb.manageyourmoneybackend.common.sync.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "sync_metadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncMetadata {
    @Id
    @Column(name = "job_name", unique = true, nullable = false)
    private String jobName;

    @Column(name = "last_sync")
    private Long lastSync;
}