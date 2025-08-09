package com.tnmaster.application.utils

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 字符串工具类测试
 * 这是一个示例测试，展示如何为工具类编写单元测试
 */
class StringUtilsTest {

  @Nested
  inner class IsBlankFunctionGroup {

    @Test
    fun `当字符串为null时应该返回true`() {
      // Given
      val input: String? = null

      // When
      val result = input.isNullOrBlank()

      // Then
      assertTrue(result, "null字符串应该被认为是空白的")
    }

    @Test
    fun `当字符串为空时应该返回true`() {
      // Given
      val input = ""

      // When
      val result = input.isBlank()

      // Then
      assertTrue(result, "空字符串应该被认为是空白的")
    }

    @Test
    fun `当字符串只包含空格时应该返回true`() {
      // Given
      val input = "   "

      // When
      val result = input.isBlank()

      // Then
      assertTrue(result, "只包含空格的字符串应该被认为是空白的")
    }

    @Test
    fun `当字符串包含非空白字符时应该返回false`() {
      // Given
      val input = "  hello  "

      // When
      val result = input.isBlank()

      // Then
      assertFalse(result, "包含非空白字符的字符串不应该被认为是空白的")
    }
  }

  @Nested
  inner class TrimFunctionGroup {

    @Test
    fun `正常情况下应该去除首尾空格`() {
      // Given
      val input = "  hello world  "

      // When
      val result = input.trim()

      // Then
      assertEquals("hello world", result, "应该去除首尾空格")
    }

    @Test
    fun `当字符串没有空格时应该返回原字符串`() {
      // Given
      val input = "hello"

      // When
      val result = input.trim()

      // Then
      assertEquals("hello", result, "没有空格的字符串应该保持不变")
    }

    @Test
    fun `当字符串只有空格时应该返回空字符串`() {
      // Given
      val input = "   "

      // When
      val result = input.trim()

      // Then
      assertEquals("", result, "只有空格的字符串trim后应该为空")
    }
  }

  @Nested
  inner class LengthFunctionGroup {

    @Test
    fun `正常字符串应该返回正确长度`() {
      // Given
      val input = "hello"

      // When
      val result = input.length

      // Then
      assertEquals(5, result, "字符串长度应该为5")
    }

    @Test
    fun `空字符串应该返回长度0`() {
      // Given
      val input = ""

      // When
      val result = input.length

      // Then
      assertEquals(0, result, "空字符串长度应该为0")
    }

    @Test
    fun `中文字符串应该返回正确长度`() {
      // Given
      val input = "你好世界"

      // When
      val result = input.length

      // Then
      assertEquals(4, result, "中文字符串长度应该为4")
    }
  }
}

