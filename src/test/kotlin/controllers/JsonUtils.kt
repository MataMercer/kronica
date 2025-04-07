package controllers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Response
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.assertNotNull

class JsonUtils {
    fun getIdFromResponse(res: Response): Long{
        val jsonRes = getJsonFromResponse(res)
        val id = jsonRes["id"].toString().toLong()
        return id
    }

    fun getJsonFromResponse(res: Response): JsonNode {
        assertThat(res.code == 200).isTrue()
        val mapper = ObjectMapper()
        val body = res.body?.string()
        assertNotNull(body)
        return mapper.readTree(body)
    }

    fun checkJsonContainsIds(json: JsonNode, ids: List<Long>){
        val list = if (json["content"]!=null) json["content"].toList() else json.toList()
        var check = true
        ids.forEach {
            list.contains()
        }


    }


}