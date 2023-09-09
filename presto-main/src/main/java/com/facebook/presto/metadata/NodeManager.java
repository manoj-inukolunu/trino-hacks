package com.facebook.presto.metadata;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.airlift.discovery.client.ServiceDescriptor;
import io.airlift.discovery.client.ServiceSelector;
import io.airlift.discovery.client.ServiceType;
import io.airlift.node.NodeInfo;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

public class NodeManager
{
    private static final Splitter DATASOURCES_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private final ServiceSelector serviceSelector;
    private final NodeInfo nodeInfo;

    @Inject
    public NodeManager(@ServiceType("presto") ServiceSelector serviceSelector, NodeInfo nodeInfo)
    {
        this.serviceSelector = checkNotNull(serviceSelector, "serviceSelector is null");
        this.nodeInfo = checkNotNull(nodeInfo, "nodeInfo is null");
    }

    public Set<Node> getActiveNodes()
    {
        ImmutableSet.Builder<Node> nodes = ImmutableSet.builder();
        for (ServiceDescriptor descriptor : serviceSelector.selectAllServices()) {
            try {
                nodes.add(nodeFromServiceDescriptor(descriptor));
            }
            catch (IllegalArgumentException e) {
                // ignore
                // TODO: log a warning here?
            }
        }
        return nodes.build();
    }

    public Set<Node> getActiveDatasourceNodes(String datasourceName)
    {
        ImmutableSet.Builder<Node> nodes = ImmutableSet.builder();
        for (ServiceDescriptor descriptor : serviceSelector.selectAllServices()) {
            String datasources = descriptor.getProperties().get("datasources");
            if (datasources == null) {
                continue;
            }
            datasources = datasources.toLowerCase();
            if (Iterables.contains(DATASOURCES_SPLITTER.split(datasources), datasourceName.toLowerCase())){
                try {
                    nodes.add(nodeFromServiceDescriptor(descriptor));
                }
                catch (IllegalArgumentException e) {
                    // ignore
                    // TODO: log a warning here?
                }
            }
        }
        return nodes.build();
    }

    public Node getCurrentNode()
    {
        for (Node node : getActiveNodes()) {
            if (node.getNodeIdentifier().equals(nodeInfo.getNodeId())) {
                return node;
            }
        }
        throw new IllegalStateException("current node is not in active set");
    }

    private static Node nodeFromServiceDescriptor(ServiceDescriptor descriptor)
    {
        URI uri = getHttpUri(descriptor);
        checkArgument(uri != null, "service descriptor is missing HTTP URI: %s", descriptor);
        return new Node(descriptor.getNodeId(), uri);
    }

    private static URI getHttpUri(ServiceDescriptor descriptor)
    {
        // favor https over http
        for (String type : asList("https", "http")) {
            String url = descriptor.getProperties().get(type);
            if (url != null) {
                try {
                    return new URI(url);
                }
                catch (URISyntaxException ignored) {
                }
            }
        }
        return null;
    }
}
