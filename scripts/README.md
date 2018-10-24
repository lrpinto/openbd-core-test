

/**
 * keys-delete-iter.lua
 * Performs mass deleting of all keys on a Redis in-memory data structure.
 * 
 * This implementation of mass deleting is based on the Redis commands KEYS, and DEL, where the result of KEYS is iteratively passed into DEL.
 * 
 * The LUA script implementation is iterative to prevent out of memory failure for key spaces with a large number of keys.
 * 
 * This is an atomic version for mass deleting, which means it will block any concurrent requests to the Redis server.
 * 
 * LUA scripts once loaded to the server will atomically run on the server side.
 *
 * @author Luisa
 * @author CodeArcs, Inc.
 *
 */

/**
 * keys-delete.lua
 * Performs mass deleting of all keys on a Redis in-memory data structure.
 * 
 * This implementation of mass deleting is based on the Redis commands KEYS, and DEL.
 * 
 * This is an atomic version for mass deleting, which means it will block any concurrent requests to the Redis server.
 * 
 * LUA scripts once loaded to the server run atomically on the server side.
 *
 * This script may not scale for a large number of keys.
 *
 * It results in out of memory failure when the key set is larger than the redis instance dedicated memory.
 * 
 * @author Luisa
 * @author CodeArcs, Inc.
 *
 */
 

/**
 * keys-expire-ttl-iter.lua
 * Performs mass deleting of all keys on a Redis in-memory data structure.
 * 
 * This implementation of mass deleting is based on the Redis commands KEYS, and TTL, where the result of KEYS is iteratively passed into TTL.
 * 
 * For every key the TTL is set to 1 second, so that keys soon expire.
 * 
 * The LUA script implementation is iterative to prevent out of memory failure for key spaces with a large number of keys.
 * 
 * This is an non-blocking version for mass deleting, since TTL does not blocks any concurrent requests on the redis server.
 * 
 * LUA scripts once loaded to the server will atomically run on the server side.
 *
 * @author Luisa
 * @author CodeArcs, Inc.
 *
 */