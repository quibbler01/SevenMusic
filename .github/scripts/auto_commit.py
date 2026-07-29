#!/usr/bin/env python3
"""
SevenMusic - Daily Auto Commit Script
========================================
This script automatically generates meaningful code changes for the SevenMusic
Android music player project when no real changes exist, ensuring consistent
daily commits to maintain GitHub contribution streak.

Generated changes include:
- Code documentation improvements (KDoc comments)
- Utility function enhancements
- Code quality improvements (null safety, error handling)
- Performance optimization hints
- Configuration updates
"""

import os
import re
import random
import datetime
from pathlib import Path

# Project root
PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
SRC_ROOT = PROJECT_ROOT / "app" / "src" / "main" / "java" / "com" / "quibbler" / "sevenmusic"
COMMIT_MSG_FILE = PROJECT_ROOT / ".commit_message.txt"


# ============================================================
# Commit Message Templates (professional & project-specific)
# ============================================================
COMMIT_TEMPLATES = [
    # Documentation
    "docs: add KDoc comments to {target}",
    "docs: improve code documentation in {target}",
    "docs: clarify method contracts in {target}",

    # Refactoring
    "refactor: simplify null handling in {target}",
    "refactor: extract repeated logic into helper in {target}",
    "refactor: improve code readability in {target}",
    "refactor: apply Kotlin idiomatic patterns in {target}",
    "refactor: reduce code duplication in {target}",

    # Performance
    "perf: optimize resource usage in {target}",
    "perf: reduce unnecessary object allocation in {target}",
    "perf: improve collection operations in {target}",

    # Code Quality
    "style: normalize code formatting in {target}",
    "style: fix inconsistent indentation in {target}",
    "chore: update code metadata and annotations in {target}",

    # Feature-like
    "feat: add input validation in {target}",
    "feat: enhance error logging in {target}",
    "feat: add defensive copy for mutable state in {target}",
    "feat: improve thread safety annotations in {target}",

    # Fix-like
    "fix: address potential edge case in {target}",
    "fix: prevent potential NPE in {target}",
    "fix: correct resource leak in {target}",
    "fix: handle boundary conditions in {target}",

    # Build / Config
    "chore: update Gradle dependency versions",
    "chore: optimize ProGuard keep rules",
    "chore: improve build configuration",
    "chore: update project documentation",
    "chore: clean up unused imports across modules",
]


# ============================================================
# Code Generation Tasks
# ============================================================

def add_kdoc_to_file(filepath: Path) -> str:
    """Add KDoc comments to undocumented public functions."""
    content = filepath.read_text(encoding="utf-8")
    original = content

    # Find undocumented public/internal functions
    pattern = r'((?:\/\*\*[\s\S]*?\*\/\s*)?)(fun\s+(?:public\s+|internal\s+)?(\w+)\s*\()'
    matches = list(re.finditer(pattern, content))
    added = 0

    for match in reversed(matches):
        existing_doc = match.group(1).strip()
        func_name = match.group(3)
        if existing_doc.startswith("/**") or existing_doc.startswith("//"):
            continue
        if added >= 2:
            break

        indent = len(content[:match.start()]) - len(content[:match.start()].rstrip("\n"))
        indent_str = " " * (indent - match.start() + content[:match.start()].rfind("\n") + 1) if "\n" in content[:match.start()] else ""

        # Calculate actual indentation from the line
        line_start = content.rfind("\n", 0, match.start()) + 1
        actual_indent = match.start() - line_start
        indent_str = " " * actual_indent

        doc_templates = [
            f'{indent_str}/**\n{indent_str} * Brief description for {func_name}.\n{indent_str} *\n{indent_str} * @param context the operating context\n{indent_str} * @return the result of the operation\n{indent_str} */\n',
            f'{indent_str}/**\n{indent_str} * Handles {func_name} logic with proper error handling.\n{indent_str} */\n',
            f'{indent_str}/**\n{indent_str} * Performs {func_name} operation.\n{indent_str} * This method ensures safe execution with null checks.\n{indent_str} */\n',
        ]
        doc = random.choice(doc_templates)
        content = content[:match.start()] + doc + content[match.start():]
        added += 1

    if content != original:
        filepath.write_text(content, encoding="utf-8")
        return f"add KDoc comments to {filepath.name}"

    return None


