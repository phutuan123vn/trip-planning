package com.kms.tripplanning.utils;

import com.kms.tripplanning.config.AuthUserDetails;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class AuditMixinListener {

    @PrePersist
    @PreUpdate
    public void logUserBefore(Object entity) {
        if (!(entity instanceof AuditMixin)) {
            return; // Only process entities that implement AuditMixin
        }

        AuthUserDetails currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        AuditMixin auditEntity = (AuditMixin) entity;
        if (auditEntity.getCreatedBy() == null) {
            auditEntity.setCreatedBy(currentUser.getId());
        } else if (auditEntity.isDeleted() && auditEntity.getDeletedBy() == null) {
            auditEntity.setDeletedBy(currentUser.getId());
        } else {
            auditEntity.setUpdatedBy(currentUser.getId());
        }
    }

    @PostPersist
    @PostUpdate
    public void logUserAfter(Object entity) {
        // This method is intentionally left blank to ensure that the @PrePersist and @PreUpdate methods are called before the transaction is committed.
    }

}
