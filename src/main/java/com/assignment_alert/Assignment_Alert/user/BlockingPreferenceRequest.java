package com.assignment_alert.Assignment_Alert.user;

import java.util.HashMap;

import com.assignment_alert.Assignment_Alert.assignments.Priority;

public record BlockingPreferenceRequest(
    HashMap<Priority, Integer> blockingTimes
) {
    
}
