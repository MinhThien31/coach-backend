package com.minhthien.web.coach.dto.request;

import com.minhthien.web.coach.enums.CoachSortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachSearchRequest {

    private String keyword;

    private Long categoryId;

    private String location;

    private Double minPrice;

    private Double maxPrice;

    private CoachSortType sort;

    private int page =0;

    private int size =10;
}
