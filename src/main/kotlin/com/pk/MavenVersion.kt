package com.pk

import com.jayway.jsonpath.JsonPath
import okhttp3.Request

class MavenVersion(groupId: String, artifactId: String) {
    private val url = "http://search.maven.org/solrsearch/select?q=g:$groupId+AND+a:$artifactId&rows=5&wt=json"

    fun getLatest(): String {
        val req = Request.Builder()
            .get()
            .url(url)
            .build()
        val resp = Http.exec(req)
        assert(resp.successful) { "req failed with ${resp.code}" }
//        println(resp.body)

        val versions: List<String> = JsonPath.read(resp.body, "$.response.docs[*].latestVersion")
//        println("version = $versions")
        assert(versions.size == 1) { "got back more than one artifact: $versions" }
        return versions[0]
    }
}