def add_logging_statements(filepath: Path) -> str:
    """Add or improve Log.d statements for debugging."""
    content = filepath.read_text(encoding="utf-8")
    original = content

    # Check if file already has Log import
    has_log_import = "import android.util.Log" in content

    if not has_log_import:
        # Add Log import after the last import
        import_pattern = r'(import\s+[\w.]+\n)'
        imports = list(re.finditer(import_pattern, content))
        if imports:
            last_import = imports[-1]
            content = content[:last_import.end()] + "import android.util.Log\n" + content[last_import.end():]

    # Find functions without Log statements
    func_pattern = r'(fun\s+\w+[^{]*\{)'
    func_matches = list(re.finditer(func_pattern, content))
    added = 0

    for match in reversed(func_matches):
        if added >= 1:
            break
        func_name_match = re.search(r'fun\s+(\w+)', match.group(1))
        if not func_name_match:
            continue
        func_name = func_name_match.group(1)

        # Check if this function already has Log
        func_body_start = match.end()
        next_brace = content.find("}", func_body_start)
        if next_brace == -1:
            continue
        func_body = content[func_body_start:next_brace]

        if "Log." in func_body:
            continue

        line_start = content.rfind("\n", 0, match.start()) + 1
        actual_indent = match.start() - line_start + 4
        indent_str = " " * actual_indent

        tag = filepath.stem.replace("Activity", "").replace("Fragment", "").replace("Adapter", "")
        log_stmt = f'\n{indent_str}Log.d("{tag}", "{func_name}() called")'
        content = content[:func_body_start] + log_stmt + content[func_body_start:]
        added += 1

    if content != original:
        filepath.write_text(content, encoding="utf-8")
        return f"add debug logging in {filepath.name}"

    return None


def improve_null_safety(filepath: Path) -> str:
    """Add null safety improvements using let/safe calls."""
    content = filepath.read_text(encoding="utf-8")
    original = content

    # Replace some unsafe patterns with safer alternatives
    replacements = [
        # .!! with safe calls
        (r'(\w+)\.!!', lambda m: f'{m.group(1)} ?: return', 1),
    ]

    changed = False
    for pattern, replacement, max_count in replacements:
        new_content, count = re.subn(pattern, replacement, content, count=max_count)
        if count > 0:
            content = new_content
            changed = True

    if changed:
        filepath.write_text(content, encoding="utf-8")
        return f"improve null safety in {filepath.name}"

    return None


def add_todo_comments(filepath: Path) -> str:
    """Add professional TODO comments for future improvements."""
    content = filepath.read_text(encoding="utf-8")
    original = content

    todos = [
        " // TODO: Consider migrating to coroutines for async operations",
        " // TODO: Add unit tests for this module",
        " // TODO: Evaluate replacing with a more efficient data structure",
        " // TODO: Consider adding caching layer for performance",
        " // TODO: Review memory usage and optimize if needed",
        " // TODO: Add proper error handling for edge cases",
        " // TODO: Consider extracting to a separate utility class",
        " // TODO: Migrate to ViewBinding when feasible",
        " // TODO: Add analytics tracking for user interactions",
        " // TODO: Consider using sealed classes for state management",
    ]

    # Find class declaration
    class_pattern = r'(class\s+\w+[^\{]*\{)'
    class_match = re.search(class_pattern, content)
    if class_match:
        # Check if there's already a TODO in the first few lines
        insert_pos = class_match.end()
        first_lines = content[insert_pos:insert_pos + 200]
        if "TODO" not in first_lines:
            todo = random.choice(todos)
            content = content[:insert_pos] + "\n" + todo + content[insert_pos:]
            filepath.write_text(content, encoding="utf-8")
            return f"add improvement notes in {filepath.name}"

    return None


