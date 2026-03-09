package com.minhthien.web.coach.specification;

import com.minhthien.web.coach.dto.request.CoachSearchRequest;
import com.minhthien.web.coach.entity.CoachProfile;
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

            Join user = root.join("user");

            if (request.getKeyword() != null) {

                predicates.add(cb.like(
                        cb.lower(user.get("fullName")),
                        "%" + request.getKeyword().toLowerCase() + "%"
                ));
            }

            if (request.getCategoryId() != null) {

                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        request.getCategoryId()
                ));
            }

            if (request.getLocation() != null && !request.getLocation().isBlank()) {

                predicates.add(cb.like(
                        cb.lower(user.get("location")),
                        "%" + request.getLocation().toLowerCase() + "%"
                ));
            }

            if (request.getMinPrice() != null) {

                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("price"),
                        request.getMinPrice()
                ));
            }

            if (request.getMaxPrice() != null) {

                predicates.add(cb.lessThanOrEqualTo(
                        root.get("price"),
                        request.getMaxPrice()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
