package com.loopers.domain.activity.event;

import com.loopers.domain.activity.ActivityCommand;
import com.loopers.domain.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ActivityService activityService;

    @Async
    @EventListener
    public void view(ActivityEvent.View event) {
        activityService.view(ActivityCommand.View.from(event.getProductId()));
    }

}