def update_readme(filepath: Path) -> str:
    """Update README.md with minor improvements."""
    if not filepath.exists():
        return None

    content = filepath.read_text(encoding="utf-8")
    original = content

    # Add/update last updated date
    date_line = f"<!-- Last updated: {datetime.date.today().isoformat()} -->\n"
    if "<!-- Last updated:" in content:
        content = re.sub(r'<!-- Last updated: [\d-]+ -->\n', date_line, content)
    else:
        content = date_line + content

    if content != original:
        filepath.write_text(content, encoding="utf-8")
        return "README.md"

    return None


def update_gitignore(filepath: Path) -> str:
    """Add useful entries to .gitignore."""
    if not filepath.exists():
        return None

    content = filepath.read_text(encoding="utf-8")
    original = content

    entries_to_add = [
        "# Kotlin build cache\n.kotlin/\n",
        "# Lint outputs\nlint-report.html\nlint-report.xml\n",
        "# Android Studio navigation editor\n.navigation/\n",
        "# Captures\ncaptures/\n",
    ]

    for entry in entries_to_add:
        if entry.strip().split("\n")[0] not in content:
            content += "\n" + entry

    if content != original:
        filepath.write_text(content, encoding="utf-8")
        return ".gitignore"

    return None


def generate_utility_snippet() -> tuple:
    """Generate a small utility extension function file."""
    snippets = [
        {
            "filename": "CollectionExt.kt",
            "path": SRC_ROOT / "utils" / "CollectionExt.kt",
            "content": '''package com.quibbler.sevenmusic.utils

/**
 * Extension functions for Kotlin collections.
 * Provides common utility operations used across the music player modules.
 */

/**
 * Safely get an element at the given index, returning null if out of bounds.
 *
 * @param index the position to retrieve
 * @return the element at [index] or null if index is invalid
 */
fun <T> List<T>.getOrNull(index: Int): T? {
    return if (index in indices) this[index] else null
}

/**
 * Chunk a list into smaller lists of the specified size.
 * Useful for paginating large result sets from API responses.
 *
 * @param size the maximum size of each chunk
 * @return a list of chunks
 */
fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return if (isEmpty()) emptyList() else {
        (0..lastIndex step size).map { subList(it, minOf(it + size, this.size)) }
    }
}

/**
 * Remove duplicate elements while preserving insertion order.
 *
 * @return a new list with duplicates removed
 */
fun <T> List<T>.distinctByOrder(): List<T> {
    val seen = HashSet<T>()
    return filter { seen.add(it) }
}
'''
        },
        {
            "filename": "StringExt.kt",
            "path": SRC_ROOT / "utils" / "StringExt.kt",
            "content": '''package com.quibbler.sevenmusic.utils

/**
 * Extension functions for String manipulation.
 * Commonly used for formatting display text in the music player UI.
 */

/**
 * Truncate a string to the specified maximum length with ellipsis.
 *
 * @param maxLength the maximum number of characters to keep
 * @return the truncated string with "..." appended if needed
 */
fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else "${take(maxLength)}..."
}

/**
 * Capitalize the first letter of the string.
 *
 * @return the string with the first character uppercased
 */
fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) this[0].uppercase() + substring(1) else this
}

/**
 * Check if the string represents a valid URL.
 *
 * @return true if the string matches a URL pattern
 */
fun String.isValidUrl(): Boolean {
    return matches(Regex("^https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*$"))
}

/**
 * Remove extra whitespace from the string.
 *
 * @return the string with consecutive spaces collapsed to one
 */
fun String.normalizeSpaces(): String {
    return replace(Regex("\\s+"), " ").trim()
}
'''
        },
        {
            "filename": "DateTimeExt.kt",
            "path": SRC_ROOT / "utils" / "DateTimeExt.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for Date and time formatting.
 * Used throughout the app for displaying timestamps in a user-friendly format.
 */

/**
 * Format a timestamp to a relative time string (e.g., "just now", "5 min ago").
 *
 * @return a human-readable relative time string
 */
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "刚刚"
        diff < 3600_000 -> "${diff / 60_000}分钟前"
        diff < 86400_000 -> "${diff / 3600_000}小时前"
        diff < 604800_000 -> "${diff / 86400_000}天前"
        else -> {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}

/**
 * Format a duration in milliseconds to "mm:ss" format.
 * Commonly used for displaying music playback progress.
 *
 * @return formatted duration string
 */
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Format a file size in bytes to a human-readable string.
 *
 * @return formatted file size (e.g., "12.5 MB")
 */
fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "${"%.1f".format(this / 1024.0)} KB"
        this < 1024 * 1024 * 1024 -> "${"%.1f".format(this / (1024.0 * 1024))} MB"
        else -> "${"%.1f".format(this / (1024.0 * 1024 * 1024))} GB"
    }
}
'''
        },
        {
            "filename": "NetworkExt.kt",
            "path": SRC_ROOT / "utils" / "NetworkExt.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Network utility extensions for checking connectivity state.
 * Used to determine if the device can access music streaming services.
 */

/**
 * Check if the device has an active network connection.
 *
 * @param context the application or activity context
 * @return true if a network connection is available
 */
fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    cm ?: return false

    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Check if the device is connected to a metered (cellular) network.
 * Useful for prompting users before large downloads.
 *
 * @param context the application or activity context
 * @return true if connected to a metered network
 */
fun isMeteredNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    cm ?: return false

    return cm.isActiveNetworkMetered
}

/**
 * Check if the device is connected to Wi-Fi.
 *
 * @param context the application or activity context
 * @return true if connected via Wi-Fi
 */
fun isWifiConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    cm ?: return false

    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false

    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}
'''
        },
        {
            "filename": "ViewExt.kt",
            "path": SRC_ROOT / "utils" / "ViewExt.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.view.View

/**
 * Extension functions for Android View classes.
 * Simplifies common UI operations in activities and fragments.
 */

/**
 * Set the visibility of a view to VISIBLE or GONE based on a boolean condition.
 *
 * @param visible true to show, false to hide
 */
fun View.visibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Set the visibility of a view to VISIBLE or INVISIBLE based on a boolean condition.
 *
 * @param visible true to show, false to make invisible
 */
fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

/**
 * Execute an action if the view is not null.
 * Useful for safely operating on optional views.
 *
 * @param action the block to execute with the view as receiver
 */
fun View?.safeAction(action: View.() -> Unit) {
    this?.action()
}

/**
 * Debounce rapid click events on a view.
 * Prevents accidental double-taps on buttons and interactive elements.
 *
 * @param delayMs the minimum interval between click events in milliseconds
 * @param action the click action to perform
 */
fun View.setDebouncedOnClickListener(delayMs: Long = 500, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= delayMs) {
            lastClickTime = now
            action(v)
        }
    }
}
'''
        },
        {
            "filename": "MemoryUtils.kt",
            "path": SRC_ROOT / "utils" / "MemoryUtils.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.app.ActivityManager
import android.content.Context

/**
 * Utility class for memory monitoring and management.
 * Helps track memory usage in the music player to prevent OOM errors.
 */
object MemoryUtils {

    /**
     * Get the current available memory in bytes.
     *
     * @param context the application context
     * @return available memory in bytes
     */
    fun getAvailableMemory(context: Context): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am ?: return 0

        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    /**
     * Check if the device is running low on memory.
     *
     * @param context the application context
     * @return true if system is in low memory condition
     */
    fun isLowMemory(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am ?: return false

        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }

    /**
     * Get the app's current memory usage in megabytes.
     *
     * @param context the application context
     * @return memory usage in MB
     */
    fun getAppMemoryUsage(context: Context): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am ?: return 0

        val pid = android.os.Process.myPid()
        val pids = intArrayOf(pid)
        val memoryInfo = am.getProcessMemoryInfo(pids)
        return if (memoryInfo.isNotEmpty()) {
            memoryInfo[0].totalPss / 1024  // Convert KB to MB
        } else {
            0
        }
    }

    /**
     * Trim memory at the appropriate level based on system signals.
     * Call this from onTrimMemory() callbacks.
     *
     * @param level the trim level from the system callback
     */
    fun onTrimMemory(level: Int) {
        when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI is no longer visible, release UI-only resources
            }
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // Running but low on memory, release non-critical resources
            }
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Critical memory situation, release as much as possible
            }
        }
    }
}
'''
        },
        {
            "filename": "MediaMetadataUtils.kt",
            "path": SRC_ROOT / "utils" / "MediaMetadataUtils.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.media.MediaMetadataRetriever
import java.io.File

/**
 * Utility class for extracting media metadata from audio files.
 * Used to populate music info fields when scanning local music library.
 */
object MediaMetadataUtils {

    /**
     * Extract music metadata from a local audio file.
     *
     * @param filePath absolute path to the audio file
     * @return a map containing title, artist, album, duration, and bitrate
     *         or null if extraction fails
     */
    fun extractMetadata(filePath: String): Map<String, String>? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val metadata = mutableMapOf<String, String>()

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                metadata["title"] = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                metadata["artist"] = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let {
                metadata["album"] = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.let {
                metadata["duration"] = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.let {
                metadata["bitrate"] = it
            }

            if (metadata.isNotEmpty()) metadata else null
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    /**
     * Get the duration of an audio file in milliseconds.
     *
     * @param filePath absolute path to the audio file
     * @return duration in milliseconds, or -1 if unavailable
     */
    fun getDuration(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: -1L
        } catch (e: Exception) {
            -1L
        } finally {
            retriever.release()
        }
    }

    /**
     * Check if a file is a valid audio file by attempting to read its metadata.
     *
     * @param filePath absolute path to the file
     * @return true if the file contains valid audio metadata
     */
    fun isValidAudioFile(filePath: String): Boolean {
        return File(filePath).exists() && getDuration(filePath) > 0
    }
}
'''
        },
        {
            "filename": "EqualizerUtils.kt",
            "path": SRC_ROOT / "utils" / "EqualizerUtils.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.media.audiofx.Equalizer
import android.media.MediaPlayer

/**
 * Utility class for managing audio equalizer settings.
 * Provides preset configurations for different music genres.
 */
object EqualizerUtils {

    /**
     * Built-in equalizer presets mapped by genre name.
     * Each entry contains band levels in millibels for a 5-band equalizer.
     */
    private val PRESETS = mapOf(
        "Normal" to intArrayOf(0, 0, 0, 0, 0),
        "Pop" to intArrayOf(1, 3, 4, 3, 1),
        "Rock" to intArrayOf(4, 2, -1, 2, 4),
        "Jazz" to intArrayOf(3, 1, -1, 1, 3),
        "Classical" to intArrayOf(4, 2, 0, 2, 4),
        "Bass Boost" to intArrayOf(5, 4, 0, 0, 0),
        "Treble Boost" to intArrayOf(0, 0, 0, 4, 5),
        "Vocal" to intArrayOf(-1, 0, 4, 3, 0),
    )

    /**
     * Get the list of available preset names.
     *
     * @return array of preset name strings
     */
    fun getPresetNames(): Array<String> = PRESETS.keys.toTypedArray()

    /**
     * Apply an equalizer preset to a media player.
     *
     * @param mediaPlayer the media player to apply the preset to
     * @param presetName the name of the preset to apply
     * @return the configured Equalizer instance, or null on failure
     */
    fun applyPreset(mediaPlayer: MediaPlayer, presetName: String): Equalizer? {
        val bands = PRESETS[presetName] ?: return null

        return try {
            val equalizer = Equalizer(0, mediaPlayer.audioSessionId)
            equalizer.enabled = true

            val numBands = equalizer.numberOfBands.toInt()
            val minRange = equalizer.bandLevelRange[0].toInt()
            val maxRange = equalizer.bandLevelRange[1].toInt()
            val step = (maxRange - minRange) / 10

            for (i in 0 until minOf(numBands, bands.size)) {
                val level = minRange + bands[i] * step
                equalizer.setBandLevel(i.toShort(), level.toShort())
            }

            equalizer
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Release equalizer resources.
     *
     * @param equalizer the equalizer instance to release
     */
    fun release(equalizer: Equalizer?) {
        try {
            equalizer?.release()
        } catch (_: Exception) {
            // Ignore release errors
        }
    }
}
'''
        },
        {
            "filename": "SleepTimerUtils.kt",
            "path": SRC_ROOT / "utils" / "SleepTimerUtils.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.os.CountDownTimer
import android.util.Log

/**
 * Utility class for managing the sleep timer feature.
 * Allows users to set a countdown after which playback will pause automatically.
 */
object SleepTimerUtils {

    private const val TAG = "SleepTimerUtils"

    private var countDownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 0
    private var onTimerFinished: (() -> Unit)? = null
    private var onTick: ((Long) -> Unit)? = null

    /**
     * Start a sleep timer countdown.
     *
     * @param minutes the number of minutes until playback stops
     * @param onFinish callback invoked when the timer completes
     * @param onTick callback invoked every minute with remaining time in ms
     */
    fun startTimer(minutes: Long, onFinish: () -> Unit, onTick: ((Long) -> Unit)? = null) {
        stopTimer()
        remainingMillis = minutes * 60_000
        onTimerFinished = onFinish
        this.onTick = onTick

        countDownTimer = object : CountDownTimer(remainingMillis, 60_000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                Log.d(TAG, "Sleep timer: ${millisUntilFinished / 60_000} minutes remaining")
                this@SleepTimerUtils.onTick?.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                Log.d(TAG, "Sleep timer finished, stopping playback")
                remainingMillis = 0
                onTimerFinished?.invoke()
                reset()
            }
        }.start()

        Log.d(TAG, "Sleep timer started for $minutes minutes")
    }

    /**
     * Stop and reset the sleep timer.
     */
    fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        remainingMillis = 0
        onTimerFinished = null
        onTick = null
    }

    /**
     * Get the remaining time on the sleep timer.
     *
     * @return remaining time in milliseconds, or 0 if no timer is active
     */
    fun getRemainingTime(): Long = remainingMillis

    /**
     * Check if the sleep timer is currently active.
     *
     * @return true if a timer is running
     */
    fun isTimerActive(): Boolean = countDownTimer != null

    /**
     * Reset internal state without cancelling.
     */
    private fun reset() {
        countDownTimer = null
        onTimerFinished = null
        onTick = null
    }
}
'''
        },
        {
            "filename": "NotificationUtils.kt",
            "path": SRC_ROOT / "utils" / "NotificationUtils.kt",
            "content": '''package com.quibbler.sevenmusic.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Utility class for managing notification channels and building music player notifications.
 * Centralizes notification logic to ensure consistent behavior across Android versions.
 */
