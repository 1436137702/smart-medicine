package world.xuewei.controller;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.xuewei.dto.RespResult;
import world.xuewei.entity.User;
import world.xuewei.utils.Assert;

/**
 * 用户控制器
 *
 * 智慧医问-智能医药系统 - 本科毕业设计项目
 */
@RestController
@RequestMapping(value = "user")
public class UserController extends BaseController<User> {

    /**
     * 修改资料
     */
    @PostMapping("/saveProfile")
    public RespResult saveProfile(User user) {
        if (Assert.isEmpty(user)) {
            return RespResult.fail("保存对象不能为空");
        }
        User existUser = userService.get(user.getId());
        if (Assert.isEmpty(existUser)) {
            return RespResult.fail("用户不存在");
        }
        user.setUserAccount(existUser.getUserAccount());
        user.setUserPwd(existUser.getUserPwd());
        user.setUserEmail(existUser.getUserEmail());
        user.setUserSex(existUser.getUserSex());
        user.setRoleStatus(existUser.getRoleStatus());
        user = userService.save(user);
        session.setAttribute("loginUser", user);
        return RespResult.success("保存成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/savePassword")
    public RespResult savePassword(String oldPass, String newPass) {
        if (Assert.isEmpty(oldPass) || Assert.isEmpty(newPass)) {
            return RespResult.fail("密码不能为空");
        }
        if (!loginUser.getUserPwd().equals(oldPass)) {
            return RespResult.fail("旧密码错误");
        }
        if (newPass.length() < 6) {
            return RespResult.fail("新密码长度不能少于6位");
        }
        loginUser.setUserPwd(newPass);
        loginUser = userService.save(loginUser);
        session.setAttribute("loginUser", loginUser);
        return RespResult.success("保存成功");
    }
}
