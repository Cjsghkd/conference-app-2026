package droidkaigi.openapi

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlMap
import java.io.File

private const val REF_PREFIX = "#/components/schemas/"

internal data class TrimResult(val keptPaths: Int, val keptSchemas: Int)

/**
 * Rewrites [specFile] in place, keeping only the paths under `/events/[eventName]/` and the
 * component schemas transitively referenced from them. Each operation's tags are replaced
 * with the path's resource segment (`/events/<eventName>/speakers` -> `Speakers`); the
 * generator derives the API class name from the tag, yielding one interface per resource.
 */
internal fun trimOpenApiSpec(specFile: File, eventName: String): TrimResult {
    val root = Yaml.default.parseToYamlNode(specFile.readText()).yamlMap
    val pathPrefix = "/events/$eventName/"

    val keptPaths = root.require("paths").filterKeys { it.startsWith(pathPrefix) }
    require(keptPaths.entries.isNotEmpty()) { "no paths start with $pathPrefix in $specFile" }
    val taggedPaths = YamlMap(
        keptPaths.entries.mapValues { (pathKey, pathItem) ->
            val resource = pathKey.content.removePrefix(pathPrefix).substringBefore('/')
            val tag = resource.replaceFirstChar(Char::uppercaseChar)
            if (pathItem !is YamlMap) pathItem
            else pathItem.mapMapValues { operation ->
                operation.set("tags", YamlList(listOf(YamlScalar(tag, operation.path)), operation.path))
            }
        },
        keptPaths.path,
    )

    val components = root.require("components")
    val schemas = components.require("schemas")
    val reachable = mutableSetOf<String>()
    val queue = ArrayDeque<String>().also { keptPaths.collectRefs(it) }
    while (true) {
        val name = queue.removeFirstOrNull() ?: break
        if (!reachable.add(name)) continue
        schemas.get<YamlNode>(name)?.collectRefs(queue)
    }

    // The base URL comes from ServerEnvironment at runtime and the top-level tag list only
    // feeds documentation grouping, so neither section feeds the generated code.
    val trimmed = root
        .remove("servers")
        .remove("tags")
        .replace("paths", taggedPaths)
        .replace("components", components.replace("schemas", schemas.filterKeys(reachable::contains)))

    // PlainExceptAmbiguous keeps response codes like "200" quoted so they stay strings.
    val yaml = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous))
    specFile.writeText(yaml.encodeToString(YamlNode.serializer(), trimmed))
    return TrimResult(keptPaths = taggedPaths.entries.size, keptSchemas = reachable.size)
}

private fun YamlNode.collectRefs(refs: MutableCollection<String>) {
    when (this) {
        is YamlMap -> {
            get<YamlScalar>("\$ref")?.content
                ?.takeIf { it.startsWith(REF_PREFIX) }
                ?.let { refs += it.removePrefix(REF_PREFIX) }
            entries.values.forEach { it.collectRefs(refs) }
        }

        is YamlList -> items.forEach { it.collectRefs(refs) }

        else -> Unit
    }
}

private fun YamlMap.require(key: String): YamlMap =
    get<YamlMap>(key) ?: throw IllegalArgumentException("missing \"$key\" mapping")

private fun YamlMap.filterKeys(predicate: (String) -> Boolean): YamlMap =
    YamlMap(entries.filterKeys { predicate(it.content) }, path)

private fun YamlMap.remove(key: String): YamlMap =
    YamlMap(entries.filterKeys { it.content != key }, path)

private fun YamlMap.replace(key: String, value: YamlNode): YamlMap =
    YamlMap(entries.mapValues { (k, v) -> if (k.content == key) value else v }, path)

private fun YamlMap.set(key: String, value: YamlNode): YamlMap =
    YamlMap(entries.filterKeys { it.content != key } + (YamlScalar(key, path) to value), path)

private fun YamlMap.mapMapValues(transform: (YamlMap) -> YamlNode): YamlMap =
    YamlMap(entries.mapValues { (_, v) -> if (v is YamlMap) transform(v) else v }, path)
