package world.xuewei.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import world.xuewei.dao.ConsultationDao;
import world.xuewei.entity.Consultation;
import world.xuewei.utils.Assert;
import world.xuewei.utils.BeanUtil;
import world.xuewei.utils.VariableNameUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 咨询记录服务类
 *
 * @author XUEW
 */
@Service
public class ConsultationService extends BaseService<Consultation> {

    @Autowired
    protected ConsultationDao consultationDao;

    @Override
    public List<Consultation> query(Consultation o) {
        QueryWrapper<Consultation> wrapper = new QueryWrapper<>();
        if (Assert.notEmpty(o)) {
            Map<String, Object> bean2Map = BeanUtil.bean2Map(o);
            for (String key : bean2Map.keySet()) {
                if (Assert.isEmpty(bean2Map.get(key))) {
                    continue;
                }
                wrapper.eq(VariableNameUtils.humpToLine(key), bean2Map.get(key));
            }
        }
        return consultationDao.selectList(wrapper);
    }

    @Override
    public List<Consultation> all() {
        return query(null);
    }

    @Override
    public Consultation save(Consultation o) {
        if (Assert.isEmpty(o.getId())) {
            consultationDao.insert(o);
        } else {
            consultationDao.updateById(o);
        }
        return consultationDao.selectById(o.getId());
    }

    @Override
    public Consultation get(Serializable id) {
        return consultationDao.selectById(id);
    }

    @Override
    public int delete(Serializable id) {
        return consultationDao.deleteById(id);
    }

    /**
     * 查询用户咨询历史
     */
    public List<Consultation> findByUserId(Integer userId) {
        QueryWrapper<Consultation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return consultationDao.selectList(wrapper);
    }
}