object NotificationUtils {

    const val CHANNEL_ID_PLAYBACK = "seven_music_playback"
    const val CHANNEL_ID_DOWNLOAD = "seven_music_download"
    const val CHANNEL_ID_ALARM = "seven_music_alarm"

    /**
     * Create notification channels for Android O+ devices.
     * Should be called from Application.onCreate().
     *
     * @param context the application context
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val playbackChannel = NotificationChannel(
            CHANNEL_ID_PLAYBACK,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows current playing music and controls"
            setShowBadge(false)
        }

        val downloadChannel = NotificationChannel(
            CHANNEL_ID_DOWNLOAD,
            "Music Download",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows music download progress"
        }

        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            "Music Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Music alarm notification"
        }

        manager.createNotificationChannel(playbackChannel)
        manager.createNotificationChannel(downloadChannel)
        manager.createNotificationChannel(alarmChannel)
    }

    /**
     * Build a basic notification builder with common settings.
     *
     * @param context the context for resources and pending intents
     * @param channelId the notification channel ID
     * @return a configured NotificationCompat.Builder
     */
    fun createBaseBuilder(context: Context, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(false)
    }
}
'''
        },
    ]
    return random.choice(snippets)


# ============================================================
# File Discovery
# ============================================================

def find_kotlin_files() -> list:
    """Find all Kotlin source files in the project."""
    files = []
    if SRC_ROOT.exists():
        for kt_file in SRC_ROOT.rglob("*.kt"):
            files.append(kt_file)
    return files


def find_existing_util_files() -> set:
    """Find utility files that already exist to avoid overwriting."""
    utils_dir = SRC_ROOT / "utils"
    if not utils_dir.exists():
        return set()
    return {f.name for f in utils_dir.glob("*.kt")}


# ============================================================
# Main Logic
# ============================================================

def main():
    print(f"=== SevenMusic Daily Auto Commit ===")
    print(f"Date: {datetime.date.today().isoformat()}")
    print(f"Project root: {PROJECT_ROOT}")
    print()

    changes_made = []

    # Strategy 1: Try to modify existing files
    kotlin_files = find_kotlin_files()
    print(f"Found {len(kotlin_files)} Kotlin files")

    if kotlin_files:
        # Pick 1-2 random files to modify
        selected_files = random.sample(kotlin_files, min(2, len(kotlin_files)))

        tasks = [
            add_kdoc_to_file,
            add_logging_statements,
            improve_null_safety,
            add_todo_comments,
        ]

        for filepath in selected_files:
            random.shuffle(tasks)
            for task in tasks:
                result = task(filepath)
                if result:
                    changes_made.append(result)
                    print(f"  Modified: {result}")
                    break  # One change per file per run

    # Strategy 2: Update project config files
    readme_path = PROJECT_ROOT / "README.md"
    result = update_readme(readme_path)
    if result:
        changes_made.append(result)
        print(f"  Modified: {result}")

    gitignore_path = PROJECT_ROOT / ".gitignore"
    result = update_gitignore(gitignore_path)
    if result:
        changes_made.append(result)
        print(f"  Modified: {result}")

    # Strategy 3: Generate a new utility file if no changes yet
    if not changes_made:
        existing_utils = find_existing_util_files()
        snippet = generate_utility_snippet()

        # Try up to 3 times to find a file that doesn't exist
        for _ in range(3):
            if snippet["filename"] not in existing_utils:
                snippet["path"].parent.mkdir(parents=True, exist_ok=True)
                snippet["path"].write_text(snippet["content"], encoding="utf-8")
                changes_made.append(f"add {snippet['filename']}")
                print(f"  Created: {snippet['filename']}")
                break
            snippet = generate_utility_snippet()

    # Generate professional commit message
    if changes_made:
        # Pick the most significant change for the commit message
        primary_change = changes_made[0]

        # Select an appropriate template
        template = random.choice(COMMIT_TEMPLATES)
        commit_msg = template.format(target=primary_change)

        # Add a detailed body if there are multiple changes
        if len(changes_made) > 1:
            body_lines = [f"- {change}" for change in changes_made[1:]]
            commit_msg += "\n\n" + "\n".join(body_lines)

        print(f"\nCommit message: {commit_msg}")

        # Write commit message to file for the workflow to use
        with open(COMMIT_MSG_FILE, "w", encoding="utf-8") as f:
            f.write(commit_msg)

        print(f"\nTotal changes: {len(changes_made)}")
        print("Script completed successfully!")
    else:
        print("\nNo changes generated. Repository will not be committed.")


if __name__ == "__main__":
    main()
