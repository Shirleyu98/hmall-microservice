package com.hmall.search.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "索引库实体")
@Document(indexName = "items")
public class ItemDoc {

    @Id
    @ApiModelProperty("商品id")
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    @ApiModelProperty("商品名称")
    private String name;

    @Field(type = FieldType.Integer)
    @ApiModelProperty("价格（分）")
    private Integer price;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty("商品图片")
    private String image;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty("类目名称")
    private String category;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty("品牌名称")
    private String brand;

    @Field(type = FieldType.Integer)
    @ApiModelProperty("销量")
    private Integer sold;

    @Field(type = FieldType.Integer,index = false)
    @ApiModelProperty("评论数")
    private Integer commentCount;

    @Field(type = FieldType.Boolean)
    @ApiModelProperty("是否是推广广告，true/false")
    private Boolean isAD;


    @Field(type = FieldType.Date)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}