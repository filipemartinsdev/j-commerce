package com.products.application.service;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Component;

import java.util.OptionalInt;

@Component
public class ScrollSubrangeExtractor {
    public ScrollPosition getPosition(ScrollSubrange subrange){
        return subrange.position().orElse(ScrollPosition.keyset());
    }

    public Limit getLimit(ScrollSubrange subrange){
        OptionalInt count = subrange.count();

        return Limit.of(
                count.isPresent() && count.getAsInt() <= 20
                        ? count.getAsInt()
                        : 20
        );
    }
}
