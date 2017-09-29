package cn.kotliner.forum.service.article.resp

import com.baidu.bjf.remoting.protobuf.FieldType
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf

class QueryReplyCountByAuthorResp {

    @Protobuf(order = 1, required = true, fieldType = FieldType.MAP)
    var result: Map<Long, Int> = HashMap()

}