package com.vm.frontend.service.impl;

import com.vm.base.utils.BaseService;
import com.vm.base.utils.DateUtil;
import com.vm.base.utils.VmProperties;
import com.vm.dao.mapper.VmFilesMapper;
import com.vm.dao.mapper.VmUsersMapper;
import com.vm.dao.po.*;
import com.vm.dao.qo.VmMoviesQueryBean;
import com.vm.frontend.service.inf.VmUsersService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by ZhangKe on 2017/12/28.
 */
@Service
public class VmUsersServiceImpl extends BaseService implements VmUsersService {
    @Autowired
    private VmUsersMapper vmUsersMapper;

    @Autowired
    private VmFilesMapper vmFilesMapper;


    /**
     * 通过username获取user
     *
     * @param username
     * @return
     */
    private VmUsers getUserByUsername(String username) {
        //是否存在此username的user
        VmUsersExample example = new VmUsersExample();
        VmUsersExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(username);
        criteria.andStatusEqualTo(BasePo.Status.NORMAL.getCode());
        List<VmUsers> vmUsers = vmUsersMapper.selectByExample(example);
        if (isEmptyList(vmUsers)) {
            return null;
        }
        return vmUsers.get(0);
    }

    @Override
    public VmUsers userLogin(CustomVmUsers user) throws Exception {


        //user是否存在
        VmUsers dbUser = getUserByUsername(user.getUsername());
        eject(isNullObject(dbUser),
                "userLogin dbUser is not exits ! user is :" + dbUser);

        //密码错误
        eject(!dbUser.getPassword().equals(user.getPassword()),
                "userLogin password is error ! user is :" + user);


        return dbUser;
    }

    @Override
    public VmUsers getUserBasicInfo(Long userId) {
        eject(isNullObject(userId),
                "getUserBasicInfo userId is null! userId is:" + userId);

        //获取指定id的user

        VmUsers dbUser = vmUsersMapper.selectByPrimaryKey(userId);

        eject(isNullObject(dbUser) || BasePo.Status.isDeleted(dbUser.getStatus()),
                "getUserBasicInfo user is not exits! userId is:" + userId);

        //屏蔽相关信息
        coverUserSomeInfo(dbUser);

        return dbUser;
    }

    /**
     * 屏蔽相关字段
     *
     * @param user
     * @return
     */
    private VmUsers coverUserSomeInfo(VmUsers user) {
        user.setPassword("");
        return user;
    }

    @Override
    @Transactional
    public VmUsers updateUserBasicInfo(CustomVmUsers user) throws Exception {
        //user是否存在
        VmUsers dbUser = getUserByUsername(user.getUsername());
        eject(isNullObject(dbUser),
                "updateUserBasicInfo dbUser is not exits ! user is :" + dbUser);

        vmUsersMapper.updateByPrimaryKeySelective(makeVmUsers(user));

        return coverUserSomeInfo(vmUsersMapper.selectByPrimaryKey(user.getId()));
    }

    @Override
    public void sendUserImg(Long fileId, VmMoviesQueryBean query, HttpServletResponse response) throws Exception {
        FileInputStream input = null;
        ServletOutputStream output = null;
        try {
            //获取用户图片id信息
            VmFiles file = vmFilesMapper.selectByPrimaryKey(fileId);
            String userImgPath = VmProperties.VM_USER_IMG_PATH;
            String width = query.getImgWidth();
            String userImgName = null;
            String contentType = null;
            if (file != null) {
                contentType = file.getContentType();
                userImgName = file.getFilename();
            }
            File f = new File(userImgPath + File.separator + width + "_" + userImgName);
            //不存在，返回默认图片
            if (!f.exists()) {
                f = new File(userImgPath + File.separator + VmProperties.VM_USER_IMG_DEFAULT_NAME);
            }
            output = response.getOutputStream();
            input = new FileInputStream(f);
            //设置响应的媒体类型

            response.setContentType(contentType); // 设置返回的文件类型
            IOUtils.copy(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(input, output);
        }
    }

    @Override
    @Transactional
    public VmUsers userRegist(CustomVmUsers user) throws Exception {

        //是否存在username相同的账户
        eject(!isNullObject(getUserByUsername(user.getUsername())),
                "userRegist username is exits!user is :" + user);
        vmUsersMapper.insert(makeRegistVmUser(user));
        VmUsers vmUsers = getUserByUsername(user.getUsername());
        return coverUserSomeInfo(vmUsers);
    }

    private VmUsers makeRegistVmUser(CustomVmUsers user) {
        VmUsers vmUsers = new VmUsers();
        vmUsers.setUsername(user.getUsername());
        vmUsers.setPassword(user.getPassword());
        vmUsers.setUpdateTime(DateUtil.unixTime().intValue());
        vmUsers.setCreateTime(DateUtil.unixTime().intValue());
        vmUsers.setStatus(CustomVmUsers.Status.NORMAL.getCode());
        vmUsers.setSex(CustomVmUsers.Sex.UNKNOWN.getCode());
        vmUsers.setImgUrl(CustomVmUsers.USER_IMG_URL_PREFIX);
        return vmUsers;
    }

    /**
     * 构建VmUsers
     *
     * @param user
     * @return
     */
    private VmUsers makeVmUsers(CustomVmUsers user) {
        VmUsers vmUser = new VmUsers();
        vmUser.setBirthday(user.getBirthday());
        vmUser.setUpdateTime(DateUtil.unixTime().intValue());
        vmUser.setDescription(user.getDescription());
        vmUser.setSex(user.getSex());
        vmUser.setUsername(user.getUsername());
        return vmUser;
    }

}