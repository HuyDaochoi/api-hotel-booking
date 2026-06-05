package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.RoleRequest;
import com.yo.apihotelbooking.repository.RoleRepository;
import com.yo.apihotelbooking.schemas.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) throws NotFoundException {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy quyền có ID: " + id));
    }

    @Transactional
    public Role createRole(RoleRequest request) throws BadRequestException {
        String roleName = request.getName().trim().toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new BadRequestException("Tên quyền này đã tồn tại: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        role.setDescription(request.getDescription());

        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(Long id, RoleRequest request) throws NotFoundException, BadRequestException {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy quyền có ID: " + id));

        String newName = request.getName().trim().toUpperCase();
        
        if (!role.getName().equals(newName) && roleRepository.existsByName(newName)) {
            throw new BadRequestException("Tên quyền này đã tồn tại: " + newName);
        }

        role.setName(newName);
        role.setDescription(request.getDescription());

        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long id) throws NotFoundException {
        if (!roleRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy quyền có ID: " + id);
        }
        roleRepository.deleteById(id);
    }
}