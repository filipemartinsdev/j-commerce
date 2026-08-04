package com.products.application.service;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Component;

@Component
public class ScrollSubrangeExtractor {
    public ScrollPosition getPosition(ScrollSubrange subrange){
        return subrange.position().orElse(ScrollPosition.keyset());
    }

    public Limit getLimit(ScrollSubrange subrange){
        return Limit.of(subrange.count().orElse(20));
    }
}
