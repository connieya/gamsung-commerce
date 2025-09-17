package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_handled")
public class EventHandled extends BaseEntity {

    @Column(name = "event_id" , nullable = false)
    private String eventId;

    @Column(name = "topic_name" , nullable = false)
    private String topicName;
}
