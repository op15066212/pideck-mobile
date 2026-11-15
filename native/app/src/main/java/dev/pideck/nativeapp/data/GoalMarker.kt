package dev.pideck.nativeapp.data

/**
 * 目标模式（goal mode）每轮注入的提示行。
 *
 * 这些不是 agent 说的话，是宿主那边的 goal 扩展往会话里塞的「继续干、还剩几轮」的记账行：
 *
 * ```
 * Continue the current goal.
 * Rounds used: 0/32
 * If done, write GOAL_COMPLETE. If blocked, write GOAL_BLOCKED: <reason>.
 * ```
 * ```
 * [GOAL MODE ACTIVE] Round: 1/32
 * ```
 *
 * 当正文段落铺在聊天流里又长又吵，手机上把它们收成一条小横条更好读；
 * 其余内容一个字都不动（agent 真的在讨论目标模式时，那些句子照常显示）。
 */
private val GOAL_LINE_PATTERNS = listOf(
    Regex("^\\[\\s*GOAL\\s+MODE[^\\]]*\\]\\s*.*$", RegexOption.IGNORE_CASE),
    Regex("^Rounds\\s+used\\s*:.*$", RegexOption.IGNORE_CASE),
    Regex("^Continue\\s+the\\s+current\\s+goal\\.?\\s*$", RegexOption.IGNORE_CASE),
    Regex("^(If\\s+done|If\\s+blocked)\\s*,.*GOAL_(COMPLETE|BLOCKED).*$", RegexOption.IGNORE_CASE),
)

internal fun isGoalModeLine(line: String): Boolean {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return false
    return GOAL_LINE_PATTERNS.any { it.matches(trimmed) }
}

/**
 * 把正文里的目标模式提示行摘出来。
 *
 * 返回 `(横条文字, 剩下的正文)`：横条文字是所有命中的行用「 · 」连起来（超长由界面省略），
 * 剩下的正文保持原顺序、去掉被摘走的行。没有命中时返回 `(null, 原文)`。
 *
 * 只在整行命中时才摘 —— 行内出现「Rounds used:」这种半句不动它，免得把正常讨论切碎。
 */
fun splitGoalBanner(text: String): Pair<String?, String> {
    if (text.isEmpty()) return null to text
    if (!text.contains("GOAL", ignoreCase = true) && !text.contains("Rounds", ignoreCase = true)) {
        return null to text
    }
    val lines = text.split('\n')
    val banner = mutableListOf<String>()
    val rest = mutableListOf<String>()
    for (line in lines) {
        if (isGoalModeLine(line)) banner += line.trim() else rest += line
    }
    if (banner.isEmpty()) return null to text
    val body = rest.joinToString("\n").replace(Regex("\\n{3,}"), "\n\n").trim()
    return banner.joinToString(" · ") to body
}
