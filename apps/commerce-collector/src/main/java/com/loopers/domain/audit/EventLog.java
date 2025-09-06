package com.loopers.domain.audit;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLog extends BaseEntity {

    @Column(name = "event_key", nullable = false)
    private String eventKey;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "ref_domain_id", nullable = false)
    private Long domainId;

    @Builder
    private EventLog(String eventKey, String eventName, Long domainId) {
        this.eventKey = eventKey;
        this.eventName = eventName;
        this.domainId = domainId;
    }

    public static EventLog create(String eventKey, String eventName, Long domainId) {
        return EventLog
                .builder()
                .eventKey(eventKey)
                .eventName(eventName)
                .domainId(domainId)
                .build();
    }
}
