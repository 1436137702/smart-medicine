package world.xuewei.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.xuewei.dto.RespResult;
import world.xuewei.entity.Consultation;
import world.xuewei.entity.Illness;
import world.xuewei.entity.Medicine;
import world.xuewei.entity.User;
import world.xuewei.utils.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 *
 * 智慧医问-智能医药系统 - 本科毕业设计项目
 */
@RestController
@RequestMapping("/message")
public class MessageController extends BaseController<User> {

    /**
     * 发送消息（RAG增强 + 咨询记录保存）
     */
    @PostMapping("/query")
    public RespResult query(String content) {
        if (Assert.isEmpty(loginUser)) {
            return RespResult.fail("请先登录");
        }
        if (Assert.isEmpty(content)) {
            return RespResult.fail("问题不能为空");
        }

        // 第一步：提取关键词，从本地数据库检索相关信息
        Map<String, Object> relatedInfo = extractRelatedInfo(content);
        List<Illness> relatedIllnesses = (List<Illness>) relatedInfo.get("illnesses");
        List<Medicine> relatedMedicines = (List<Medicine>) relatedInfo.get("medicines");

        // 第二步：构建增强prompt
        String enhancedPrompt = buildEnhancedPrompt(content, relatedIllnesses, relatedMedicines);

        // 第三步：调用通义千问API
        String aiAnswer = apiService.query(enhancedPrompt);

        // 第四步：保存咨询记录
        Consultation consultation = Consultation.builder()
                .userId(loginUser.getId())
                .question(content)
                .answer(aiAnswer)
                .relatedIllnessIds(buildIdString(relatedIllnesses, "id"))
                .relatedMedicineIds(buildIdString(relatedMedicines, "id"))
                .build();
        consultationService.save(consultation);

        // 第五步：返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("answer", aiAnswer);
        result.put("relatedIllnesses", relatedIllnesses);
        result.put("relatedMedicines", relatedMedicines);
        return RespResult.success("查询成功", result);
    }

    /**
     * 查询用户咨询历史
     */
    @PostMapping("/history")
    public RespResult history() {
        if (Assert.isEmpty(loginUser)) {
            return RespResult.fail("请先登录");
        }
        List<Consultation> historyList = consultationService.findByUserId(loginUser.getId());
        return RespResult.success("查询成功", historyList);
    }

    /**
     * 从用户问题中提取关键词并检索本地数据库
     */
    private Map<String, Object> extractRelatedInfo(String content) {
        List<Illness> relatedIllnesses = new ArrayList<>();
        List<Medicine> relatedMedicines = new ArrayList<>();

        // 按疾病名称模糊匹配
        List<Illness> allIllnesses = illnessService.all();
        for (Illness illness : allIllnesses) {
            if (matchKeyword(content, illness.getIllnessName())
                    || matchKeyword(content, illness.getIllnessSymptom())
                    || matchKeyword(content, illness.getSpecialSymptom())) {
                relatedIllnesses.add(illness);
            }
        }

        // 按药品名称/功效模糊匹配
        List<Medicine> allMedicines = medicineService.all();
        for (Medicine medicine : allMedicines) {
            if (matchKeyword(content, medicine.getMedicineName())
                    || matchKeyword(content, medicine.getKeyword())
                    || matchKeyword(content, medicine.getMedicineEffect())) {
                relatedMedicines.add(medicine);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("illnesses", relatedIllnesses);
        result.put("medicines", relatedMedicines);
        return result;
    }

    /**
     * 简单关键词匹配
     */
    private boolean matchKeyword(String content, String target) {
        if (Assert.isEmpty(target)) {
            return false;
        }
        String[] keywords = target.split("[、，,。；;\\s]+");
        for (String keyword : keywords) {
            if (keyword.length() >= 2 && content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建增强prompt，将本地检索到的疾病/药品信息拼接到系统提示中
     */
    private String buildEnhancedPrompt(String userQuestion, List<Illness> illnesses, List<Medicine> medicines) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的智能医疗助手。以下是与用户问题相关的本地医学知识库信息：\n\n");

        if (!illnesses.isEmpty()) {
            prompt.append("【相关疾病信息】\n");
            for (Illness illness : illnesses) {
                prompt.append("- 疾病名称：").append(illness.getIllnessName()).append("\n");
                prompt.append("  诱发因素：").append(illness.getIncludeReason()).append("\n");
                prompt.append("  主要症状：").append(illness.getIllnessSymptom()).append("\n");
                if (!Assert.isEmpty(illness.getSpecialSymptom())) {
                    prompt.append("  特殊症状：").append(illness.getSpecialSymptom()).append("\n");
                }
                prompt.append("\n");
            }
        }

        if (!medicines.isEmpty()) {
            prompt.append("【相关药品信息】\n");
            for (Medicine medicine : medicines) {
                prompt.append("- 药品名称：").append(medicine.getMedicineName()).append("\n");
                prompt.append("  功效：").append(medicine.getMedicineEffect()).append("\n");
                if (!Assert.isEmpty(medicine.getUsAge())) {
                    prompt.append("  用法用量：").append(medicine.getUsAge()).append("\n");
                }
                if (!Assert.isEmpty(medicine.getTaboo())) {
                    String taboo = medicine.getTaboo();
                    if (taboo.length() > 100) {
                        taboo = taboo.substring(0, 100) + "...";
                    }
                    prompt.append("  禁忌：").append(taboo).append("\n");
                }
                prompt.append("\n");
            }
        }

        prompt.append("请根据以上医学知识库信息，结合你的专业知识，对用户的问题进行详细解答。\n");
        prompt.append("如果本地知识库没有相关信息，请根据你的专业知识回答。\n");
        prompt.append("重要：请在回答末尾添加免责声明：本回答仅供参考，不构成医疗诊断或治疗建议。如有不适，请及时就医。\n\n");
        prompt.append("用户问题：").append(userQuestion);

        return prompt.toString();
    }

    /**
     * 构建ID字符串（逗号分隔）
     */
    private String buildIdString(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            try {
                java.lang.reflect.Field field = list.get(i).getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                sb.append(field.get(list.get(i)));
            } catch (Exception e) {
                sb.append("0");
            }
        }
        return sb.toString();
    }
}
