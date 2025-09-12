package com.back.standard.util

import com.back.standard.sampleResource.SampleResource
import com.back.standard.util.Ut.file.delete
import com.back.standard.util.Ut.file.getExtensionByTika
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class FileUtTest {
    @Test
    @DisplayName("SampleResource.IMG_JPG_SAMPLE1.makeCopy")
    fun t1() {
        val newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        // newFilePath 의 확장자가 jpg 인지 확인
        Assertions.assertThat(newFilePath).endsWith(".jpg")

        delete(newFilePath)
    }

    @Test
    @DisplayName("getExtensionByTika")
    fun t2() {
        val newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        val ext = getExtensionByTika(newFilePath)
        Assertions.assertThat(ext).isEqualTo("jpg")

        delete(newFilePath)
    }
}