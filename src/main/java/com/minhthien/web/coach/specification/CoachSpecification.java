package com.minhthien.web.coach.specification;

import com.minhthien.web.coach.dto.request.CoachSearchRequest;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CoachSpecification {

    public static Specification<CoachProfile> filter(CoachSearchRequest request) {

        return (Root<CoachProfile> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            Join<CoachProfile, User> user = root.join("user");

            // search keyword
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {

                predicates.add(
                        cb.like(
                                cb.lower(user.get("fullName")),
                                "%" + request.getKeyword().toLowerCase() + "%"
                        )
                );
            }

            // search name
            if (request.getName() != null && !request.getName().isBlank()) {

                predicates.add(
                        cb.like(
                                cb.lower(user.get("fullName")),
                                "%" + request.getName().toLowerCase() + "%"
                        )
                );
            }

            // category
            if (request.getCategoryId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("category").get("id"),
                                request.getCategoryId()
                        )
                );
            }

            // location
            if (request.getLocation() != null && !request.getLocation().isBlank()) {

                predicates.add(
                        cb.like(
                                cb.lower(root.get("location")),
                                "%" + request.getLocation().toLowerCase() + "%"
                        )
                );
            }

            // min price
            if (request.getMinPrice() != null) {

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("price"),
                                request.getMinPrice()
                        )
                );
            }

            // max price
            if (request.getMaxPrice() != null) {

                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("price"),
                                request.getMaxPrice()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}