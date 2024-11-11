package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.validation.comparison.ComparisonMethod

class JsonBQueryBuilder(
    private val filter: FilterCriteria
) {

    private fun buildJsonPath(column: String, path: String): String {
        return path.split(".").joinToString("->") { "'$it'" }
    }

    fun <T> buildQuery(entity: Class<T>): String {
        val entityName = filter.entitySimpleName.lowercase()
        val column = if (filter.jpaAttribute == "pseudoProperties") "pseudo_property_data" else filter.jpaAttribute
        val jsonPath = filter.pseudoPropertyPathToKey?.let { buildJsonPath(column, it) }

        val condition = buildCondition(column, jsonPath, filter.comparison, filter.value)

        val query = StringBuilder("SELECT * FROM $entityName WHERE $condition")

        // Append nested filter if exists
        filter.nestedFilter?.let {
            val nestedQueryBuilder = JsonBQueryBuilder(it)
            query.append(" AND (${nestedQueryBuilder.buildQuery(entity)})")
        }

        return query.toString()
    }

    private fun buildCondition(column: String, jsonPath: String?, comparison: ComparisonMethod?, value: Any?): String {
        val target = if (jsonPath != null) "$column->$jsonPath" else column
        return when (comparison) {
            ComparisonMethod.EQUALS -> "$target = '${value}'::jsonb"
            ComparisonMethod.NOT_EQUALS -> "$target != '${value}'::jsonb"
            ComparisonMethod.CONTAINS -> "$target @> '\"$value\"'::jsonb"
            ComparisonMethod.DOES_NOT_CONTAIN -> "NOT $target @> '\"$value\"'::jsonb"
            ComparisonMethod.STARTS_WITH -> "$target LIKE '$value%'"
            ComparisonMethod.DOES_NOT_START_WITH -> "$target NOT LIKE '$value%'"
            ComparisonMethod.ENDS_WITH -> "$target LIKE '%$value'"
            ComparisonMethod.DOES_NOT_END_WITH -> "$target NOT LIKE '%$value'"
            ComparisonMethod.REGEX -> "$target ~ '$value'"
            ComparisonMethod.DOES_NOT_MATCH_REGEX -> "$target !~ '$value'"
            ComparisonMethod.GREATER_THAN -> "($target)::numeric > $value"
            ComparisonMethod.LESS_THAN -> "($target)::numeric < $value"
            ComparisonMethod.GREATER_THAN_OR_EQUAL -> "($target)::numeric >= $value"
            ComparisonMethod.LESS_THAN_OR_EQUAL -> "($target)::numeric <= $value"
            ComparisonMethod.NOT_GREATER_THAN -> "($target)::numeric <= $value"
            ComparisonMethod.NOT_LESS_THAN -> "($target)::numeric >= $value"
            ComparisonMethod.BEFORE -> "($target)::timestamp < '$value'"
            ComparisonMethod.AFTER -> "($target)::timestamp > '$value'"
            ComparisonMethod.NOT_BEFORE -> "($target)::timestamp >= '$value'"
            ComparisonMethod.NOT_AFTER -> "($target)::timestamp <= '$value'"
            ComparisonMethod.BETWEEN -> {
                val (start, end) = value as Pair<*, *>
                "($target)::timestamp BETWEEN '$start' AND '$end'"
            }
            ComparisonMethod.NOT_BETWEEN -> {
                val (start, end) = value as Pair<*, *>
                "($target)::timestamp NOT BETWEEN '$start' AND '$end'"
            }
            ComparisonMethod.ENUM_EQUALS -> "$target = '$value'::jsonb"
            ComparisonMethod.ENUM_NOT_EQUALS -> "$target != '$value'::jsonb"
            ComparisonMethod.ENUM_IN -> "$target IN (${(value as List<*>).joinToString(",")})"
            ComparisonMethod.ENUM_NOT_IN -> "$target NOT IN (${(value as List<*>).joinToString(",")})"
            ComparisonMethod.IN -> "$target @> '[${(value as List<*>).joinToString(",")}]'::jsonb"
            ComparisonMethod.NOT_IN -> "NOT $target @> '[${(value as List<*>).joinToString(",")}]'::jsonb"
            ComparisonMethod.CONTAINS_ALL -> {
                val list = (value as List<*>).joinToString(",") { "\"$it\"" }
                "$target @> '[$list]'::jsonb"
            }
            ComparisonMethod.DOES_NOT_CONTAIN_ALL -> {
                val list = (value as List<*>).joinToString(",") { "\"$it\"" }
                "NOT $target @> '[$list]'::jsonb"
            }
            ComparisonMethod.CONTAINS_ANY -> {
                val list = (value as List<*>).joinToString(",") { "\"$it\"" }
                "$target ?| ARRAY[$list]"
            }
            ComparisonMethod.DOES_NOT_CONTAIN_ANY -> {
                val list = (value as List<*>).joinToString(",") { "\"$it\"" }
                "NOT $target ?| ARRAY[$list]"
            }
            null -> throw UnsupportedOperationException("Comparison method cannot be null")
        }
    }
}
