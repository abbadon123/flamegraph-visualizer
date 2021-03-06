package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


class BackTracesRequestHandler(urlDecoder: QueryStringDecoder, context: ChannelHandlerContext) :
        AccumulativeTreeRequestHandler(urlDecoder, context) {
    override val type: TreeType = TreeType.BACK_TRACES

    override fun doProcess(logFile: File) {
        val callTraces = TreeManager.getTree(logFile, TreeType.CALL_TRACES, filter)
        if (callTraces == null) { // it is not possible to get backtraces without calltraces
            ProfilerHttpRequestHandler.sendProto(context, null)
            return
        }
        /* if it is a request for all backtraces and calltraces tree contains more than maximumNodesCount then
         * send response BAD_REQUEST */
        if (ProfilerHttpRequestHandler.getParameter(urlDecoder, "method") == null &&
                callTraces.treeInfo.nodesCount > maximumNodesCount) {
            ProfilerHttpRequestHandler.sendStatus(HttpResponseStatus.BAD_REQUEST, context.channel(),
                    "Calltraces tree contains too many nodes: ${callTraces.treeInfo.nodesCount}. For this request it must" +
                            "contain less than $maximumNodesCount. For this tree only method backtraces are available.")
        } else {
            super.doProcess(logFile)
        }
    }
}
