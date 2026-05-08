package world.xuewei.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.xuewei.dto.RespResult;
import world.xuewei.entity.Feedback;
import world.xuewei.utils.Assert;

/**
 * 反馈控制器
 *
 * 智慧医问-智能医药系统 - 本科毕业设计项目
 */
@RestController
@RequestMapping(value = "feedback")
public class FeedbackController extends BaseController<Feedback> {

    /**
     * 保存反馈（自动填充登录用户信息）
     */
    @PostMapping("/save")
    public RespResult save(Feedback feedback) {
        if (Assert.isEmpty(feedback)) {
            return RespResult.fail("保存对象不能为空");
        }
        if (loginUser != null) {
            feedback.setUserId(loginUser.getId());
            if (Assert.isEmpty(feedback.getName())) {
                feedback.setName(loginUser.getUserName());
            }
            if (Assert.isEmpty(feedback.getEmail())) {
                feedback.setEmail(loginUser.getUserEmail());
            }
        }
        feedback.setStatus(0);
        feedback = feedbackService.save(feedback);
        return RespResult.success("提交成功，感谢您的反馈！");
    }

    /**
     * 标记反馈为已处理
     */
    @PostMapping("/markProcessed")
    public RespResult markProcessed(Integer id) {
        if (Assert.isEmpty(id)) {
            return RespResult.fail("反馈ID不能为空");
        }
        Feedback feedback = feedbackService.get(id);
        if (Assert.isEmpty(feedback)) {
            return RespResult.fail("反馈不存在");
        }
        feedback.setStatus(1);
        feedbackService.save(feedback);
        return RespResult.success("标记成功");
    }
}
