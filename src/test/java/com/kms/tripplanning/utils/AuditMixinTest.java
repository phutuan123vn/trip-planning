package com.kms.tripplanning.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class AuditMixinTest {

    private static class TestEntity extends AuditMixin {}

    @Test
    void isDeleted_shouldReturnFalse_whenDeletedAtIsNull() {
        TestEntity entity = new TestEntity();
        
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_shouldReturnTrue_whenDeletedAtIsSet() {
        TestEntity entity = new TestEntity();
        entity.setDeletedAt(OffsetDateTime.now());
        
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    void markDeleted_shouldSetDeletedAt() {
        TestEntity entity = new TestEntity();
        entity.markDeleted();
        
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.isDeleted()).isTrue();
    }
}
