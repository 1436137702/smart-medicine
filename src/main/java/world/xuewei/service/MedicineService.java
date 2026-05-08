package world.xuewei.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import world.xuewei.dao.MedicineDao;
import world.xuewei.dao.PageviewDao;
import world.xuewei.entity.Medicine;
import world.xuewei.entity.Pageview;
import world.xuewei.utils.Assert;
import world.xuewei.utils.BeanUtil;
import world.xuewei.utils.VariableNameUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 药品服务类
 *
 * 智慧医问-智能医药系统 - 本科毕业设计项目
 */
@Service
public class MedicineService extends BaseService<Medicine> {

    @Autowired
    protected MedicineDao medicineDao;

    @Autowired
    protected PageviewDao pageviewDao;

    @Override
    public List<Medicine> query(Medicine o) {
        QueryWrapper<Medicine> wrapper = new QueryWrapper();
        if (Assert.notEmpty(o)) {
            Map<String, Object> bean2Map = BeanUtil.bean2Map(o);
            for (String key : bean2Map.keySet()) {
                if (Assert.isEmpty(bean2Map.get(key))) {
                    continue;
                }
                wrapper.eq(VariableNameUtils.humpToLine(key), bean2Map.get(key));
            }
        }
        return medicineDao.selectList(wrapper);
    }

    @Override
    public List<Medicine> all() {
        return query(null);
    }

    @Override
    public Medicine save(Medicine o) {
        if (Assert.isEmpty(o.getId())) {
            medicineDao.insert(o);
        } else {
            medicineDao.updateById(o);
        }
        return medicineDao.selectById(o.getId());
    }

    @Override
    public Medicine get(Serializable id) {
        return medicineDao.selectById(id);
    }

    @Override
    public int delete(Serializable id) {
        return medicineDao.deleteById(id);
    }

    public Map<String, Object> getMedicineList(String nameValue, Integer page) {

        List<Medicine> medicineList = null;
        Map<String, Object> map = new HashMap<>(4);
        if (Assert.notEmpty(nameValue)) {
            medicineList = medicineDao.selectList(new QueryWrapper<Medicine>().
                    like("medicine_name", nameValue)
                    .or().like("keyword", nameValue)
                    .or().like("medicine_effect", nameValue)
                    .last("limit " + (page - 1) * 9 + "," + page * 9));
        } else {
            medicineList = medicineDao.selectList(new QueryWrapper<Medicine>()
                    .last("limit " + (page - 1) * 9 + "," + page * 9));
        }

        for (Medicine medicine : medicineList) {
            Pageview pageInfo = pageviewDao.selectOne(new QueryWrapper<Pageview>()
                    .eq("type", 2)
                    .eq("medicine_id", medicine.getId()));
            medicine.setPageviews(pageInfo == null ? 0 : pageInfo.getPageviews());
        }

        map.put("medicineList", medicineList);
        map.put("size", medicineList.size() < 9 ? 1 : medicineList.size() / 9 + 1);
        return map;
    }

    /**
     * 获取药品详情并记录浏览量
     */
    public Map<String, Object> getMedicineOne(Integer id) {
        Medicine medicine = medicineDao.selectById(id);
        Map<String, Object> map = new HashMap<>(4);

        Pageview pageInfo = pageviewDao.selectOne(new QueryWrapper<Pageview>()
                .eq("type", 2)
                .eq("medicine_id", id));
        if (Assert.isEmpty(pageInfo)) {
            pageInfo = new Pageview();
            pageInfo.setType(2);
            pageInfo.setMedicineId(id);
            pageInfo.setPageviews(1);
            pageviewDao.insert(pageInfo);
        } else {
            pageInfo.setPageviews(pageInfo.getPageviews() + 1);
            pageviewDao.updateById(pageInfo);
        }

        map.put("medicine", medicine);
        map.put("pageview", pageInfo.getPageviews());
        return map;
    }

    /**
     * 获取热门药品排行（按浏览量降序）
     */
    public List<Map<String, Object>> getHotMedicineList(int limit) {
        QueryWrapper<Pageview> pvWrapper = new QueryWrapper<>();
        pvWrapper.eq("type", 2);
        pvWrapper.isNotNull("medicine_id");
        pvWrapper.orderByDesc("pageviews");
        pvWrapper.last("LIMIT " + limit);
        List<Pageview> pageviews = pageviewDao.selectList(pvWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Pageview pv : pageviews) {
            Medicine medicine = medicineDao.selectById(pv.getMedicineId());
            if (medicine != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", medicine.getId());
                map.put("medicineName", medicine.getMedicineName());
                map.put("pageviews", pv.getPageviews());
                result.add(map);
            }
        }
        return result;
    }
}
