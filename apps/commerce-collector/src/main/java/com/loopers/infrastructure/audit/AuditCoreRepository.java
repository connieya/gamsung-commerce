package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.AuditRepository;
import com.loopers.domain.audit.EventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuditCoreRepository implements AuditRepository {

    private final EventLogJpaRepository eventLogJpaRepository;

    @Override
    public void saveAll(List<EventLog> eventLogs) {
        eventLogJpaRepository.saveAll(eventLogs);
    }
}
