package com.back.standard.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.net.URI
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

object Ut {
    object jwt {
        fun toString(secret: String, expireSeconds: Int, body: Map<String, Any>): String {
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            val jwt = Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()

            return jwt
        }

        fun isValid(secret: String, jwtStr: String): Boolean {
            return try {
                val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)

                true
            } catch (e: Exception) {
                false
            }
        }

        fun payload(secret: String, jwtStr: String): Map<String, Any>? {
            return try {
                val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .payload as Map<String, Any>

            } catch (e: Exception) {
                null
            }
        }
    }

    object json {
        lateinit var objectMapper: ObjectMapper

        fun toString(obj: Any, defaultValue: String = ""): String {
            return try {
                objectMapper.writeValueAsString(obj)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    object cmd {
        fun run(vararg args: String) {
            val isWindows = System
                .getProperty("os.name")
                .lowercase(Locale.getDefault())
                .contains("win")

            val builder = ProcessBuilder(
                args
                    .map { it.replace("{{DOT_CMD}}", if (isWindows) ".cmd" else "") }
                    .toList()
            )

            // 에러 스트림도 출력 스트림과 함께 병합
            builder.redirectErrorStream(true)

            // 프로세스 시작
            val process = builder.start()

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }

            val exitCode = process.waitFor()

            println("종료 코드: $exitCode")
        }

        fun runAsync(vararg args: String) {
            Thread(Runnable {
                run(*args)
            }).start()
        }
    }

    object file {
        lateinit var TMP_DIR_PATH: String

        fun getFileExt(filePath: String): String {
            val lastDotIndex = filePath.lastIndexOf('.')

            return if (lastDotIndex != -1 && lastDotIndex < filePath.length - 1) {
                filePath.substring(lastDotIndex + 1).lowercase()
            } else {
                "tmp"
            }
        }

        fun getFileExtTypeCodeFromFileExt(ext: String): String {
            return when (ext.lowercase()) {
                "jpeg", "jpg", "gif", "png", "svg", "webp" -> "img"
                "mp4", "avi", "mov" -> "video"
                "mp3", "m4a" -> "audio"
                else -> "etc"
            }
        }

        fun getFileExtType2CodeFromFileExt(ext: String): String {
            return when (ext.lowercase()) {
                "jpeg", "jpg" -> "jpg"
                else -> ext
            }
        }

        fun copy(src: String, dest: String) {
            val source = Path.of(src)
            val target = Path.of(dest).let {
                if (it.exists() && it.isDirectory()) it.resolve(source.fileName) else it
            }

            requireNotNull(target.parent).createDirectories()

            source.copyTo(target, overwrite = true)
        }

        fun delete(filePath: String) {
            Path.of(filePath).deleteIfExists()
        }

        fun exists(filePath: String): Boolean {
            return Path.of(filePath).exists()
        }

        fun download(url: String): String {
            val uri = URI(url)
            val path = uri.path
            val originFileName = path.substringAfterLast('/')
                .ifEmpty { "unknown" }
            val ext = getFileExt(originFileName)
            val finalFileName =
                "download_${System.currentTimeMillis()}__${originFileName}.$ext"
            val filePath = Path.of(TMP_DIR_PATH, finalFileName)

            uri.toURL().openStream().use { input ->
                filePath.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return filePath.toString()
        }
    }
}