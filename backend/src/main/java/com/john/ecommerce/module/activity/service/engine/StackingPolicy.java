package com.john.ecommerce.module.activity.service.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StackingPolicy {

    public List<PromoCandidate> select(List<PromoCandidate> candidates) {
        List<PromoCandidate> sorted = candidates.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getPriority() != null ? b.getPriority() : 0,
                        a.getPriority() != null ? a.getPriority() : 0))
                .toList();
        List<PromoCandidate> selected = new ArrayList<>();
        Set<String> usedGroups = new HashSet<>();
        for (PromoCandidate c : sorted) {
            String group = c.getStackGroup();
            if (group != null && !group.isBlank()) {
                if (usedGroups.contains(group)) {
                    continue;
                }
                if (!Boolean.TRUE.equals(c.getStackable())) {
                    usedGroups.add(group);
                }
            }
            selected.add(c);
        }
        return selected;
    }
}
