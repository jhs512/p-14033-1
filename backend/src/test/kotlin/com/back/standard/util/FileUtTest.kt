package com.back.standard.util

import com.back.standard.sampleResource.SampleResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

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
}