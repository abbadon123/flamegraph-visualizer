package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getFilter;

public abstract class TreeRequestHandler {

    @Nullable
    protected final Filter filter;
    final QueryStringDecoder urlDecoder;
    final ChannelHandlerContext context;
    @Nullable
    final File logFile;

    TreeRequestHandler(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        this.urlDecoder = urlDecoder;
        this.context = context;
        this.logFile = PluginFileManager.getInstance().getLogFile(urlDecoder);
        this.filter = getFilter(urlDecoder);
    }

    abstract void process();
}