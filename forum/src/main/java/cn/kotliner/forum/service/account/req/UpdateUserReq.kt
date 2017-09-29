package cn.kotliner.forum.service.account.req

import com.baidu.bjf.remoting.protobuf.FieldType.UINT64
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf
import com.fasterxml.jackson.annotation.JsonProperty

class UpdateUserReq {

    @Protobuf(required = true, order = 1, fieldType = UINT64, description = "用户id")
    @JsonProperty("id")
    var id: Long = 0

    @Protobuf(required = true, order = 2, fieldType = UINT64, description = "更新数据")
    @JsonProperty("args")
    var args: Map<String, String> = HashMap()

}