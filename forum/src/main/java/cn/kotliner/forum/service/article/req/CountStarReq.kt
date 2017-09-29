package cn.kotliner.forum.service.article.req

import com.baidu.bjf.remoting.protobuf.FieldType
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf
import com.fasterxml.jackson.annotation.JsonProperty

/*********************************************************************
 * Created by chpengzh@foxmail.com
 * Copyright (c) http://chpengzh.com - All Rights Reserved
 *********************************************************************/
class CountStarReq {

    @Protobuf(order = 1, required = true, fieldType = FieldType.STRING)
    @JsonProperty("star_pool")
    var poolIds: List<String> = ArrayList()

}