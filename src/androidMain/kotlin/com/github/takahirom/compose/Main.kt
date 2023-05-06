@file:Suppress("FunctionName")

package com.github.takahirom.compose

import android.util.Log
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.ReusableComposeNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

data class TreeNode(
    var name: String
) {
    val children = mutableListOf<TreeNode>()
}

fun runApp() {
    val composer = Recomposer(Dispatchers.Main)

    GlobalSnapshotManager.ensureStarted()
    val mainScope = MainScope()
    mainScope.launch(DefaultChoreographerFrameClock) {
        composer.runRecomposeAndApplyChanges()
    }

    val rootNode = TreeNode("Root")
    Composition(TreeNodeApplier(rootNode), composer).apply {
        setContent {
            Content()
        }
        launchNodeLogger(mainScope, rootNode)
        launchComposeInsideLogger(composer, mainScope)
    }
}

private fun launchNodeLogger(
    mainScope: CoroutineScope,
    node: TreeNode
) {
    mainScope.launch {
        var nodeString = ""
        while (true) {
            val newNodeString = node.toString()
            if (nodeString != newNodeString) {
                nodeString = newNodeString
                println(nodeString)
            }
            yield()
        }
    }
}

@Composable
fun Content() {
    Node("1") {
        Node("2") {
            Node("4")
            Node("5")
        }
        Node("3")
    }
}

@Composable
private fun Node(
    name: String,
    content: @Composable () -> Unit = {}
) {
    ReusableComposeNode<TreeNode, TreeNodeApplier>(
        factory = {
            TreeNode(name)
        },
        update = {
            set(name) { this.name = it }
        },
        content = content
    )
}

class TreeNodeApplier(node: TreeNode) : AbstractApplier<TreeNode>(node) {
    override fun onClear() {
        println("onClear")
        current.children.clear()
    }

    override fun insertBottomUp(index: Int, instance: TreeNode) {
        // use top down
        Log.d("TreeNodeApplier", "insertBottomUp: ${instance.name}")
    }

    override fun insertTopDown(index: Int, instance: TreeNode) {
        println("insert$index")
        current.children.add(index, instance)
        Log.d("TreeNodeApplier", "insertTopDown: ${instance.name}")
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.children.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        println("remove$index")
        current.children.remove(index, count)
    }
}