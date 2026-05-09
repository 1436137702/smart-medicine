package world.xuewei.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import world.xuewei.dao.IllnessDao;
import world.xuewei.entity.*;
import world.xuewei.utils.Assert;
import world.xuewei.utils.BeanUtil;
import world.xuewei.utils.VariableNameUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 疾病服务类
 *
 * 智慧医问-智能医药系统 - 本科毕业设计项目
 */
@Service
public class IllnessService extends BaseService<Illness> {

    @Autowired
    protected IllnessDao illnessDao;

    @Override
    public List<Illness> query(Illness o) {
        QueryWrapper<Illness> wrapper = new QueryWrapper();
        if (Assert.notEmpty(o)) {
            Map<String, Object> bean2Map = BeanUtil.bean2Map(o);
            for (String key : bean2Map.keySet()) {
                if (Assert.isEmpty(bean2Map.get(key))) {
                    continue;
                }
                wrapper.eq(VariableNameUtils.humpToLine(key), bean2Map.get(key));
            }
        }
        return illnessDao.selectList(wrapper);
    }

    @Override
    public List<Illness> all() {
        return query(null);
    }

    @Override
    public Illness save(Illness o) {
        if (Assert.isEmpty(o.getId())) {
            illnessDao.insert(o);
        } else {
            illnessDao.updateById(o);
        }
        return illnessDao.selectById(o.getId());
    }

    @Override
    public Illness get(Serializable id) {
        return illnessDao.selectById(id);
    }

    @Override
    public int delete(Serializable id) {
        return illnessDao.deleteById(id);
    }

    public Map<String, Object> findIllness(Integer kind, String illnessName, Integer page) {

        if (page == null || page <= 0) {
            page = 1;
        }
        int offset = (page - 1) * 9;

        Map<String, Object> map = new HashMap<>(4);
        QueryWrapper<Illness> illnessQueryWrapper = new QueryWrapper<>();
        if (Assert.notEmpty(illnessName)) {
            illnessQueryWrapper
                    .like("illness_name", illnessName)
                    .or()
                    .like("include_reason", illnessName)
                    .or()
                    .like("illness_symptom", illnessName)
                    .or()
                    .like("special_symptom", illnessName);
        }
        if (kind != null) {
            if (Assert.notEmpty(illnessName)) {
                illnessQueryWrapper.last("and (kind_id = " + kind + ") ORDER BY create_time DESC limit " + offset + "," + page * 9);
            } else {
                illnessQueryWrapper.eq("kind_id", kind);
                illnessQueryWrapper.orderByDesc("create_time");
                illnessQueryWrapper.last("limit " + offset + "," + page * 9);
            }
        } else {
            illnessQueryWrapper.orderByDesc("create_time");
            illnessQueryWrapper.last("limit " + offset + "," + page * 9);
        }
        List<Map<String, Object>> list = illnessDao.selectMaps(illnessQueryWrapper);
        if (list == null) {
            list = new ArrayList<>();
        }
        int size = list.size();
        list.forEach(l -> {
            Integer id = MapUtil.getInt(l, "id");
            Pageview pageInfo = pageviewDao.selectOne(new QueryWrapper<Pageview>().eq("illness_id", id));
            l.put("kindName", "暂无归属类");
            l.put("create_time", MapUtil.getDate(l, "create_time"));
            l.put("pageview", pageInfo == null ? 0 : pageInfo.getPageviews());
            Integer kindId = MapUtil.getInt(l, "kind_id");
            if (Assert.notEmpty(kindId)) {
                IllnessKind illnessKind = illnessKindDao.selectById(kindId);
                if (Assert.notEmpty(illnessKind)) {
                    l.put("kindName", illnessKind.getName());
                }
            }
        });
        map.put("illness", list);
        map.put("size", size < 9 ? 1 : size / 9 + 1);
        return map;
    }

    public Map<String, Object> findIllnessOne(Integer id) {
        Illness illness = illnessDao.selectOne(new QueryWrapper<Illness>().eq("id", id));
        List<IllnessMedicine> illnessMedicines = illnessMedicineDao.selectList(new QueryWrapper<IllnessMedicine>().eq("illness_id", id));
        List<Medicine> list = new ArrayList<>(4);
        Map<String, Object> map = new HashMap<>(4);
        Pageview pageInfo = pageviewDao.selectOne(new QueryWrapper<Pageview>().eq("type", 1).eq("illness_id", id));
        if (Assert.isEmpty(pageInfo)) {
            pageInfo = new Pageview();
            pageInfo.setType(1);
            pageInfo.setIllnessId(id);
            pageInfo.setPageviews(1);
            pageviewDao.insert(pageInfo);
        } else {
            pageInfo.setPageviews(pageInfo.getPageviews() + 1);
            pageviewDao.updateById(pageInfo);
        }
        map.put("illness", illness);

        if (CollUtil.isNotEmpty(illnessMedicines)) {
            illnessMedicines.forEach(illnessMedicine -> {
                Medicine medicine = medicineDao.selectOne(new QueryWrapper<Medicine>().eq("id", illnessMedicine.getMedicineId()));
                if (ObjectUtil.isNotNull(medicine)) {
                    list.add(medicine);
                }
            });
            map.put("medicine", list);

        }

        return map;
    }

    public Illness getOne(QueryWrapper<Illness> queryWrapper) {
        return illnessDao.selectOne(queryWrapper);
    }

    /**
     * 获取热门疾病排行（按浏览量降序）
     */
    public List<Map<String, Object>> getHotIllnessList(int limit) {
        QueryWrapper<Pageview> pvWrapper = new QueryWrapper<>();
        pvWrapper.eq("type", 1);
        pvWrapper.isNotNull("illness_id");
        pvWrapper.orderByDesc("pageviews");
        pvWrapper.last("LIMIT " + limit);
        List<Pageview> pageviews = pageviewDao.selectList(pvWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Pageview pv : pageviews) {
            Illness illness = illnessDao.selectById(pv.getIllnessId());
            if (illness != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", illness.getId());
                map.put("illnessName", illness.getIllnessName());
                map.put("kindId", illness.getKindId());
                map.put("pageviews", pv.getPageviews());
                result.add(map);
            }
        }
        return result;
    }
}