package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.MemberAttr
import jakarta.persistence.EntityManager
import org.hibernate.Session

class MemberAttrRepositoryImpl(
    private val entityManager: EntityManager,
) : MemberAttrRepositoryCustom {
    override fun findBySubjectAndName(subject: Member, name: String): MemberAttr? {
        return entityManager.unwrap(Session::class.java)
            .byNaturalId(MemberAttr::class.java)
            .using(MemberAttr::subject.name, subject)
            .using(MemberAttr::name.name, name)
            .load()
    }
}