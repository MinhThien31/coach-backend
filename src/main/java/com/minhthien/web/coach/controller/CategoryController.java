package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.CategoryRequest;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.CategoryResponse;
import com.minhthien.web.coach.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {

        return ApiResponse.<CategoryResponse>builder()
                .data(categoryService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {

        return ApiResponse.<CategoryResponse>builder()
                .data(categoryService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {

        categoryService.delete(id);

        return ApiResponse.<String>builder()
                .data("Category deleted")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {

        return ApiResponse.<CategoryResponse>builder()
                .data(categoryService.getById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll() {

        return ApiResponse.<List<CategoryResponse>>builder()
                .data(categoryService.getAll())
                .build();
    }
}
