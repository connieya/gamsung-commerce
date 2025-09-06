package com.loopers.domain.audit;

import java.util.List;

public interface AuditRepository {
    void saveAll(List<EventLog> eventLogs);
}
