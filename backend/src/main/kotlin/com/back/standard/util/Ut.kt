package com.back.standard.util

import com.back.standard.extensions.base64Decode
import com.back.standard.extensions.base64Encode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.apache.tika.Tika
import org.springframework.http.HttpHeaders
import java.net.HttpURLConnection
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
        lateinit var tika: Tika
        private const val ORIGINAL_FILE_NAME_SEPARATOR = "--originalFileName_"
        lateinit var TMP_DIR_PATH: String
        private val MIME_TYPE_MAP: LinkedHashMap<String, String> = linkedMapOf(
            "application/json" to "json",
            "text/plain" to "txt",
            "text/html" to "html",
            "text/css" to "css",
            "application/javascript" to "js",
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/gif" to "gif",
            "image/webp" to "webp",
            "image/svg+xml" to "svg",
            "application/pdf" to "pdf",
            "application/xml" to "xml",
            "application/zip" to "zip",
            "application/gzip" to "gz",
            "application/x-tar" to "tar",
            "application/x-7z-compressed" to "7z",
            "application/vnd.rar" to "rar",
            "audio/mpeg" to "mp3",
            "audio/mp4" to "m4a",
            "audio/x-m4a" to "m4a",
            "audio/wav" to "wav",
            "video/quicktime" to "mov",
            "video/mp4" to "mp4",
            "video/webm" to "webm",
            "video/x-msvideo" to "avi"
        )

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
            val connection = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000             // 커넥션 지연으로 소켓이 오래 붙어있는 상황 방지
                readTimeout = 30_000                // 응답 지연으로 스트림이 열린 채로 남는 상황 방지
            }

            try {
                connection.connect()

                // Content-Type 은 가능한 한 원시 헤더에서 꺼내며 charset 등은 제거
                val contentType: String = connection
                    .getHeaderField(HttpHeaders.CONTENT_TYPE)
                    ?.replace(Regex("charset=.*"), "")
                    ?.replace(";", "")
                    ?.trim()
                    ?: ""

                // Content-Disposition 에서 filename/filename* 모두 대응 (RFC 5987)
                val contentDispositionFileName: String = run {
                    val header = connection.getHeaderField(HttpHeaders.CONTENT_DISPOSITION) ?: return@run ""
                    // filename*=(UTF-8''...) 혹은 filename="..." 모두 매칭
                    val rx = Regex("""filename\*?=(?:UTF-8''|")?([^\";]+)""", RegexOption.IGNORE_CASE)
                    val m = rx.find(header) ?: return@run ""
                    // RFC5987 방식의 % 인코딩 대응
                    kotlin.runCatching { java.net.URLDecoder.decode(m.groupValues[1], "UTF-8") }
                        .getOrElse { m.groupValues[1] }
                }

                // 원본 파일명: Content-Disposition 우선, 없으면 URL 경로 마지막 세그먼트
                val originFileName = contentDispositionFileName.ifEmpty {
                    connection.url.path.substringAfterLast('/').ifEmpty { "unknown" }
                }

                // 확장자 결정: 기존 확장자(tmp 제외) 우선, 없으면 MIME 매핑, 그래도 없으면 tmp
                val ext = getFileExt(originFileName)
                    .takeUnless { it == "tmp" }
                    ?: MIME_TYPE_MAP[contentType]
                    ?: "tmp"

                // 저장 경로 구성(원본 파일명은 base64 로 안전 보관)
                val fileName =
                    "${System.currentTimeMillis()}${ORIGINAL_FILE_NAME_SEPARATOR}${originFileName.base64Encode()}.$ext"
                val filePath = Path.of(TMP_DIR_PATH, fileName)

                // 응답 코드별 입력 스트림 선택
                // - 2xx/3xx: inputStream
                // - 4xx/5xx: errorStream(없으면 inputStream) → 스트림을 반드시 use 로 닫음
                val status = connection.responseCode
                val src =
                    if (status >= 400) (connection.errorStream ?: connection.inputStream) else connection.inputStream

                // 파일 저장(Buffered 로 I/O 호출 수 감소 → 리소스 사용 최적화)
                src.use { input ->
                    filePath.outputStream().buffered().use { output ->
                        input.copyTo(output, DEFAULT_BUFFER_SIZE) // 기본 8KB 버퍼, 상황 따라 JVM 기본
                    }
                }

                // 확장자 복원 시도(여전히 tmp 인 경우에만 Tika 사용)
                val finalFilePath = if (ext == "tmp") {
                    restoreExtIfCanByTika(filePath.toString())
                } else {
                    filePath.toString()
                }

                return finalFilePath
            } finally {
                //    스트림은 위에서 use 로 닫혔으므로 여기서는 disconnect 만 확실히 호출
                kotlin.runCatching { connection.disconnect() }
            }
        }

        fun getOriginFileName(filePath: String): String {
            val path = Path.of(filePath)

            val fileName = path.fileName.toString()

            if (!fileName.contains(ORIGINAL_FILE_NAME_SEPARATOR)) return fileName

            val encodedOriginFileName =
                fileName.substringAfter(ORIGINAL_FILE_NAME_SEPARATOR, "")
                    .substringBeforeLast('.', "")

            return if (encodedOriginFileName.isNotEmpty()) {
                encodedOriginFileName.base64Decode()
            } else {
                "unknown"
            }
        }

        fun getFileExtByTika(filePath: String): String {
            val mimeType = tika.detect(filePath)
                .takeUnless {
                    it in setOf(
                        "",
                        "application/octet-stream",
                        "application/x-unknown",
                    )
                }
                ?: tika.detect(Path.of(filePath))

            return MIME_TYPE_MAP[mimeType] ?: "tmp"
        }

        fun restoreExtIfCanByTika(filePath: String): String {
            val extByTika = getFileExtByTika(filePath)

            if (extByTika == "tmp")
                return filePath

            return renameExt(filePath, extByTika)
        }

        fun mv(src: String, dest: String) {
            val source = Path.of(src)
            val target = Path.of(dest).let {
                if (it.exists() && it.isDirectory()) it.resolve(source.fileName) else it
            }

            requireNotNull(target.parent).createDirectories()

            source.moveTo(target, overwrite = true)
        }

        fun renameExt(filePath: String, ext: String): String {
            val path = Path.of(filePath)
            val newFilePath = path.parent.resolve("${path.nameWithoutExtension}.$ext")

            val dest = newFilePath.toString()

            mv(filePath, dest)

            return dest
        }
    }
}