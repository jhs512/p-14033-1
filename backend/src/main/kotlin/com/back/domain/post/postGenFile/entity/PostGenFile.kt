package com.back.domain.post.postGenFile.entity

import com.back.domain.post.post.entity.Post
import com.back.global.jpa.entity.BaseTime
import jakarta.persistence.*

@Entity
class PostGenFile(
    @ManyToOne(fetch = FetchType.LAZY)
    val post: Post,
    @Enumerated(EnumType.STRING)
    val typeCode: TypeCode,
    val filePath: String,
) : BaseTime() {
    enum class TypeCode {
        attachment,
        thumbnail
    }
}