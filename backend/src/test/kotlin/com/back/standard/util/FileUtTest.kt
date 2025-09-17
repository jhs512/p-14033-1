package com.back.standard.util

import com.back.standard.extensions.base64Decode
import com.back.standard.extensions.base64Encode
import com.back.standard.sampleResource.SampleResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

@SpringBootTest
@ActiveProfiles("test")
class FileUtTest {
    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.fileName")
    fun t1() {
        assertThat(SampleResource.IMG_JPG_SAMPLE1.fileName).isEqualTo("sample1-200x300.jpg")
    }

    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.fileExt")
    fun t2() {
        assertThat(SampleResource.IMG_JPG_SAMPLE1.fileExt).isEqualTo("jpg")
    }

    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode")
    fun t3() {
        assertThat(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode).isEqualTo("img")
    }

    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code")
    fun t4() {
        assertThat(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code).isEqualTo("jpg")
    }

    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.filePath")
    fun t5() {
        assertThat(File(SampleResource.IMG_JPG_SAMPLE1.filePath).exists())
    }

    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.makeCopy()")
    fun t6() {
        val sample1Copied = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        assertThat(File(sample1Copied).exists())
        Ut.file.delete(sample1Copied)
    }

    @Test
    @DisplayName("Base64 Encode/Decode")
    fun t7() {
        val encoded = "abcd".base64Encode()

        assertThat(encoded.base64Decode()).isEqualTo("abcd")
    }

    @Test
    @DisplayName("download by url")
    fun t8() {
        val downloadFilePath = Ut.file.download("https://placehold.co/600x600?text=U_U")

        assertThat(Ut.file.exists(downloadFilePath)).isTrue()

        Ut.file.delete(downloadFilePath)
    }

    @Test
    @DisplayName("파일 다운로드 후 생성되는 파일의 파일명에 원본 파일명 포함시킴, 그래야 나중에 원본 파일명 복원 가능")
    fun t9() {
        val downloadFilePath = Ut.file.download("https://placehold.co/600x600?text=U_U")

        val originFileName = Ut.file.getOriginFileName(downloadFilePath)

        assertThat(originFileName).isEqualTo("600x600")

        Ut.file.delete(downloadFilePath)
    }

    @Test
    @DisplayName("http 응답헤더의 Content-Type 기반으로 확장자 추출")
    fun t10() {
        val downloadFilePath = Ut.file.download("https://placehold.co/600x600?text=U_U")

        assertThat(Ut.file.getFileExt(downloadFilePath)).isEqualTo("svg")

        Ut.file.delete(downloadFilePath)
    }

    @Test
    @DisplayName("Content-Type 로도 파악할 수 없다면 Tika 사용")
    fun t11() {
        val downloadFilePath =
            Ut.file.download("https://httpbin.org/response-headers?Content-Type=application/octet-stream")

        assertThat(Ut.file.getFileExt(downloadFilePath)).isEqualTo("txt")

        Ut.file.delete(downloadFilePath)
    }
}