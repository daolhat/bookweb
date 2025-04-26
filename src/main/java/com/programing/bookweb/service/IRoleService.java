package com.programing.bookweb.service;

import com.programing.bookweb.entity.Role;

import java.util.List;

public interface IRoleService {

    Role createRole(Role role);

    Role getRoleById(Long roleId);

    List<Role> getAllRoles();

    void deleteRole(Long roleId);
}
