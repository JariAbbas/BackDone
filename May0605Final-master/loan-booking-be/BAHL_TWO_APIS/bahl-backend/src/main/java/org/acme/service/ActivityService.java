package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.repository.ActivityRepository;
import java.util.List;

@ApplicationScoped
public class ActivityService {

    @Inject
    ActivityRepository activityRepository;

    public List<String> getActivitiesForUser(String userId) {
        return activityRepository.getActivitiesByUserId(userId);
    }
}