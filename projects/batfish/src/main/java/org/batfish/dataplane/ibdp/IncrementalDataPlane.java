package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.Network;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpSession;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;

public class IncrementalDataPlane implements Serializable, DataPlane {

  private static final long serialVersionUID = 1L;

  private transient Network<BgpNeighbor, BgpSession> _bgpTopology;

  private Map<Ip, Set<String>> _ipOwners;

  private Map<Ip, String> _ipOwnersSimple;

  private Map<Ip, Map<String, Set<String>>> _ipVrfOwners;

  Map<String, Node> _nodes;

  private Topology _topology;

  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    return _nodes
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                eNode ->
                    eNode
                        .getValue()
                        .getVirtualRouters()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey, eVr -> eVr.getValue().getFib()))));
  }

  @Override
  public Map<Ip, Set<String>> getIpOwners() {
    return _ipOwners;
  }

  @Override
  public Map<Ip, Map<String, Set<String>>> getIpVrfOwners() {
    return _ipVrfOwners;
  }

  public Map<Ip, String> getIpOwnersSimple() {
    return _ipOwnersSimple;
  }

  public Map<String, Node> getNodes() {
    return _nodes;
  }

  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> getRibs() {
    ImmutableSortedMap.Builder<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
    _nodes.forEach(
        (hostname, node) -> {
          ImmutableSortedMap.Builder<String, GenericRib<AbstractRoute>> byVrf =
              new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
          node.getVirtualRouters()
              .forEach(
                  (vrf, virtualRouter) -> {
                    GenericRib<AbstractRoute> rib = virtualRouter.getMainRib();
                    byVrf.put(vrf, rib);
                  });
          ribs.put(hostname, byVrf.build());
        });
    return ribs.build();
  }

  @Override
  public Topology getTopology() {
    return _topology;
  }

  @Override
  public SortedSet<Edge> getTopologyEdges() {
    return _topology.getEdges();
  }

  protected void initIpOwners(
      Map<Ip, Set<String>> ipOwners,
      Map<Ip, String> ipOwnersSimple,
      Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    setIpOwners(ipOwners);
    setIpOwnersSimple(ipOwnersSimple);
    setIpVrfOwners(ipVrfOwners);
  }

  public void setIpOwners(Map<Ip, Set<String>> ipOwners) {
    _ipOwners = ipOwners;
  }

  public void setIpOwnersSimple(Map<Ip, String> ipOwnersSimple) {
    _ipOwnersSimple = ipOwnersSimple;
  }

  public void setIpVrfOwners(Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    this._ipVrfOwners = ipVrfOwners;
  }

  public void setNodes(Map<String, Node> nodes) {
    _nodes = nodes;
  }

  public void setTopology(Topology topology) {
    _topology = topology;
  }

  @Override
  public Network<BgpNeighbor, BgpSession> getBgpTopology() {
    return _bgpTopology;
  }

  @Override
  public Map<String, Configuration> getConfigurations() {
    return _nodes
        .entrySet()
        .stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getConfiguration()));
  }

  void setBgpTopology(Network<BgpNeighbor, BgpSession> bgpTopology) {
    this._bgpTopology = bgpTopology;
  }

  /**
   * Retrieve the {@link PrefixTracer} for each {@link VirtualRouter} after dataplane computation.
   * Map structure: Hostname -> VRF name -> prefix tracer.
   */
  public SortedMap<String, SortedMap<String, PrefixTracer>> getPrefixTracingInfo() {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return _nodes
        .entrySet()
        .stream()
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Comparator.naturalOrder(),
                Entry::getKey,
                e ->
                    e.getValue()
                        .getVirtualRouters()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableSortedMap.toImmutableSortedMap(
                                Comparator.naturalOrder(),
                                Entry::getKey,
                                e2 -> e2.getValue().getPrefixTracer()))));
  }

  @Override
  public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return _nodes
        .entrySet()
        .stream()
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Comparator.naturalOrder(),
                Entry::getKey,
                e ->
                    e.getValue()
                        .getVirtualRouters()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableSortedMap.toImmutableSortedMap(
                                Comparator.naturalOrder(),
                                Entry::getKey,
                                e2 -> e2.getValue().getPrefixTracer().summarize()))));
  }
}