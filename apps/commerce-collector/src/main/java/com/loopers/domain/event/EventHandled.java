package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_handled")
public class EventHandled extends BaseEntity {

    private String eventId;
}
