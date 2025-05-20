package com.system.project1.service;

import com.system.project1.entity.Staff;
import java.util.List;

public interface StaffService {
    Staff saveStaff(Staff staff);

    List<Staff> getAllStaff();

    Staff getStaffById(Long id);

    Staff updateStaff(Staff staff);

    void deleteStaff(Long id);
}