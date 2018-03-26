package com.vm.admin.service.inf;

import com.vm.admin.dao.po.VmAdmins;
import com.vm.admin.dao.qo.VmAdminsQueryBean;
import com.vm.admin.service.dto.VmAdminsDto;
import com.vm.dao.util.BasePo;
import com.vm.dao.util.PageBean;

import java.util.List;

/**
 * Created by ZhangKe on 2018/3/26.
 */
public interface VmAdminsService {
    List<VmAdminsDto> getAdmins(PageBean page, VmAdminsQueryBean query);

    Long getAdminsTotal(PageBean page, VmAdminsQueryBean query);

    VmAdminsDto addAdmin(VmAdminsDto vmAdminsDto);

    VmAdminsDto editAdmin(VmAdminsDto vmAdminsDto);

    VmAdmins getAdminById(Long userId, BasePo.Status status, BasePo.IsDeleted isDeleted);

    VmAdmins getAdminById(Long userId, BasePo.IsDeleted isDeleted);
}