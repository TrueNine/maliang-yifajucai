package com.tnmaster.config

import org.casbin.jcasbin.main.Enforcer
import org.casbin.jcasbin.model.Model
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Test-specific Casbin configuration
 * 
 * This configuration creates a simple in-memory Casbin enforcer for testing
 * without requiring database connectivity or complex adapter setup.
 */
@TestConfiguration
@Profile("test")
class TestCasbinConfig {

    @Bean
    @Primary
    fun testEnforcer(): Enforcer {
        // Create a simple RBAC model for testing
        val modelText = """
            [request_definition]
            r = sub, obj, act

            [policy_definition]
            p = sub, obj, act

            [role_definition]
            g = _, _

            [policy_effect]
            e = some(where (p.eft == allow))

            [matchers]
            m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
        """.trimIndent()

        // Create enforcer with in-memory model (no adapter)
        val model = Model.newModelFromString(modelText)
        val enforcer = Enforcer(model)

        // Pre-populate with some test data
        initializeTestPolicies(enforcer)

        // Enable logging for debugging
        enforcer.enableLog(true)
        
        return enforcer
    }

    private fun initializeTestPolicies(enforcer: Enforcer) {
        try {
            // Add basic role definitions
            enforcer.addRoleForUser("test_user_123", "USER")
            enforcer.addRoleForUser("test_login_user", "USER")
            
            // Add basic permissions for USER role
            enforcer.addPolicy("USER", "READ_USER", "allow")
            enforcer.addPolicy("USER", "WRITE_USER", "allow")
            
            // Add admin role and permissions for more comprehensive testing
            enforcer.addPolicy("ADMIN", "READ_USER", "allow")
            enforcer.addPolicy("ADMIN", "WRITE_USER", "allow")
            enforcer.addPolicy("ADMIN", "DELETE_USER", "allow")
            
        } catch (e: Exception) {
            // Log but don't fail - some policies might already exist
            println("Warning: Could not initialize some test policies: ${e.message}")
        }
    }
}
