package com.example.consistenthashing;

import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A scalable implementation of consistent hashing using a virtual node ring.
 * This implementation provides thread-safe operations and supports dynamic node
 * addition/removal
 * with minimal redistribution of keys.
 */
public class ConsistentHashRing {
    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashRing.class);

    private final ConcurrentSkipListMap<Long, VirtualNode> hashRing;
    private final Map<String, Node> physicalNodes;
    private final ReadWriteLock lock;
    private final int virtualNodesPerPhysicalNode;
    private final String virtualNodeSuffix;

    /**
     * Creates a new consistent hash ring with the specified number of virtual nodes
     * per physical node.
     * 
     * @param virtualNodesPerPhysicalNode Number of virtual nodes to create for each
     *                                    physical node
     * @param virtualNodeSuffix           Suffix to append to virtual node names for
     *                                    uniqueness
     */
    public ConsistentHashRing(int virtualNodesPerPhysicalNode, String virtualNodeSuffix) {
        this.hashRing = new ConcurrentSkipListMap<>();
        this.physicalNodes = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.virtualNodesPerPhysicalNode = virtualNodesPerPhysicalNode;
        this.virtualNodeSuffix = virtualNodeSuffix;

        logger.info("Initialized consistent hash ring with {} virtual nodes per physical node",
                virtualNodesPerPhysicalNode);
    }

    /**
     * Creates a new consistent hash ring with default settings (150 virtual nodes
     * per physical node).
     */
    public ConsistentHashRing() {
        this(150, "#");
    }

    /**
     * Adds a physical node to the hash ring.
     * 
     * @param nodeId      Unique identifier for the node
     * @param nodeAddress Network address of the node
     * @return true if the node was added successfully, false if it already exists
     */
    public boolean addNode(String nodeId, String nodeAddress) {
        lock.writeLock().lock();
        try {
            if (physicalNodes.containsKey(nodeId)) {
                logger.warn("Node {} already exists in the hash ring", nodeId);
                return false;
            }

            Node physicalNode = new Node(nodeId, nodeAddress);
            physicalNodes.put(nodeId, physicalNode);

            // Create virtual nodes for this physical node
            for (int i = 0; i < virtualNodesPerPhysicalNode; i++) {
                String virtualNodeId = nodeId + virtualNodeSuffix + i;
                VirtualNode virtualNode = new VirtualNode(virtualNodeId, physicalNode);
                long hash = hash(virtualNodeId);
                hashRing.put(hash, virtualNode);
            }

            logger.info("Added node {} with {} virtual nodes to the hash ring",
                    nodeId, virtualNodesPerPhysicalNode);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a physical node from the hash ring.
     * 
     * @param nodeId Unique identifier of the node to remove
     * @return true if the node was removed successfully, false if it doesn't exist
     */
    public boolean removeNode(String nodeId) {
        lock.writeLock().lock();
        try {
            if (!physicalNodes.containsKey(nodeId)) {
                logger.warn("Node {} does not exist in the hash ring", nodeId);
                return false;
            }

            // Remove all virtual nodes for this physical node
            Iterator<Map.Entry<Long, VirtualNode>> iterator = hashRing.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, VirtualNode> entry = iterator.next();
                if (entry.getValue().getPhysicalNode().getId().equals(nodeId)) {
                    iterator.remove();
                }
            }

            physicalNodes.remove(nodeId);

            logger.info("Removed node {} and all its virtual nodes from the hash ring", nodeId);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the node responsible for a given key.
     * 
     * @param key The key to hash and find the responsible node for
     * @return The physical node responsible for the key, or null if no nodes exist
     */
    public Node getNode(String key) {
        lock.readLock().lock();
        try {
            // Validate input key
            if (key == null || key.trim().isEmpty()) {
                logger.warn("Invalid key provided: {}", key);
                return null;
            }

            if (hashRing.isEmpty()) {
                logger.warn("Hash ring is empty, cannot find node for key: {}", key);
                return null;
            }

            long hash = hash(key);
            Map.Entry<Long, VirtualNode> entry = hashRing.ceilingEntry(hash);

            // If no entry found, wrap around to the first entry (ring behavior)
            if (entry == null) {
                entry = hashRing.firstEntry();
            }

            Node responsibleNode = entry.getValue().getPhysicalNode();
            logger.debug("Key '{}' (hash: {}) mapped to node: {}", key, hash, responsibleNode.getId());
            return responsibleNode;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets multiple nodes responsible for a given key (for replication purposes).
     * 
     * @param key   The key to hash and find responsible nodes for
     * @param count Number of nodes to return
     * @return List of physical nodes responsible for the key, ordered by proximity
     */
    public List<Node> getNodes(String key, int count) {
        lock.readLock().lock();
        try {
            // Validate input key
            if (key == null || key.trim().isEmpty()) {
                logger.warn("Invalid key provided: {}", key);
                return Collections.emptyList();
            }

            if (hashRing.isEmpty()) {
                logger.warn("Hash ring is empty, cannot find nodes for key: {}", key);
                return Collections.emptyList();
            }

            List<Node> nodes = new ArrayList<>();
            long hash = hash(key);
            NavigableMap<Long, VirtualNode> tailMap = hashRing.tailMap(hash, true);

            // Add nodes from the tail map
            for (VirtualNode virtualNode : tailMap.values()) {
                Node physicalNode = virtualNode.getPhysicalNode();
                if (!nodes.contains(physicalNode)) {
                    nodes.add(physicalNode);
                    if (nodes.size() >= count) {
                        break;
                    }
                }
            }

            // If we need more nodes, wrap around to the beginning
            if (nodes.size() < count) {
                for (VirtualNode virtualNode : hashRing.values()) {
                    Node physicalNode = virtualNode.getPhysicalNode();
                    if (!nodes.contains(physicalNode)) {
                        nodes.add(physicalNode);
                        if (nodes.size() >= count) {
                            break;
                        }
                    }
                }
            }

            logger.debug("Key '{}' mapped to {} nodes: {}",
                    key, nodes.size(),
                    nodes.stream().map(Node::getId).toList());
            return nodes;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets statistics about the hash ring distribution.
     * 
     * @return HashRingStats containing distribution information
     */
    public HashRingStats getStats() {
        lock.readLock().lock();
        try {
            Map<String, Integer> nodeDistribution = new HashMap<>();

            // Count virtual nodes per physical node
            for (VirtualNode virtualNode : hashRing.values()) {
                String nodeId = virtualNode.getPhysicalNode().getId();
                nodeDistribution.merge(nodeId, 1, Integer::sum);
            }

            return new HashRingStats(
                    physicalNodes.size(),
                    hashRing.size(),
                    virtualNodesPerPhysicalNode,
                    nodeDistribution);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets all physical nodes in the hash ring.
     * 
     * @return Set of all physical nodes
     */
    public Set<Node> getAllNodes() {
        lock.readLock().lock();
        try {
            return new HashSet<>(physicalNodes.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if the hash ring is empty.
     * 
     * @return true if no nodes exist, false otherwise
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return hashRing.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the total number of physical nodes.
     * 
     * @return Number of physical nodes
     */
    public int getNodeCount() {
        lock.readLock().lock();
        try {
            return physicalNodes.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Computes the hash value for a given key using MurmurHash3.
     * 
     * @param key The key to hash
     * @return The hash value
     */
    private long hash(String key) {
        return Hashing.murmur3_128()
                .hashString(key, StandardCharsets.UTF_8)
                .asLong();
    }

    /**
     * Represents a physical node in the distributed system.
     */
    public static class Node {
        private final String id;
        private final String address;
        private final long createdAt;

        public Node(String id, String address) {
            this.id = id;
            this.address = address;
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() {
            return id;
        }

        public String getAddress() {
            return address;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Node node = (Node) o;
            return Objects.equals(id, node.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Node{id='" + id + "', address='" + address + "'}";
        }
    }

    /**
     * Represents a virtual node in the hash ring.
     */
    private static class VirtualNode {
        private final String id;
        private final Node physicalNode;

        public VirtualNode(String id, Node physicalNode) {
            this.id = id;
            this.physicalNode = physicalNode;
        }

        public String getId() {
            return id;
        }

        public Node getPhysicalNode() {
            return physicalNode;
        }

        @Override
        public String toString() {
            return "VirtualNode{id='" + id + "', physicalNode=" + physicalNode.getId() + "}";
        }
    }

    /**
     * Statistics about the hash ring distribution.
     */
    public static class HashRingStats {
        private final int physicalNodeCount;
        private final int virtualNodeCount;
        private final int virtualNodesPerPhysicalNode;
        private final Map<String, Integer> nodeDistribution;

        public HashRingStats(int physicalNodeCount, int virtualNodeCount,
                int virtualNodesPerPhysicalNode, Map<String, Integer> nodeDistribution) {
            this.physicalNodeCount = physicalNodeCount;
            this.virtualNodeCount = virtualNodeCount;
            this.virtualNodesPerPhysicalNode = virtualNodesPerPhysicalNode;
            this.nodeDistribution = new HashMap<>(nodeDistribution);
        }

        public int getPhysicalNodeCount() {
            return physicalNodeCount;
        }

        public int getVirtualNodeCount() {
            return virtualNodeCount;
        }

        public int getVirtualNodesPerPhysicalNode() {
            return virtualNodesPerPhysicalNode;
        }

        public Map<String, Integer> getNodeDistribution() {
            return new HashMap<>(nodeDistribution);
        }

        @Override
        public String toString() {
            return String.format("HashRingStats{physicalNodes=%d, virtualNodes=%d, distribution=%s}",
                    physicalNodeCount, virtualNodeCount, nodeDistribution);
        }
    }
}