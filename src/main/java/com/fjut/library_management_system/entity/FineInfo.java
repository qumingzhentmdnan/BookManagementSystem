package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.poi.hpsf.Decimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("fine_info")
public class FineInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 罚款id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 借阅id
     */
    @TableField("borrowing_id")
    private Long borrowingId;

    /**
     * 罚款金额
     */
    @TableField("fine_price")
    private BigDecimal finePrice;

    /**
     * 是否支付
     */
    @TableField("payed")
    private Boolean payed;

    /**
     * 支付日期
     */
    @TableField("paying_date")
    private LocalDate payingDate;
}
