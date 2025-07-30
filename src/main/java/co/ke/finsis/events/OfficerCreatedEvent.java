package co.ke.finsis.events;

import org.springframework.context.ApplicationEvent;

public class OfficerCreatedEvent extends ApplicationEvent {
    private Long officerId;

    public OfficerCreatedEvent(Object source, Long officerId) {
        super(source);
        this.officerId = officerId;
    }

    public Long getOfficerId() {
        return officerId;
    }
}