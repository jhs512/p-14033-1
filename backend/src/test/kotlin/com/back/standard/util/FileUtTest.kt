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
}