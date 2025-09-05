package com.loopers.domain.audit;

import com.loopers.interfaces.consumer.audit.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional
    public void save(List<DomainEvent.Audit> audits) {

        List<EventLog> eventLogs = new ArrayList<>();
        for (DomainEvent.Audit audit : audits) {
            EventLog.create(audit.eventKey(),audit.eventName(),audit.domainId())   ;
        }

        auditRepository.saveAll(eventLogs);

    }
}
