package com.alex.mallproduct.entity;

import com.alex.common.valid.AddGroup;
import com.alex.common.valid.ListValue;
import com.alex.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 品牌
 * 
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	//groups: 分组校验
	@TableId
	@NotNull(message = "id is required when update", groups = {UpdateGroup.class})
	@Null(message = "no id when save a new brand", groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "brand name cannot be blank", groups = {UpdateGroup.class, AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "must be a valid URL address", groups = {UpdateGroup.class, AddGroup.class})
	@NotEmpty(groups = {AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(vals = {0, 1}, groups = {AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "/^[a-zA-Z]$/", message = "must be a letter", groups = {UpdateGroup.class, AddGroup.class})
	@NotEmpty(groups = { AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0, message = "must >= 0", groups = {UpdateGroup.class, AddGroup.class})
	@NotNull(groups = {AddGroup.class})
	private Integer sort;

}
