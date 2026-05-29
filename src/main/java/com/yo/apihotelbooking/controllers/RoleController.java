package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.RoleRequest;
import com.yo.apihotelbooking.schemas.domain.Role;
import com.yo.apihotelbooking.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ApiResponse<Role> getRoleById(@PathVariable Long id) throws Exception {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @PostMapping
    public ApiResponse<Role> createRole(@RequestBody @Valid RoleRequest request) throws Exception {
        return ApiResponse.success("Tạo quyền thành công", roleService.createRole(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Role> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleRequest request) throws Exception {
        return ApiResponse.success("Cập nhật quyền thành công", roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) throws Exception {
        roleService.deleteRole(id);
        return ApiResponse.successMessage("Xóa quyền thành công");
    }
}