package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import java.time.LocalDateTime

data class MemberWithUsernameDto(
    val id: Int,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val isAdmin: Boolean,
    val username: String,
    val name: String,
    val profileImageUrl: String,
) {
    constructor(member: Member) : this(
        member.id,
        member.createDate,
        member.modifyDate,
        member.isAdmin,
        member.username,
        member.name,
        member.redirectToProfileImgUrlOrDefault
    )
}
