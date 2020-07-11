package io.gitlab.arturbosch.detekt.core.rules

import io.github.detekt.psi.absolutePath
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.api.MultiRule
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleId
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetId
import io.gitlab.arturbosch.detekt.api.internal.BaseRule
import io.gitlab.arturbosch.detekt.api.internal.PathFilters
import io.gitlab.arturbosch.detekt.api.internal.relativePath
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

fun Config.isActive(): Boolean =
    valueOrDefault("active", true)

fun Config.shouldAnalyzeFile(file: KtFile): Boolean {
    val filters = createPathFilters()
    return filters == null || !filters.isIgnored(file.absolutePath())
fun RuleSetProvider.createRuleSet(config: Config): RuleSet =
    instance(config.subConfig(ruleSetId))

fun RuleSet.shouldAnalyzeFile(file: KtFile, config: Config): Boolean {
    fun filters(): PathFilters? {
        val subConfig = config.subConfig(id)
        val includes = subConfig.valueOrNull<String>(Config.INCLUDES_KEY)?.trim()
        val excludes = subConfig.valueOrNull<String>(Config.EXCLUDES_KEY)?.trim()
        return PathFilters.of(includes, excludes)
    }

    val filters = filters()
    if (filters != null) {
        val path = Paths.get(file.relativePath())
        return !filters.isIgnored(path)
    }
    return true
}

fun RuleSet.visitFile(
    file: KtFile,
    bindingContext: BindingContext = BindingContext.EMPTY
): List<Finding> =
    rules.flatMap {
        it.visitFile(file, bindingContext)
        it.findings
    }

typealias IdMapping = Map<RuleId, RuleSetId>

fun associateRuleIdsToRuleSetIds(rules: Map<RuleSetId, List<BaseRule>>): IdMapping {
    fun extractIds(rule: BaseRule) =
        if (rule is MultiRule) {
            rule.rules.asSequence().map(Rule::ruleId)
        } else {
            sequenceOf(rule.ruleId)
        }
    return rules
        .asSequence()
        .flatMap { (ruleSetId, baseRules) ->
            baseRules
                .asSequence()
                .flatMap(::extractIds)
                .distinct()
                .map { ruleId -> ruleId to ruleSetId }
        }
        .toMap()
}
