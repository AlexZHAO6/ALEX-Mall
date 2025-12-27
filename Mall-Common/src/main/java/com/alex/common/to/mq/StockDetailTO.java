package com.alex.common.to.mq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class StockDetailTO {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    private Long wareId;
    /**
     * status 1-yes 2-no
     */
    private Integer lockStatus;
}
