package com.example.notes.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap

@Component
class SuspiciousRequestTracker(
    @Value("\${security.suspicious.window-ms:600000}")
    private val windowMs: Long,
    @Value("\${security.suspicious.threshold:5}")
    private val threshold: Int
) {
    private val attempts: ConcurrentHashMap<String, ArrayDeque<Long>> = ConcurrentHashMap()

    fun record(key: String): Boolean {
        val now = System.currentTimeMillis()
        val deque = attempts.computeIfAbsent(key) { ArrayDeque() }
        synchronized(deque) {
            while (deque.isNotEmpty() && now - deque.first > windowMs) {
                deque.removeFirst()
            }
            deque.addLast(now)
            return deque.size >= threshold
        }
    }
}
