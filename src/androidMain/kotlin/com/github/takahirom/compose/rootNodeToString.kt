package com.github.takahirom.compose

fun TreeNode.rootNodeToString(): String {
    return buildString {
        appendLine("RootNode")
        children.forEachIndexed { index, node ->
            if (index == children.lastIndex) {
                appendLine("└── $node")
            } else {
                appendLine("├── $node")
            }
        }
    }
}
