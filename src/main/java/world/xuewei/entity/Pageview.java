package world.xuewei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 浏览实体
 *
 * @author XUEW
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@TableName("pageview")
public class Pageview implements Serializable {

    /**
     * 浏览量主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 浏览量
     */
    private Integer pageviews;

    /**
     * 类型：1疾病，2药品
     */
    private Integer type;

    /**
     * 病的id
     */
    private Integer illnessId;

    /**
     * 药品id
     */
    private Integer medicineId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
