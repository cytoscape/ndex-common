package org.ndexbio.common.models.data;

public enum Status
{
    QUEUED,
    STAGED,
    PROCESSING,
    COMPLETED,
    COMPLETED_WITH_WARNINGS,
    COMPLETED_WITH_ERRORS,
    FAILED
}