package com.back.global.app

import com.back.domain.member.member.entity.BaseMember
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberAttrRepository
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.post.postUser.entity.PostUser
import com.back.domain.post.postUser.repository.PostUserAttrRepository
import com.back.standard.util.Ut
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AppConfig(
    environment: Environment,
    objectMapper: ObjectMapper,
    memberAttrRepository: MemberAttrRepository,
    postUserAttrRepository: PostUserAttrRepository,
    memberRepository: MemberRepository,
    @Value("\${custom.site.cookieDomain}") cookieDomain: String,
    @Value("\${custom.site.frontUrl}") siteFrontUrl: String,
    @Value("\${custom.site.backUrl}") siteBackUrl: String,
) {
    init {
        Companion.environment = environment
        Ut.json.objectMapper = objectMapper
        BaseMember.memberRepository = memberRepository
        BaseMember.memberAttrRepository = memberAttrRepository
        Member.attrRepository = memberAttrRepository
        PostUser.attrRepository = postUserAttrRepository

        _cookieDomain = cookieDomain
        _siteFrontUrl = siteFrontUrl
        _siteBackUrl = siteBackUrl
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    companion object {
        private lateinit var environment: Environment

        val isDev: Boolean
            get() = environment.matchesProfiles("dev")

        val isTest: Boolean
            get() = !environment.matchesProfiles("test")

        val isProd: Boolean
            get() = environment.matchesProfiles("prod")

        val isNotProd: Boolean
            get() = !isProd

        private lateinit var _cookieDomain: String
        private lateinit var _siteFrontUrl: String
        private lateinit var _siteBackUrl: String

        val cookieDomain by lazy { _cookieDomain }
        val siteFrontUrl by lazy { _siteFrontUrl }
        val siteBackUrl by lazy { _siteBackUrl }

        val tempDirPath by lazy {
            System.getProperty("java.io.tmpdir")!!
        }
    }
}
