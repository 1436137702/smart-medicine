package world.xuewei.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import world.xuewei.entity.Consultation;

/**
 * 咨询记录数据库访问
 *
 * @author XUEW
 */
@Repository
public interface ConsultationDao extends BaseMapper<Consultation> {

}
